package io.boomerang.engine.repository;

import java.util.Date;
import java.util.List;

import io.boomerang.engine.entity.AgentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface AgentRepository extends MongoRepository<AgentEntity, String> {

  boolean existsById(String id);

  @Query("{ '_id': ?0 }")
  @Update("{ '$set': { 'lastConnectedDate': ?1 } }")
  void updateLastConnected(String agentId, Date lastConnected);

  @Query(value = "{ '_id': ?0 }", fields = "{ 'taskTypes': 1, '_id': 0 }")
  List<String> findTaskTypesByAgentId(String agentId);
}
