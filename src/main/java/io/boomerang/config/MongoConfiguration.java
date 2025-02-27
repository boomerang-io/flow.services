package io.boomerang.config;

import org.springframework.boot.actuate.data.mongo.MongoHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfiguration {
  @Bean
  public HealthIndicator mongoHealthIndicator(MongoTemplate mongoClient) {
    return new MongoHealthIndicator(mongoClient);
  }
}
