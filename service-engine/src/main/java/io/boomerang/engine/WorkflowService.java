package io.boomerang.engine;

import io.boomerang.common.entity.WorkflowRunEntity;
import io.boomerang.common.enums.RunStatus;
import io.boomerang.common.model.*;
import io.boomerang.common.repository.*;
import io.boomerang.util.ParameterUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/*
 * Service implements the CRUD ops on a Workflow
 */
@Service
public class WorkflowService {
  private static final Logger LOGGER = LogManager.getLogger();

  private final WorkflowRepository workflowRepository;
  private final WorkflowRevisionRepository workflowRevisionRepository;
  private final WorkflowRunRepository workflowRunRepository;
  private final TaskRunRepository taskRunRepository;
  private final ActionRepository actionRepository;
  private final TaskRevisionRepository taskRevisionRepository;
  private final MongoTemplate mongoTemplate;
  private final TaskService taskService;
  private final WorkflowRunService workflowRunService;

  public WorkflowService(
      WorkflowRepository workflowRepository,
      WorkflowRevisionRepository workflowRevisionRepository,
      WorkflowRunRepository workflowRunRepository,
      TaskRunRepository taskRunRepository,
      ActionRepository actionRepository,
      TaskRevisionRepository taskRevisionRepository,
      MongoTemplate mongoTemplate,
      TaskService taskService,
      WorkflowRunService workflowRunService) {
    this.workflowRepository = workflowRepository;
    this.workflowRevisionRepository = workflowRevisionRepository;
    this.workflowRunRepository = workflowRunRepository;
    this.taskRunRepository = taskRunRepository;
    this.actionRepository = actionRepository;
    this.taskRevisionRepository = taskRevisionRepository;
    this.mongoTemplate = mongoTemplate;
    this.taskService = taskService;
    this.workflowRunService = workflowRunService;
  }

  /*
   * Queues the Workflow to be executed (and optionally starts the execution)
   *
   * Trigger will be set to 'Engine' if empty
   */
  public WorkflowRun submit(String workflowId, WorkflowSubmitRequest request, boolean start) {
    final WorkflowRunEntity wfRunEntity = new WorkflowRunEntity();
    //    wfRunEntity.setWorkflowRevisionRef(wfRevision.getId());
    wfRunEntity.setWorkflowRef(workflowId);
    wfRunEntity.setWorkflowVersion(request.getWorkflowVersion());
    wfRunEntity.setCreationDate(new Date());
    wfRunEntity.setStatus(RunStatus.notstarted);
    wfRunEntity.setLabels(request.getLabels());
    if (!Objects.isNull(request.getDebug())) {
      wfRunEntity.setDebug(request.getDebug());
    }
    if (!Objects.isNull(request.getTimeout()) && request.getTimeout() != 0) {
      wfRunEntity.setTimeout(request.getTimeout());
    }
    if (!Objects.isNull(request.getRetries()) && request.getRetries() != 0) {
      wfRunEntity.setRetries(request.getRetries());
    }
    if (request.getLabels() != null && !request.getLabels().isEmpty()) {
      wfRunEntity.getLabels().putAll(request.getLabels());
    }
    if (request.getAnnotations() != null && !request.getAnnotations().isEmpty()) {
      wfRunEntity.getAnnotations().putAll(request.getAnnotations());
    }
    if (request.getParams() != null && !request.getParams().isEmpty()) {
      wfRunEntity.setParams(
          ParameterUtil.addUniqueParams(wfRunEntity.getParams(), request.getParams()));
    }
    if (request.getWorkspaces() != null && !request.getWorkspaces().isEmpty()) {
      wfRunEntity.getWorkspaces().addAll(request.getWorkspaces());
    }
    // Set Trigger
    if (Objects.isNull(request.getTrigger())) {
      wfRunEntity.setTrigger("engine");
    } else {
      wfRunEntity.setTrigger(request.getTrigger().getTrigger());
    }
    // Add System Generated Annotations
    Map<String, Object> annotations = new HashMap<>();
    annotations.put("boomerang.io/generation", "4");
    annotations.put("boomerang.io/kind", "WorkflowRun");
    if (start) {
      // Add annotation to know this was created with ?start=true
      wfRunEntity.getAnnotations().put("boomerang.io/submit-with-start", "true");
    }
    wfRunEntity.getAnnotations().putAll(annotations);
    return workflowRunService.run(wfRunEntity, start);
  }
}
