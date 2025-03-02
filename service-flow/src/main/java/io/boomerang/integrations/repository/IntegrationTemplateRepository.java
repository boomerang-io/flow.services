package io.boomerang.integrations.repository;

import io.boomerang.integrations.entity.IntegrationTemplateEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IntegrationTemplateRepository extends MongoRepository<IntegrationTemplateEntity, String> {

}

