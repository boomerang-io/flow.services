package io.boomerang.quartz;

import io.boomerang.core.RelationshipService;
import io.boomerang.core.TokenService;
import io.boomerang.core.enums.RelationshipLabel;
import io.boomerang.core.enums.RelationshipType;
import io.boomerang.core.model.Token;
import io.boomerang.workflow.ScheduleService;
import io.boomerang.workflow.WorkflowService;
import io.boomerang.workflow.model.TriggerEnum;
import io.boomerang.workflow.model.WorkflowSchedule;
import io.boomerang.workflow.model.WorkflowScheduleType;
import io.boomerang.workflow.model.ref.WorkflowSubmitRequest;
import java.util.ArrayList;
import java.util.List;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/*
 * This is used by the Quartz Trigger to execute the Scheduled Job
 *
 * Caution: if this is renamed or moved packages then all the jobs in the DB will need to have the
 * jobClass reference updated.
 */
@PersistJobDataAfterExecution
public class QuartzSchedulerJob extends QuartzJobBean {

  private static final Logger logger = LoggerFactory.getLogger(QuartzSchedulerJob.class);

  private ApplicationContext applicationContext;

  /**
   * This method is called by Spring since we set the {@link
   * SchedulerFactoryBean#setApplicationContextSchedulerContextKey(String)} in {@link
   * QuartzConfiguration}
   *
   * @param applicationContext
   */
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /** This is the method that will be executed each time the trigger is fired. */
  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    JobDetail jobDetail = context.getJobDetail();
    logger.info(
        "This is the Quartz Execute Job, executed for {} with JobDetails = {}",
        jobDetail.getKey().getName(),
        jobDetail.getJobDataMap());

    if (applicationContext == null) {
      logger.info("applicationContext is null");
    }

    WorkflowService workflowService = applicationContext.getBean(WorkflowService.class);
    ScheduleService workflowScheduleService = applicationContext.getBean(ScheduleService.class);
    TokenService tokenService = applicationContext.getBean(TokenService.class);
    RelationshipService relationshipService = applicationContext.getBean(RelationshipService.class);

    WorkflowSchedule schedule = workflowScheduleService.internalGet(jobDetail.getKey().getName());
    if (schedule != null) {
      if (schedule.getType().equals(WorkflowScheduleType.runOnce)) {
        logger.info("Executing runOnce schedule: {}, and marking as completed.", schedule.getId());
        workflowScheduleService.complete(schedule.getId());
      }

      WorkflowSubmitRequest request = new WorkflowSubmitRequest();
      request.setLabels(schedule.getLabels());
      request.setParams(request.getParams());
      request.setTrigger(TriggerEnum.schedule);

      // Hoist token to ThreadLocal SecurityContext - this AuthN/AuthZ allows the WorkflowRun to be
      // triggered
      Token token = tokenService.createWorkflowSessionToken(jobDetail.getKey().getGroup());
      final List<GrantedAuthority> authorities = new ArrayList<>();
      final UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(jobDetail.getKey().getGroup(), null, authorities);
      authToken.setDetails(token);
      SecurityContextHolder.getContext().setAuthentication(authToken);

      // Auto start is not needed when using the default handler
      // As the default handler will pick up the queued Workflow and start the Workflow when ready.
      // However if using the non-default Handler then this may be needed to be set to true.
      boolean autoStart =
          applicationContext
              .getEnvironment()
              .getProperty("flow.workflowrun.auto-start-on-submit", boolean.class);
      String team = jobDetail.getDescription();
      if (team == null || team.isEmpty()) {
        // Attempt to get the team from Workflow relationship.
        // Internal create for Workflows that create schedules via Engine where team won't be set.
        team =
            relationshipService.getParentByLabel(
                RelationshipLabel.HAS_WORKFLOW,
                RelationshipType.WORKFLOW,
                jobDetail.getKey().getGroup());
      }
      workflowService.submit(team, jobDetail.getKey().getGroup(), request, autoStart);
    }
  }
}
