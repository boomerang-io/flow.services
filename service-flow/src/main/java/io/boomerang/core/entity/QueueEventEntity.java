 package io.boomerang.core.entity;

import java.time.Instant;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "queue")
 @Data
 public class QueueEventEntity {
  @Id private String id;
  private String eventType;
  private String payload;
  private Instant timestamp;

  public QueueEventEntity(String eventType, String payload) {
    this.eventType = eventType;
    this.payload = payload;
    this.timestamp = Instant.now();
  }
 }
