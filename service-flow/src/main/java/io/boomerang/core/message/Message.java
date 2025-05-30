package io.boomerang.core.message;

import java.time.Instant;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
public class Message {

  @Id private String id;
  private Instant timestamp;
  private String type;
  private String message;

  public Message() {}

  public Message(String type, String message) {
    this.id = new ObjectId().toHexString();
    this.timestamp = Instant.now();
    this.type = type;
    this.message = message;
  }
}
