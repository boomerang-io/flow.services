package io.boomerang.core.audit;

import java.util.HashMap;
import java.util.Map;

public enum AuditAction {
  create("create"),
  update("update"),
  delete("delete"),
  submit("submit"),
  cancelled("cancelled"), // NOSONAR
  notstarted("notstarted"),
  ready("ready"),
  running("running"),
  waiting("waiting"), // NOSONAR
  succeeded("succeeded"),
  failed("failed"),
  invalid("invalid"),
  skipped("skipped"),
  timedout("timedout"),
  export("export"),
  leave("leave"); // NOSONAR

  private String label;

  private static final Map<String, AuditAction> BY_LABEL = new HashMap<>();

  AuditAction(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  static {
    for (AuditAction e : values()) {
      BY_LABEL.put(e.label, e);
    }
  }

  public static AuditAction valueOfLabel(String label) {
    return BY_LABEL.get(label);
  }
}
