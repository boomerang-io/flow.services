package io.boomerang.engine.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import io.boomerang.engine.entity.TaskRunEntity;

public interface TaskRunRepository
    extends MongoRepository<TaskRunEntity, String> {

  List<TaskRunEntity> findByWorkflowRunRef(String workflowRunRef);

  Optional<TaskRunEntity> findFirstByNameAndWorkflowRunRef(String name, String workflowRunRef);
  
  void deleteByWorkflowRef(String workflowRef);
  
  void deleteByWorkflowRunRef(String workflowRunRef);
}
