package io.boomerang.workflow.model.ref;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RunPhase {
  pending("pending"), running("running"), completed("completed"), finalized("finalized"); // NOSONAR

  private String phase;

  RunPhase(String phase) {
    this.phase = phase;
  }

  @JsonValue
  public String getPhase() {
    return phase;
  }

  public static io.boomerang.common.model.RunPhase getRunPhase(String phase) {
    return Arrays.asList(io.boomerang.common.model.RunPhase.values()).stream()
        .filter(value -> value.getPhase().equals(phase)).findFirst().orElse(null);
  }
}
