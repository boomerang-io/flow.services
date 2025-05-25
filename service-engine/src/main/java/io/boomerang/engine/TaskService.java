package io.boomerang.engine;

import io.boomerang.common.entity.TaskEntity;
import io.boomerang.common.entity.TaskRevisionEntity;
import io.boomerang.common.enums.TaskStatus;
import io.boomerang.common.model.Task;
import io.boomerang.common.model.WorkflowTask;
import io.boomerang.common.repository.TaskRepository;
import io.boomerang.common.repository.TaskRevisionRepository;
import io.boomerang.error.BoomerangError;
import io.boomerang.error.BoomerangException;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskRevisionRepository taskRevisionRepository;

  public TaskService(TaskRepository taskRepository, TaskRevisionRepository taskRevisionRepository) {
    this.taskRepository = taskRepository;
    this.taskRevisionRepository = taskRevisionRepository;
  }

  /**
   * Validates the task reference, version, and status
   *
   * <p>Shared with Workflow Service
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

  private Task convertEntityToModel(TaskEntity entity, TaskRevisionEntity revision) {
    Task task = new Task();
    BeanUtils.copyProperties(entity, task);
    BeanUtils.copyProperties(revision, task, "id");
    return task;
  }
}
