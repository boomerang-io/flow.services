package io.boomerang.engine.repository;

import io.boomerang.common.entity.WorkflowRunEntity;
import io.boomerang.common.enums.RunPhase;
import io.boomerang.common.enums.RunStatus;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkflowRunRepository extends MongoRepository<WorkflowRunEntity, String> {

  void deleteByWorkflowRef(String workflowRef);

  List<WorkflowRunEntity> findByPhaseInAndStatusIn(List<RunPhase> phase, List<RunStatus> statuses);

  List<WorkflowRunEntity> findByPhase(RunPhase phase);
}
