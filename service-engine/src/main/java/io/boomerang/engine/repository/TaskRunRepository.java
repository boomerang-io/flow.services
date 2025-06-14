package io.boomerang.engine.repository;

import io.boomerang.common.entity.TaskRunEntity;
import io.boomerang.common.entity.WorkflowRunEntity;
import io.boomerang.common.enums.RunPhase;
import io.boomerang.common.enums.RunStatus;
import io.boomerang.common.enums.TaskType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskRunRepository extends MongoRepository<TaskRunEntity, String> {

  List<TaskRunEntity> findByWorkflowRunRef(String workflowRunRef);

  Optional<TaskRunEntity> findFirstByNameAndWorkflowRunRef(String name, String workflowRunRef);

  void deleteByWorkflowRef(String workflowRef);

  void deleteByWorkflowRunRef(String workflowRunRef);

  List<WorkflowRunEntity> findByPhaseInAndStatusInAndTypeIn(
      List<RunPhase> phase, List<RunStatus> statuses, List<TaskType> types);
}
