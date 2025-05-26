package io.boomerang.core.audit;

import java.util.HashMap;
import java.util.Map;

public enum AuditResource {
  system("system"),
  workflow("workflow"),
  workflowrun("workflowrun"),
  workflowtemplate("workflowtemplate"),
  taskrun("taskrun"),
  task("task)"),
  teamtask("teamtask)"),
  action("action"),
  user("user"),
  team("team"),
  token("token"),
  parameter("parameter"),
  schedule("schedule"),
  integration("integration");

  private String label;

  private static final Map<String, AuditResource> BY_LABEL = new HashMap<>();

  AuditResource(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  static {
    for (AuditResource e : values()) {
      BY_LABEL.put(e.label, e);
    }
  }

  public static AuditResource valueOfLabel(String label) {
    return BY_LABEL.get(label);
  }
}
