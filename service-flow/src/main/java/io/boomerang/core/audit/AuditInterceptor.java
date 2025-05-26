package io.boomerang.core.audit;

import io.boomerang.common.model.*;
import io.boomerang.core.model.Token;
import io.boomerang.security.IdentityService;
import io.boomerang.workflow.model.Team;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Intercepts the Create, Update, Delete, and Actions performed on objects and creates an Audit log
 *
 * <p>We don't audit read events
 *
 * <p>TODO: add an audit level to the application.properties file to enable/disable auditing
 *
 * <p>Ref: https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/advice.html Ref:
 * https://www.baeldung.com/spring-boot-authentication-audit
 */
@Aspect
@Component
@ConditionalOnProperty(name = "flow.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditInterceptor {
  private static final Logger LOGGER = LogManager.getLogger();

  @Autowired private IdentityService identityService;

  @Autowired private AuditRepository auditRepository;

  private Map<String, String> teamNameToAuditId = new HashMap<>();

  /** Intercepts the audit, pulls out token information and passes onto an Async task */
  @AfterReturning(pointcut = "@annotation(audit)", returning = "result")
  public void interceptAudit(JoinPoint joinPoint, Audit audit, Object result) {
    LOGGER.info("AuditInterceptor - Intercepted {} on {}", audit.action(), audit.scope());
    Object[] args = joinPoint.getArgs();
    LOGGER.debug("AuditInterceptor - Arguments: {}", Arrays.stream(args).toList().toString());

    Token accessToken = this.identityService.getCurrentIdentity();

    processAudit(audit, args, result, accessToken);
  }

  // TODO move to common so we can annotate engine
  @Async
  private void processAudit(Audit audit, Object[] args, Object result, Token token) {
    // Retrieve Team from args if applicable - its always the first param
    Optional<String> parent = Optional.empty();
    switch (audit.scope()) {
      case workflow, teamtask, schedule -> Optional.of((String) args[0]);
    }
    String ref = "";
    Optional<String> label = Optional.empty();
    Optional<Map<String, String>> data = Optional.empty();

    try {
      switch (audit.scope()) {
        case workflow -> {
          switch (audit.action()) {
            case create, update -> {
              Workflow workflow = castResult(result, Workflow.class);
              ref = workflow.getName();
              label = Optional.of(workflow.getDisplayName());
            }
            case submit -> {
              WorkflowRun workflowRun = castResult(result, WorkflowRun.class);
              ref = workflowRun.getWorkflowName();
            }
            case delete -> {
              ref = (String) args[1];
            }
          }
        }
        case workflowrun -> {
          switch (audit.action()) {
            case create -> {
              WorkflowRun workflowRun = castResult(result, WorkflowRun.class);
              ref = workflowRun.getId();
              Map<String, String> runData = new HashMap<>();
              runData.put("duration", String.valueOf(workflowRun.getDuration()));
              runData.put("workflowRef", workflowRun.getWorkflowRef());
              runData.put("phase", workflowRun.getPhase().toString());
              runData.put("status", workflowRun.getStatus().toString());
              data = Optional.of(runData);
            }
          }
        }
        case workflowtemplate -> {
          switch (audit.action()) {
            case create, update -> {
              WorkflowTemplate template = castResult(result, WorkflowTemplate.class);
              ref = template.getName();
              label = Optional.of(template.getDisplayName());
            }
            case delete -> {
              ref = (String) args[0];
            }
          }
        }
        case team -> {
          switch (audit.action()) {
            case create, update -> {
              Team team = castResult(result, Team.class);
              ref = team.getName();
              label = Optional.of(team.getDisplayName());
            }
            case delete -> {
              ref = (String) args[0];
            }
          }
        }
        case task -> {
          switch (audit.action()) {
            case create, update -> {
              Task task = castResult(result, Task.class);
              ref = task.getName();
              label = Optional.of(task.getDisplayName());
            }
            case delete -> {
              ref = (String) args[0];
            }
          }
        }
        case teamtask -> {
          switch (audit.action()) {
            case create, update -> {
              Task task = castResult(result, Task.class);
              ref = task.getName();
              label = Optional.of(task.getDisplayName());
            }
            case delete -> {
              ref = (String) args[1];
            }
          }
        }
        case schedule -> {
          switch (audit.action()) {
            case create, update -> {
              WorkflowSchedule schedule = castResult(result, WorkflowSchedule.class);
              ref = schedule.getId();
              label = Optional.of(schedule.getName());
            }
            case delete -> {
              ref = (String) args[1];
            }
          }
        }
        case parameter -> {
          switch (audit.action()) {
            case create, update -> {
              AbstractParam param = castResult(result, AbstractParam.class);
              ref = param.getName();
              label = Optional.of(param.getLabel());
            }
            case delete -> {
              ref = (String) args[0];
            }
          }
        }
      }
      upsertLog(audit.scope(), audit.action(), ref, label, parent, data, token);
    } catch (ClassCastException ex) {
      LOGGER.error(
          "AuditInterceptor - Failed to cast result to expected type for scope: {}. Error: {}",
          audit.scope().name(),
          ex.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T castResult(Object result, Class<T> clazz) {
    if (clazz.isInstance(result)) {
      return (T) result;
    } else {
      throw new ClassCastException("Cannot cast result to " + clazz.getName());
    }
  }

  /*
   * Upserts an AuditEntity
   */
  private void upsertLog(
      AuditResource scope,
      AuditAction type,
      String selfRef,
      Optional<String> selfName,
      Optional<String> parent,
      Optional<Map<String, String>> data,
      Token token) {
    try {
      Optional<AuditEntity> auditEntity =
          auditRepository.findFirstByScopeAndSelfRef(scope, selfRef);

      // Create new Audit Event entry
      AuditEvent auditEvent = new AuditEvent(type, token);

      // Create or Update Audit Entity
      if (auditEntity.isPresent()) {
        if (data.isPresent()) {
          auditEntity.get().getData().putAll(data.get());
        }
        if (selfName.isPresent()) {
          auditEntity.get().setSelfLabel(selfName.get());
        }
        if (parent.isPresent()) {
          auditEntity.get().setParentRef(parent.get());
        }
        auditEntity.get().getEvents().add(auditEvent);
        auditRepository.save(auditEntity.get());
      } else {
        auditRepository.insert(new AuditEntity(scope, selfRef, selfName, parent, auditEvent, data));
      }
    } catch (Exception ex) {
      LOGGER.error("Unable to upsert Audit record with exception: {}.", ex.toString());
    }
  }
}
