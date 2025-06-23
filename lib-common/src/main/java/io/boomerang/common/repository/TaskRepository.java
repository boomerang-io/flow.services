package io.boomerang.common.repository;

import io.boomerang.common.entity.TaskEntity;
import io.boomerang.common.enums.TaskStatus;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskRepository extends MongoRepository<TaskEntity, String> {

  boolean existsByName(String name);

  Integer countByNameAndStatus(String name, TaskStatus status);

  Integer countByIdAndStatus(String id, TaskStatus status);

  Optional<TaskEntity> findByName(String name);

  void deleteByName(String name);
}
