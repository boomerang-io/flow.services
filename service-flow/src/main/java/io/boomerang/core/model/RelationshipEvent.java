package io.boomerang.core.model;

public class RelationshipEvent {

  private final long timestamp;
  private String message;

  public RelationshipEvent(String message) {
    this.timestamp = System.currentTimeMillis();
    this.message = message;
  }
}
