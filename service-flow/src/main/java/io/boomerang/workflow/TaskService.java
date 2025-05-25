package io.boomerang.workflow;

import io.boomerang.client.EngineClient;
import io.boomerang.common.entity.TaskEntity;
import io.boomerang.common.entity.TaskRevisionEntity;
import io.boomerang.common.enums.TaskStatus;
import io.boomerang.common.model.ChangeLog;
import io.boomerang.common.model.ChangeLogVersion;
import io.boomerang.common.model.Task;
import io.boomerang.common.model.WorkflowTask;
import io.boomerang.common.repository.TaskRepository;
import io.boomerang.common.repository.TaskRevisionRepository;
import io.boomerang.core.RelationshipService;
import io.boomerang.core.UserService;
import io.boomerang.core.enums.RelationshipLabel;
import io.boomerang.core.enums.RelationshipType;
import io.boomerang.core.model.User;
import io.boomerang.error.BoomerangError;
import io.boomerang.error.BoomerangException;
import io.boomerang.security.IdentityService;
import io.boomerang.workflow.tekton.TektonConverter;
import io.boomerang.workflow.tekton.TektonTask;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

/*
 * Tasks are stored in a main TaskEntity with fields that have limited change scope
 * and a TaskRevisionEntity that holds the versioned elements
 *
 * It utilises a @DocumentReference for the parent field that allows us to retrieve the TaskEntity from within the TaskRevisionEntity when reading
 *
 * - Checks Relationships
 * - Determines if to add or remove elements
 * - Forward call onto Engine (converts slug to ID)
 * - Converts response as needed for UI (including converting ID to Slug)
 */
@Service
public class TaskService {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final String CHANGELOG_INITIAL = "Initial Task Template";
  private static final String CHANGELOG_UPDATE = "Updated Task Template";
  private static final String NAME_REGEX = "^([0-9a-zA-Z\\-]+)$";
  private static final String ANNOTATION_GENERATION = "4";
  private static final String ANNOTATION_KIND = "Task";

  private final RelationshipService relationshipService;
  private final IdentityService identityService;
  private final UserService userService;
  private final TaskRepository taskRepository;
  private final TaskRevisionRepository taskRevisionRepository;
  private final MongoTemplate mongoTemplate;

  public TaskService(
      EngineClient engineClient,
      RelationshipService relationshipService,
      IdentityService identityService,
      UserService userService,
      TaskRepository taskRepository,
      TaskRevisionRepository taskRevisionRepository,
      MongoTemplate mongoTemplate) {
    this.relationshipService = relationshipService;
    this.identityService = identityService;
    this.userService = userService;
    this.taskRepository = taskRepository;
    this.taskRevisionRepository = taskRevisionRepository;
    this.mongoTemplate = mongoTemplate;
  }

  /*
   * Retrieve a TEAMTASK by team, name and optional version. If no version specified, will retrieve the latest.
   */
  public Task get(String team, String name, Optional<Integer> version) {
    // Checks principal and provided Task has relationship to Team.
    if (!Objects.isNull(name) && !name.isBlank()) {
      List<String> taskRefs =
          relationshipService.filter(
              RelationshipType.TEAMTASK,
              Optional.of(List.of(name)),
              Optional.of(RelationshipType.TEAM),
              Optional.of(List.of(team)),
              false);
      if (!taskRefs.isEmpty()) {
        // Assumes there is only one task of that slug in a team
        return internalGet(taskRefs.get(0), version);
      }
    }
    throw new BoomerangException(
        BoomerangError.TASK_INVALID_REF, name, version.isPresent() ? version.get() : "latest");
  }

  /*
   * Retrieve a TASK by name and optional version. If no version specified, will retrieve the latest.
   */
  public Task get(String name, Optional<Integer> version) {
    if (!Objects.isNull(name) && !name.isBlank()) {
      List<String> taskRefs =
          relationshipService.filter(
              RelationshipType.TASK,
              Optional.of(List.of(name)),
              Optional.empty(),
              Optional.empty(),
              false);
      if (!taskRefs.isEmpty()) {
        // Assumes there is only one task of that slug in a team
        return internalGet(taskRefs.get(0), version);
      }
    }
    throw new BoomerangException(
        BoomerangError.TASK_INVALID_REF, name, version.isPresent() ? version.get() : "latest");
  }

  private Task internalGet(String ref, Optional<Integer> version) {
    Optional<TaskEntity> taskEntity = taskRepository.findById(ref);
    if (taskEntity.isPresent()) {
      Optional<TaskRevisionEntity> taskRevisionEntity;
      if (version.isPresent()) {
        taskRevisionEntity =
            taskRevisionRepository.findByParentRefAndVersion(
                taskEntity.get().getId(), version.get());
      } else {
        taskRevisionEntity =
            taskRevisionRepository.findByParentRefAndLatestVersion(taskEntity.get().getId());
      }
      if (taskRevisionEntity.isPresent()) {
        return convertEntityToModel(taskEntity.get(), taskRevisionEntity.get());
      }
    }
    throw new BoomerangException(
        BoomerangError.TASK_INVALID_REF, ref, version.isPresent() ? version.get() : "latest");
  }

  /*
   * Query for TEAMTASKS.
   */
  public Page<Task> query(
      String queryTeam,
      Optional<Integer> queryLimit,
      Optional<Integer> queryPage,
      Optional<Direction> querySort,
      Optional<List<String>> queryLabels,
      Optional<List<String>> queryStatus,
      Optional<List<String>> queryNames) {

    // Check for relationship
    List<String> refs =
        relationshipService.filter(
            RelationshipType.TEAMTASK,
            queryNames,
            Optional.of(RelationshipType.TEAM),
            Optional.of(List.of(queryTeam)),
            false);
    LOGGER.debug("Task Refs: {}", refs.toString());
    if (refs == null || refs.size() == 0) {
      return Page.empty();
    }
    return internalQuery(queryLimit, queryPage, querySort, queryLabels, queryStatus, refs);
  }

  /*
   * Query for TASKS.
   */
  public Page<Task> query(
      Optional<Integer> queryLimit,
      Optional<Integer> queryPage,
      Optional<Direction> querySort,
      Optional<List<String>> queryLabels,
      Optional<List<String>> queryStatus,
      Optional<List<String>> queryNames) {

    List<String> refs =
        relationshipService.filter(
            RelationshipType.TASK, queryNames, Optional.empty(), Optional.empty(), false);
    LOGGER.debug("Global Task Refs: {}", refs.toString());
    if (refs == null || refs.size() == 0) {
      return Page.empty();
    }
    return internalQuery(queryLimit, queryPage, querySort, queryLabels, queryStatus, refs);
  }

  private Page<Task> internalQuery(
      Optional<Integer> queryLimit,
      Optional<Integer> queryPage,
      Optional<Direction> querySort,
      Optional<List<String>> queryLabels,
      Optional<List<String>> queryStatus,
      List<String> queryRefs) {
    Pageable pageable = Pageable.unpaged();
    final Sort sort = Sort.by(new Sort.Order(querySort.orElse(Direction.ASC), "creationDate"));
    if (queryLimit.isPresent()) {
      pageable = PageRequest.of(queryPage.get(), queryLimit.get(), sort);
    }
    List<Criteria> criteriaList = new ArrayList<>();

    if (queryLabels.isPresent()) {
      queryLabels.get().stream()
          .forEach(
              l -> {
                String decodedLabel = "";
                try {
                  decodedLabel = URLDecoder.decode(l, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                  throw new BoomerangException(e, BoomerangError.QUERY_INVALID_FILTERS, "labels");
                }
                LOGGER.debug(decodedLabel.toString());
                String[] label = decodedLabel.split("[=]+");
                Criteria labelsCriteria =
                    Criteria.where("labels." + label[0].replace(".", "#")).is(label[1]);
                criteriaList.add(labelsCriteria);
              });
    }

    if (queryStatus.isPresent()) {
      if (queryStatus.get().stream()
          .allMatch(q -> EnumUtils.isValidEnumIgnoreCase(TaskStatus.class, q))) {
        Criteria criteria = Criteria.where("status").in(queryStatus.get());
        criteriaList.add(criteria);
      } else {
        throw new BoomerangException(BoomerangError.QUERY_INVALID_FILTERS, "status");
      }
    }

    List<ObjectId> queryIds = queryRefs.stream().map(ObjectId::new).collect(Collectors.toList());
    Criteria criteria = Criteria.where("_id").in(queryIds);
    criteriaList.add(criteria);

    Criteria[] criteriaArray = criteriaList.toArray(new Criteria[criteriaList.size()]);
    Criteria allCriteria = new Criteria();
    if (criteriaArray.length > 0) {
      allCriteria.andOperator(criteriaArray);
    }
    Query query = new Query(allCriteria);
    if (queryLimit.isPresent()) {
      query.with(pageable);
    } else {
      query.with(sort);
    }

    List<TaskEntity> taskEntities = mongoTemplate.find(query.with(pageable), TaskEntity.class);

    List<Task> tasks = new LinkedList<>();
    taskEntities.forEach(
        e -> {
          LOGGER.debug(e.toString());
          Optional<TaskRevisionEntity> taskRevisionEntity =
              taskRevisionRepository.findByParentRefAndLatestVersion(e.getId());
          if (taskRevisionEntity.isPresent()) {
            tasks.add(convertEntityToModel(e, taskRevisionEntity.get()));
          }
        });

    Page<Task> page = PageableExecutionUtils.getPage(tasks, pageable, () -> tasks.size());
    return page;
  }

  /*
   * Creates the Task and Relationship
   */
  public Task create(String team, Task request) {
    // Validate Access
    if (!relationshipService.check(
        RelationshipType.TEAM, team, Optional.empty(), Optional.empty())) {
      throw new BoomerangException(BoomerangError.PERMISSION_DENIED);
    }

    // Check name matches the requirements
    if (request.getName().isBlank() || !request.getName().matches(NAME_REGEX)) {
      throw new BoomerangException(BoomerangError.TASK_INVALID_NAME, request.getName());
    }

    // Check Slugs for Tasks in team
    if (relationshipService.check(
        RelationshipType.TEAMTASK,
        request.getName(),
        Optional.of(RelationshipType.TEAM),
        Optional.of(List.of(team)))) {
      throw new BoomerangException(BoomerangError.TASK_ALREADY_EXISTS, request.getName());
    }

    // Create Task
    Task task = internalCreate(request);

    // Create Relationship
    relationshipService.createNodeAndEdge(
        RelationshipType.TEAM,
        team,
        RelationshipLabel.HAS_TASK,
        RelationshipType.TEAMTASK,
        task.getId(),
        task.getName(),
        Optional.empty(),
        Optional.empty());

    // Remove ID
    task.setId(null);
    return task;
  }

  public Task create(Task request) {
    // Check name matches the requirements
    if (request.getName().isBlank() || !request.getName().matches(NAME_REGEX)) {
      throw new BoomerangException(BoomerangError.TASK_INVALID_NAME, request.getName());
    }

    // Check Slugs for GlobalTasks
    if (relationshipService.check(
        RelationshipType.TASK, request.getName(), Optional.empty(), Optional.empty())) {
      throw new BoomerangException(BoomerangError.TASK_ALREADY_EXISTS, request.getName());
    }

    // Create Task
    Task task = internalCreate(request);

    // Create Relationship
    relationshipService.createNodeAndEdge(
        RelationshipType.ROOT,
        "root",
        RelationshipLabel.HAS_TASK,
        RelationshipType.TASK,
        task.getId(),
        task.getName(),
        Optional.empty(),
        Optional.empty());

    // Remove ID
    task.setId(null);
    return task;
  }

  private Task internalCreate(Task request) {
    // Ignore any provided Ids as this is a create
    request.setId(null);
    // Set verified to false - this is only able to be set via Loader - TODO: allow admins to set it
    request.setVerified(false);

    // Update Changelog
    updateChangeLog(request.getChangelog());

    // Set Display Name if not provided
    if (request.getDisplayName() == null || request.getDisplayName().isBlank()) {
      request.setDisplayName(request.getName());
    }

    // Set System Generated Annotations
    request.getAnnotations().put("boomerang.io/generation", ANNOTATION_GENERATION);
    request.getAnnotations().put("boomerang.io/kind", ANNOTATION_KIND);

    // Set as initial version
    request.setVersion(1);

    // Set Changelog
    request.setChangelog(updateChangeLog(new ChangeLog(CHANGELOG_INITIAL)));

    // Save
    TaskEntity taskEntity = new TaskEntity(request);
    TaskRevisionEntity taskRevisionEntity = new TaskRevisionEntity(request);
    taskEntity = taskRepository.save(taskEntity);
    taskRevisionEntity.setParentRef(taskEntity.getId());
    taskRevisionRepository.save(taskRevisionEntity);

    return convertEntityToModel(taskEntity, taskRevisionEntity);
  }

  /*
   * Apply allows you to create a new version as well as create new
   *
   * Names are akin to a slug and are immutable. If the name changes, a new TaskTemplate is created
   *
   */
  public Task apply(String name, String team, Task request, boolean replace) {
    if (name.isBlank() || !name.matches(NAME_REGEX)) {
      throw new BoomerangException(BoomerangError.TASK_INVALID_NAME, request.getName());
    }

    List<String> refs =
        relationshipService.filter(
            RelationshipType.TEAMTASK,
            Optional.of(List.of(name)),
            Optional.of(RelationshipType.TEAM),
            Optional.of(List.of(team)),
            false);
    if (!refs.isEmpty()) {
      request.setId(refs.get(0));
      // Name is immutable
      request.setName(name);
      Task task = this.internalApply(request, replace);

      // Remove ID
      task.setId(null);
      return task;
    } else {
      return this.create(team, request);
    }
  }

  public Task apply(String name, Task request, boolean replace) {
    LOGGER.debug("Applying Task: {}", request.toString());
    if (name.isBlank() || !name.matches(NAME_REGEX)) {
      throw new BoomerangException(BoomerangError.TASK_INVALID_NAME, request.getName());
    }
    List<String> refs =
        relationshipService.filter(
            RelationshipType.TASK,
            Optional.of(List.of(name)),
            Optional.empty(),
            Optional.empty(),
            false);
    if (!refs.isEmpty()) {
      request.setId(refs.get(0));
      request.setName(name); // name is immutable
      Task task = this.internalApply(request, replace);

      // Remove ID
      task.setId(null);
      return task;
    } else {
      return this.create(request);
    }
  }

  // TODO set verfieid to whatever is in databsed
  private Task internalApply(Task request, boolean replace) {
    // Retrieve Task
    Optional<TaskEntity> taskOpt = taskRepository.findById(request.getId());
    if (taskOpt.isEmpty()) {
      return this.create(request);
    }
    TaskEntity taskEntity = taskOpt.get();

    // Check for active status
    if (TaskStatus.inactive.equals(taskEntity.getStatus())
        && !TaskStatus.active.equals(request.getStatus())) {
      throw new BoomerangException(
          BoomerangError.TASK_INACTIVE_STATUS, request.getName(), "latest");
    }

    // Get latest revision
    Optional<TaskRevisionEntity> taskRevisionEntity =
        taskRevisionRepository.findByParentRefAndLatestVersion(request.getId());
    if (taskRevisionEntity.isEmpty()) {
      throw new BoomerangException(BoomerangError.TASK_INVALID_REF, request.getName(), "latest");
    }

    // Update TaskTemplateEntity
    // Set System Generated Annotations
    // Name (slug), Type, Creation Date, and Verified cannot be updated
    if (request.getStatus() != null) {
      taskEntity.setStatus(request.getStatus());
    }
    if (!request.getAnnotations().isEmpty()) {
      taskEntity.getAnnotations().putAll(request.getAnnotations());
    }
    taskEntity.getAnnotations().put("boomerang.io/generation", ANNOTATION_GENERATION);
    taskEntity.getAnnotations().put("boomerang.io/kind", ANNOTATION_KIND);
    if (!request.getLabels().isEmpty()) {
      taskEntity.getLabels().putAll(request.getLabels());
    }

    // Create / Replace TaskRevisionEntity
    TaskRevisionEntity newTaskRevisionEntity = new TaskRevisionEntity(request);
    if (replace) {
      newTaskRevisionEntity.setId(taskRevisionEntity.get().getId());
      newTaskRevisionEntity.setVersion(taskRevisionEntity.get().getVersion());
    } else {
      newTaskRevisionEntity.setVersion(taskRevisionEntity.get().getVersion() + 1);
    }

    // Set Display Name if not provided
    if (newTaskRevisionEntity.getDisplayName() == null
        || newTaskRevisionEntity.getDisplayName().isBlank()) {
      newTaskRevisionEntity.setDisplayName(request.getName());
    }

    // Update changelog
    ChangeLog changelog =
        new ChangeLog(
            taskRevisionEntity.get().getVersion().equals(1) ? CHANGELOG_INITIAL : CHANGELOG_UPDATE);
    newTaskRevisionEntity.setChangelog(updateChangeLog(changelog));

    // Save entities
    TaskEntity savedEntity = taskRepository.save(taskEntity);
    newTaskRevisionEntity.setParentRef(taskEntity.getId());
    TaskRevisionEntity savedRevision = taskRevisionRepository.save(newTaskRevisionEntity);

    return convertEntityToModel(savedEntity, savedRevision);
  }

  public TektonTask getAsTekton(String team, String name, Optional<Integer> version) {
    Task template = this.get(team, name, version);
    return TektonConverter.convertTaskTemplateToTektonTask(template);
  }

  public TektonTask getAsTekton(String name, Optional<Integer> version) {
    Task template = this.get(name, version);
    return TektonConverter.convertTaskTemplateToTektonTask(template);
  }

  public TektonTask createAsTekton(String team, TektonTask tektonTask) {
    Task template = TektonConverter.convertTektonTaskToTaskTemplate(tektonTask);
    this.create(team, template);
    return tektonTask;
  }

  public TektonTask createAsTekton(TektonTask tektonTask) {
    Task template = TektonConverter.convertTektonTaskToTaskTemplate(tektonTask);
    this.create(template);
    return tektonTask;
  }

  public TektonTask applyAsTekton(
      String name, String team, TektonTask tektonTask, boolean replace) {
    Task template = TektonConverter.convertTektonTaskToTaskTemplate(tektonTask);
    this.apply(name, team, template, replace);
    return tektonTask;
  }

  public TektonTask applyAsTekton(String name, TektonTask tektonTask, boolean replace) {
    Task template = TektonConverter.convertTektonTaskToTaskTemplate(tektonTask);
    this.apply(name, template, replace);
    return tektonTask;
  }

  public void validateAsTekton(TektonTask tektonTask) {
    TektonConverter.convertTektonTaskToTaskTemplate(tektonTask);
  }

  public List<ChangeLogVersion> changelog(String team, String name) {
    if (name.isBlank() || !name.matches(NAME_REGEX)) {
      throw new BoomerangException(BoomerangError.TASK_INVALID_NAME, name);
    }
    List<String> refs =
        relationshipService.filter(
            RelationshipType.TEAMTASK,
            Optional.of(List.of(name)),
            Optional.of(RelationshipType.TEAM),
            Optional.of(List.of(team)),
            false);
    if (!refs.isEmpty()) {
      return internalChangelog(refs.get(0));
    }
    // TODO - change error to don't have access
    throw new BoomerangException(BoomerangError.TASK_INVALID_NAME, name);
  }

  public List<ChangeLogVersion> changelog(String name) {
    List<String> refs =
        relationshipService.filter(
            RelationshipType.TASK,
            Optional.of(List.of(name)),
            Optional.empty(),
            Optional.empty(),
            false);
    if (!refs.isEmpty()) {
      return internalChangelog(refs.get(0));
    }
    // TODO - change error to don't have access
    throw new BoomerangException(BoomerangError.TASK_INVALID_NAME, name);
  }

  /*
   * Retrieve all the changelogs and return by version
   */
  private List<ChangeLogVersion> internalChangelog(String ref) {
    Task task = this.get(ref, Optional.empty());
    List<TaskRevisionEntity> taskRevisionEntities =
        taskRevisionRepository.findByParentRef(task.getId());
    if (taskRevisionEntities.isEmpty()) {
      throw new BoomerangException(BoomerangError.TASK_INVALID_REF, ref, "latest");
    }
    List<ChangeLogVersion> changelogs = new LinkedList<>();
    taskRevisionEntities.forEach(
        v -> {
          ChangeLogVersion cl = new ChangeLogVersion();
          cl.setVersion(v.getVersion());
          if (v.getChangelog() != null) {
            cl.setAuthor(v.getChangelog().getAuthor());
            cl.setReason(v.getChangelog().getReason());
            cl.setDate(v.getChangelog().getDate());
            switchChangeLogAuthorToUserName(cl);
          }
          changelogs.add(cl);
        });
    return changelogs;
  }

  /*
   * Deletes a TeamTask - team is required as you cannot delete a global template (only make
   * inactive)
   */
  public void delete(String team, String name) {
    if (Objects.isNull(name) || name.isBlank()) {
      throw new BoomerangException(BoomerangError.TASK_INVALID_REF);
    }
    List<String> refs =
        relationshipService.filter(
            RelationshipType.TEAMTASK,
            Optional.of(List.of(name)),
            Optional.of(RelationshipType.TEAM),
            Optional.of(List.of(team)),
            false);
    if (!refs.isEmpty()) {
      taskRevisionRepository.deleteByParentRef(name);
      taskRepository.deleteById(name);
    }
    // TODO - change error to don't have access
    throw new BoomerangException(BoomerangError.TASK_INVALID_NAME, name);
  }

  /**
   * Validates the task reference, version, and status
   *
   * <p>Shared with Engine Service
   *
   * @param wfTask
   * @return Task
   */
  public Task retrieveAndValidateTask(final WorkflowTask wfTask) {
    // Get TaskEntity - this will check valid ref and Version
    if (wfTask == null || wfTask.getTaskRef() == null || wfTask.getTaskRef().isEmpty()) {
      throw new BoomerangException(BoomerangError.TASK_INVALID_REF);
    }
    Optional<TaskEntity> taskEntity = taskRepository.findById(wfTask.getTaskRef());
    if (taskEntity.isPresent()) {
      // Check Task Status
      if (TaskStatus.inactive.equals(taskEntity.get().getStatus())) {
        throw new BoomerangException(
            BoomerangError.TASK_INACTIVE_STATUS, wfTask.getTaskRef(), wfTask.getTaskVersion());
      }
      // Retrieve version or latest
      Optional<TaskRevisionEntity> taskRevisionEntity;
      if (wfTask.getTaskVersion() != null && wfTask.getTaskVersion() > 0) {
        taskRevisionEntity =
            taskRevisionRepository.findByParentRefAndVersion(
                taskEntity.get().getId(), wfTask.getTaskVersion());
      } else {
        taskRevisionEntity =
            taskRevisionRepository.findByParentRefAndLatestVersion(taskEntity.get().getId());
      }
      if (taskRevisionEntity.isPresent()) {
        return convertEntityToModel(taskEntity.get(), taskRevisionEntity.get());
      }
    }
    throw new BoomerangException(
        BoomerangError.TASK_INVALID_REF,
        wfTask.getTaskRef(),
        wfTask.getTaskVersion() != null && wfTask.getTaskVersion() > 0
            ? wfTask.getTaskVersion()
            : "latest");
  }

  // Override changelog date and set author. Used on creation/update of TaskTemplate
  private ChangeLog updateChangeLog(ChangeLog changelog) {
    if (changelog == null) {
      changelog = new ChangeLog();
    }
    changelog.setDate(new Date());
    if (identityService.getCurrentIdentity().getPrincipal() != null) {
      changelog.setAuthor(identityService.getCurrentIdentity().getPrincipal());
    }
    return changelog;
  }

  // TODO - need to make more performant
  private void switchChangeLogAuthorToUserName(ChangeLog changelog) {
    if (changelog != null && changelog.getAuthor() != null) {
      Optional<User> user = userService.getUserByID(changelog.getAuthor());
      if (user.isPresent()) {
        changelog.setAuthor(
            user.get().getDisplayName().isEmpty()
                ? user.get().getName()
                : user.get().getDisplayName());
      } else {
        changelog.setAuthor("---");
      }
    }
  }

  private Task convertEntityToModel(TaskEntity entity, TaskRevisionEntity revision) {
    Task task = new Task();
    BeanUtils.copyProperties(entity, task);
    BeanUtils.copyProperties(revision, task, "id"); // want to keep the TaskEntity ID

    // Switch to names for the User in changelog
    switchChangeLogAuthorToUserName(task.getChangelog());

    // Remove ID
    // TODO do we remove IDs from the Task model and only pass the entity model to the engine
    task.setId(null);
    return task;
  }
}
