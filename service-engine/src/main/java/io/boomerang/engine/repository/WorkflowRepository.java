package io.boomerang.engine.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import io.boomerang.engine.entity.WorkflowEntity;
import io.boomerang.engine.model.enums.WorkflowStatus;

public interface WorkflowRepository extends MongoRepository<WorkflowEntity, String> {

  List<WorkflowEntity> findByStatus(WorkflowStatus status);
}
