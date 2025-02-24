package io.boomerang.workflow.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import io.boomerang.workflow.entity.WorkflowScheduleEntity;
import io.boomerang.workflow.model.WorkflowScheduleStatus;

public interface WorkflowScheduleRepository extends MongoRepository<WorkflowScheduleEntity, String> {
  
  Optional<List<WorkflowScheduleEntity>> findByWorkflowRef(String ref);
  
  Optional<List<WorkflowScheduleEntity>> findByIdInAndStatusIn(List<String> ids, List<WorkflowScheduleStatus> statuses);
  
  Optional<List<WorkflowScheduleEntity>> findByWorkflowRefInAndStatusIn(List<String> workflowRefs, List<WorkflowScheduleStatus> statuses);

}
