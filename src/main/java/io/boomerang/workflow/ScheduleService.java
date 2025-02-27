package io.boomerang.workflow;

import io.boomerang.client.EngineClient;
import io.boomerang.core.RelationshipService;
import io.boomerang.core.model.RelationshipType;
import io.boomerang.error.BoomerangError;
import io.boomerang.error.BoomerangException;
import io.boomerang.quartz.QuartzSchedulerService;
import io.boomerang.workflow.entity.WorkflowScheduleEntity;
import io.boomerang.workflow.model.WorkflowSchedule;
import io.boomerang.workflow.model.WorkflowScheduleCalendar;
import io.boomerang.workflow.model.WorkflowScheduleStatus;
import io.boomerang.workflow.model.WorkflowScheduleType;
import io.boomerang.workflow.model.ref.Workflow;
import io.boomerang.workflow.repository.WorkflowScheduleRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

/*
 * Workflow Schedule Service provides all the methods for both the Schedules page and the individual Workflow Schedule
 * and abstracts the quartz implementation.
 *
 * @since Flow 3.6.0
 */
@Service
public class ScheduleService {

  private final Logger logger = LogManager.getLogger(getClass());

  private final QuartzSchedulerService taskScheduler;
  private final WorkflowScheduleRepository scheduleRepository;
  private final WorkflowService workflowService;
  private final RelationshipService relationshipService;
  private final EngineClient engineClient;
  private final MongoTemplate mongoTemplate;

  public ScheduleService(
      QuartzSchedulerService taskScheduler,
      WorkflowScheduleRepository scheduleRepository,
      WorkflowService workflowService,
      RelationshipService relationshipService,
      EngineClient engineClient,
      MongoTemplate mongoTemplate) {
    this.taskScheduler = taskScheduler;
    this.scheduleRepository = scheduleRepository;
    this.workflowService = workflowService;
    this.relationshipService = relationshipService;
    this.engineClient = engineClient;
    this.mongoTemplate = mongoTemplate;
  }

  /*
   * Retrieves a specific schedule
   *
   * @return a single Workflow Schedule
   */
  public WorkflowSchedule get(String team, String scheduleId) {
    final Optional<WorkflowScheduleEntity> scheduleEntity = scheduleRepository.findById(scheduleId);
    if (scheduleEntity.isPresent()
        && scheduleEntity.get().getWorkflowRef() != null
        && relationshipService.hasNodes(
            RelationshipType.TEAM,
            team,
            RelationshipType.WORKFLOW,
            Optional.of(List.of(scheduleEntity.get().getWorkflowRef())),
            Optional.empty(),
            Optional.empty())) {
      return convertScheduleEntityToModel(scheduleEntity.get());
    }
    throw new BoomerangException(BoomerangError.SCHEDULE_INVALID_REF);
  }

  /*
   * Internal Get
   *
   * Used by ExecuteScheduleJob
   */
  public WorkflowSchedule internalGet(String scheduleId) {
    final Optional<WorkflowScheduleEntity> scheduleEntity = scheduleRepository.findById(scheduleId);
    if (scheduleEntity.isPresent()) {
      return convertScheduleEntityToModel(scheduleEntity.get());
    }
    throw new BoomerangException(BoomerangError.SCHEDULE_INVALID_REF);
  }

  /*
   * Provides an all encompassing schedule retrieval method with optional filters. Ignores deleted schedules.
   *
   * @return list of Workflow Schedules
   */
  public Page<WorkflowSchedule> query(
      String queryTeam,
      int page,
      int limit,
      Sort sort,
      Optional<List<String>> queryStatus,
      Optional<List<String>> queryTypes,
      Optional<List<String>> queryWorkflows) {
    List<String> refs =
        relationshipService.filter(
            RelationshipType.WORKFLOW,
            queryWorkflows,
            Optional.of(RelationshipType.TEAM),
            Optional.of(List.of(queryTeam)));
    if (!refs.isEmpty()) {
      List<Criteria> criteriaList = new ArrayList<>();
      Criteria criteria = Criteria.where("workflowRef").in(refs);
      criteriaList.add(criteria);

      if (queryStatus.isPresent()) {
        if (queryStatus.get().stream()
            .allMatch(q -> EnumUtils.isValidEnumIgnoreCase(WorkflowScheduleStatus.class, q))) {
          Criteria statusCriteria = Criteria.where("status").in(queryStatus.get());
          criteriaList.add(statusCriteria);
        } else {
          throw new BoomerangException(BoomerangError.QUERY_INVALID_FILTERS, "status");
        }
      }

      if (queryTypes.isPresent()) {
        Criteria queryCriteria = Criteria.where("type").in(queryTypes.get());
        criteriaList.add(queryCriteria);
      }

      Criteria[] criteriaArray = criteriaList.toArray(new Criteria[criteriaList.size()]);
      Criteria allCriteria = new Criteria();
      if (criteriaArray.length > 0) {
        allCriteria.andOperator(criteriaArray);
      }
      Query query = new Query(allCriteria);
      final Pageable pageable = PageRequest.of(page, limit, sort);
      query.with(pageable);

      List<WorkflowScheduleEntity> scheduleEntities =
          mongoTemplate.find(query.with(pageable), WorkflowScheduleEntity.class);

      List<WorkflowSchedule> workflowSchedules = new LinkedList<>();
      scheduleEntities.forEach(
          e -> {
            workflowSchedules.add(convertScheduleEntityToModel(e));
          });

      Page<WorkflowSchedule> pages =
          PageableExecutionUtils.getPage(
              workflowSchedules,
              pageable,
              () -> mongoTemplate.count(query, WorkflowScheduleEntity.class));
      return pages;
    }
    throw new BoomerangException(BoomerangError.SCHEDULE_INVALID_REF);
  }

  /*
   * Create a schedule based on the payload which includes the Workflow Id.
   *
   * @return echos the created schedule
   */
  public WorkflowSchedule create(String team, final WorkflowSchedule schedule) {
    if (schedule != null
        && schedule.getWorkflowRef() != null
        && relationshipService.hasNodes(
            RelationshipType.TEAM,
            team,
            RelationshipType.WORKFLOW,
            Optional.of(List.of(schedule.getWorkflowRef())),
            Optional.empty(),
            Optional.empty())) {
      WorkflowScheduleEntity scheduleEntity = internalCreate(team, schedule);
      return convertScheduleEntityToModel(scheduleEntity);
    }
    throw new BoomerangException(BoomerangError.SCHEDULE_INVALID_REF);
  }

  public WorkflowScheduleEntity internalCreate(final String team, final WorkflowSchedule schedule) {
    // Validate required fields are present
    if ((WorkflowScheduleType.runOnce.equals(schedule.getType())
            && schedule.getDateSchedule() == null)
        || (!WorkflowScheduleType.runOnce.equals(schedule.getType())
            && schedule.getCronSchedule() == null)
        || schedule.getTimezone() == null
        || schedule.getTimezone().isBlank()) {
      // TODO: better accurate error
      throw new BoomerangException(BoomerangError.SCHEDULE_INVALID_REQ);
    }
    Workflow workflow =
        engineClient.getWorkflow(schedule.getWorkflowRef(), Optional.empty(), false);
    WorkflowScheduleEntity scheduleEntity = new WorkflowScheduleEntity();
    BeanUtils.copyProperties(schedule, scheduleEntity);
    Boolean enableJob = false;
    if (WorkflowScheduleStatus.active.equals(scheduleEntity.getStatus())
        && workflow != null
        && workflow.getTriggers().getSchedule().getEnabled()) {
      enableJob = true;
    } else if (WorkflowScheduleStatus.active.equals(scheduleEntity.getStatus())
        && workflow != null
        && !workflow.getTriggers().getSchedule().getEnabled()) {
      scheduleEntity.setStatus(WorkflowScheduleStatus.trigger_disabled);
    }
    scheduleRepository.save(scheduleEntity);
    createOrUpdateSchedule(team, scheduleEntity, enableJob);
    return scheduleEntity;
  }

  /*
   * Helper method to convert from Entity to Model as well as adding in the next schedule date.
   *
   * @return the single returnable schedule.
   */
  private WorkflowSchedule convertScheduleEntityToModel(WorkflowScheduleEntity entity) {
    try {
      return new WorkflowSchedule(entity, this.taskScheduler.getNextTriggerDate(entity));
    } catch (Exception e) {
      // Trap exception as we still want to return the dates that we can
      logger.debug("Unable to retrieve next schedule date for {}, skipping.", entity.getId());
      return new WorkflowSchedule(entity);
    }
  }

  /*
   * Retrieves the calendar dates between a start and end date period for the schedules provided.
   *
   * @return list of Schedule Calendars
   *
   * TODO add relationship check
   */
  public List<WorkflowScheduleCalendar> calendars(
      String team, final List<String> scheduleIds, Date fromDate, Date toDate) {
    List<WorkflowScheduleCalendar> scheduleCalendars = new LinkedList<>();
    final Optional<List<WorkflowScheduleEntity>> scheduleEntities =
        scheduleRepository.findByIdInAndStatusIn(scheduleIds, getStatusesNotCompletedOrDeleted());
    if (scheduleEntities.isPresent()) {
      scheduleEntities
          .get()
          .forEach(
              e -> {
                WorkflowScheduleCalendar scheduleCalendar = new WorkflowScheduleCalendar();
                scheduleCalendar.setScheduleId(e.getId());
                scheduleCalendar.setDates(getCalendarForDates(e.getId(), fromDate, toDate));
                scheduleCalendars.add(scheduleCalendar);
              });
    }
    return scheduleCalendars;
  }

  /*
   * Retrieves the calendar dates between a start and end date period for a specific workflow
   *
   * @return list of Schedule Calendars
   */
  public List<WorkflowScheduleCalendar> getCalendarsForWorkflow(
      String team, final String workflowId, Date fromDate, Date toDate) {
    if (relationshipService.hasNodes(
        RelationshipType.TEAM,
        team,
        RelationshipType.WORKFLOW,
        Optional.of(List.of(workflowId)),
        Optional.empty(),
        Optional.empty())) {
      List<WorkflowScheduleCalendar> scheduleCalendars = new LinkedList<>();
      final Optional<List<WorkflowScheduleEntity>> scheduleEntities =
          scheduleRepository.findByWorkflowRefInAndStatusIn(
              List.of(workflowId), getStatusesNotCompletedOrDeleted());
      if (scheduleEntities.isPresent()) {
        scheduleEntities
            .get()
            .forEach(
                e -> {
                  WorkflowScheduleCalendar scheduleCalendar = new WorkflowScheduleCalendar();
                  scheduleCalendar.setScheduleId(e.getId());
                  scheduleCalendar.setDates(getCalendarForDates(e.getId(), fromDate, toDate));
                  scheduleCalendars.add(scheduleCalendar);
                });
      }
      return scheduleCalendars;
    }
    throw new BoomerangException(BoomerangError.SCHEDULE_INVALID_REF);
  }

  /*
   * Retrieves the calendar dates between a start and end date period for a specific schedule
   *
   * @return a list of dates for a single Schedule Calendar
   */
  private List<Date> getCalendarForDates(final String scheduleId, Date fromDate, Date toDate) {
    final WorkflowScheduleEntity scheduleEntity =
        scheduleRepository.findById(scheduleId).orElse(null);
    if (scheduleEntity != null) {
      try {
        return this.taskScheduler.getJobTriggerDates(scheduleEntity, fromDate, toDate);
      } catch (Exception e) {
        // Trap exception as we still want to return the dates that we can
        e.printStackTrace();
        logger.debug(
            "Unable to retrieve calendar for Schedule: {}, skipping.", scheduleEntity.getId());
      }
    }
    return new LinkedList<>();
  }

  /*
   * Update a schedule based on the payload and the Schedules Id.
   *
   * @return echos the updated schedule
   */
  public WorkflowSchedule apply(String team, final WorkflowSchedule request) {
    if (request != null
        && request.getId() != null
        && !request.getId().isBlank()
        && !request.getId().isEmpty()) {
      final Optional<WorkflowScheduleEntity> optScheduleEntity =
          scheduleRepository.findById(request.getId());
      if (optScheduleEntity.isPresent()) {
        WorkflowScheduleEntity scheduleEntity = optScheduleEntity.get();
        /*
         * The copy ignores ID and creationDate to ensure data integrity
         */
        WorkflowScheduleStatus previousStatus = scheduleEntity.getStatus();
        BeanUtils.copyProperties(request, scheduleEntity, "id", "creationDate");

        /*
         * Complex Status checking to determine what can and can't be enabled
         */
        // Check if job is trying to be enabled with date in the past
        WorkflowScheduleStatus newStatus = scheduleEntity.getStatus();
        Workflow workflow =
            workflowService.get(team, scheduleEntity.getWorkflowRef(), Optional.empty(), false);
        Boolean enableJob = true;
        if (!previousStatus.equals(newStatus)) {
          if (WorkflowScheduleStatus.active.equals(previousStatus)
              && WorkflowScheduleStatus.inactive.equals(newStatus)) {
            scheduleEntity.setStatus(WorkflowScheduleStatus.inactive);
            enableJob = false;
          } else if (WorkflowScheduleStatus.inactive.equals(previousStatus)
              && WorkflowScheduleStatus.active.equals(newStatus)) {
            if (workflow != null && !workflow.getTriggers().getSchedule().getEnabled()) {
              scheduleEntity.setStatus(WorkflowScheduleStatus.trigger_disabled);
              enableJob = false;
            }
            if (WorkflowScheduleType.runOnce.equals(scheduleEntity.getType())) {
              Date currentDate = new Date();
              if (scheduleEntity.getDateSchedule().getTime() < currentDate.getTime()) {
                logger.error(
                    "Cannot enable schedule ("
                        + scheduleEntity.getId()
                        + ") as it is in the past.");
                scheduleEntity.setStatus(WorkflowScheduleStatus.error);
                scheduleRepository.save(scheduleEntity);
                try {
                  this.taskScheduler.cancelJob(scheduleEntity);
                } catch (SchedulerException e) {
                  e.printStackTrace();
                }
                return new WorkflowSchedule(scheduleEntity);
              }
            }
          }

        } else {
          if (WorkflowScheduleStatus.inactive.equals(newStatus)) {
            enableJob = false;
          }
        }
        scheduleRepository.save(scheduleEntity);
        createOrUpdateSchedule(team, scheduleEntity, enableJob);
        return convertScheduleEntityToModel(scheduleEntity);
      }
    } else if (request != null) {
      request.setId(null);
      return this.create(team, request);
    }
    throw new BoomerangException(BoomerangError.SCHEDULE_INVALID_REF);
  }

  /*
   * Helper method to determine if we are updating a cron or runonce schedule. It also handles
   * pausing a schedule if the status is set to pause.
   */
  private void createOrUpdateSchedule(
      final String team, final WorkflowScheduleEntity schedule, Boolean enableJob) {
    try {
      if (WorkflowScheduleType.runOnce.equals(schedule.getType())) {
        this.taskScheduler.createOrUpdateRunOnceJob(team, schedule);
      } else {
        this.taskScheduler.createOrUpdateCronJob(team, schedule);
      }
      if (!enableJob) {
        this.taskScheduler.pauseJob(schedule);
      }
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * Enables all schedules that have been disabled by the trigger being disabled. This is needed to
   * differentiate between user paused and trigger disabled schedules.
   */
  protected void enableAllTriggerSchedules(final String workflowId) {
    final Optional<List<WorkflowScheduleEntity>> entities =
        scheduleRepository.findByWorkflowRefInAndStatusIn(
            List.of(workflowId), List.of(WorkflowScheduleStatus.trigger_disabled));
    if (entities.isPresent()) {
      entities
          .get()
          .forEach(
              s -> {
                try {
                  enableSchedule(s.getId());
                } catch (SchedulerException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              });
    }
  }

  /*
   * Enables a specific schedule
   */
  private void enableSchedule(String scheduleId) throws SchedulerException {
    Optional<WorkflowScheduleEntity> optSchedule = scheduleRepository.findById(scheduleId);
    if (optSchedule.isPresent()) {
      WorkflowScheduleEntity schedule = optSchedule.get();
      if (WorkflowScheduleType.runOnce.equals(schedule.getType())) {
        Date currentDate = new Date();
        logger.info("Current DateTime: ", currentDate.getTime());
        logger.info("Schedule DateTime: ", schedule.getDateSchedule().getTime());
        if (schedule.getDateSchedule().getTime() < currentDate.getTime()) {
          logger.info("Cannot enable schedule (" + schedule.getId() + ") as it is in the past.");
          schedule.setStatus(WorkflowScheduleStatus.error);
          scheduleRepository.save(schedule);
        } else {
          schedule.setStatus(WorkflowScheduleStatus.active);
          scheduleRepository.save(schedule);
          this.taskScheduler.resumeJob(schedule);
        }
      } else {
        schedule.setStatus(WorkflowScheduleStatus.active);
        scheduleRepository.save(schedule);
        this.taskScheduler.resumeJob(schedule);
      }
    }
  }

  /*
   * Disables all schedules that are currently active and is used when the trigger is disabled.
   */
  protected void disableAllTriggerSchedules(final String workflowId) {
    final Optional<List<WorkflowScheduleEntity>> entities =
        scheduleRepository.findByWorkflowRefInAndStatusIn(
            List.of(workflowId), List.of(WorkflowScheduleStatus.active));
    if (entities.isPresent()) {
      entities
          .get()
          .forEach(
              s -> {
                try {
                  s.setStatus(WorkflowScheduleStatus.trigger_disabled);
                  scheduleRepository.save(s);
                  this.taskScheduler.pauseJob(s);
                } catch (SchedulerException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              });
    }
  }

  /*
   * Complete a specific schedule
   *
   * Used by ExecuteScheduleJob
   */
  public void complete(String scheduleId) {
    Optional<WorkflowScheduleEntity> schedule = scheduleRepository.findById(scheduleId);
    if (schedule.isPresent()) {
      schedule.get().setStatus(WorkflowScheduleStatus.completed);
      scheduleRepository.save(schedule.get());
    }
  }

  /*
   * Mark all schedules as deleted and cancel the quartz jobs. This is used when a workflow is deleted.
   */
  protected void deleteAllForWorkflow(final String workflowId) {
    final Optional<List<WorkflowScheduleEntity>> entities =
        scheduleRepository.findByWorkflowRef(workflowId);
    if (entities.isPresent()) {
      entities
          .get()
          .forEach(
              s -> {
                this.internalDelete(s);
              });
    }
  }

  /*
   * Mark a single schedule as deleted and cancel the quartz jobs. Used by the UI when deleting a schedule.
   */
  public void delete(String team, final String scheduleId) {
    final Optional<WorkflowScheduleEntity> schedule = scheduleRepository.findById(scheduleId);
    if (schedule.isPresent()
        && relationshipService.hasNodes(
            RelationshipType.TEAM,
            team,
            RelationshipType.WORKFLOW,
            Optional.of(List.of(schedule.get().getWorkflowRef())),
            Optional.empty(),
            Optional.empty())) {
      this.internalDelete(schedule.get());
    }
    throw new BoomerangException(BoomerangError.SCHEDULE_INVALID_REF);
  }

  private void internalDelete(WorkflowScheduleEntity entity) {
    try {
      this.taskScheduler.cancelJob(entity);
      scheduleRepository.deleteById(entity.getId());
    } catch (SchedulerException e) {
      logger.info("Unable to delete schedule: {}.", entity.getId());
      logger.error(e);
    }
  }

  private List<WorkflowScheduleStatus> getStatusesNotCompletedOrDeleted() {
    List<WorkflowScheduleStatus> statuses = new LinkedList<>();
    statuses.add(WorkflowScheduleStatus.active);
    statuses.add(WorkflowScheduleStatus.inactive);
    statuses.add(WorkflowScheduleStatus.trigger_disabled);
    statuses.add(WorkflowScheduleStatus.error);
    return statuses;
  }
}
