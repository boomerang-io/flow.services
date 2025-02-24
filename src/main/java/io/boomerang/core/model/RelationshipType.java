package io.boomerang.core.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RelationshipType {

  WORKFLOW("workflow"), WORKFLOWRUN("workflowrun"), USER("user"), TEAM("team"),
  ROOT("root"), APPROVERGROUP("approvergroup"), TEMPLATE("template"), TOKEN("token"),
  INTEGRATION("integration"), SCHEDULE("schedule"), TASK("task");

  private String ref;

  RelationshipType(String ref) {
    this.ref = ref;
  }

  @JsonValue
  public String getRef() {
    return ref;
  }
}
