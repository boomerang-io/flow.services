package io.boomerang.common.repository;

import io.boomerang.common.entity.WorkflowRunEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkflowRunRepository extends MongoRepository<WorkflowRunEntity, String> {

  void deleteByWorkflowRef(String workflowRef);
}
