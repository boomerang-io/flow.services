package io.boomerang.engine.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import io.boomerang.common.entity.WorkflowRunEntity;

public interface WorkflowRunRepository extends MongoRepository<WorkflowRunEntity, String> {

  void deleteByWorkflowRef(String workflowRef);
}
