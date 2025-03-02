package io.boomerang.workflow.model.ref;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RunStatus {
  notstarted("notstarted"), ready("ready"), running("running"), waiting("waiting"),  // NOSONAR
  succeeded("succeeded"), failed("failed"), invalid("invalid"), skipped("skipped"), // NOSONAR 
  cancelled("cancelled"), timedout("timedout"); // NOSONAR

  private String status;

  RunStatus(String status) {
    this.status = status;
  }

  @JsonValue
  public String getStatus() {
    return status;
  }

  public static io.boomerang.common.model.RunStatus getRunStatus(String status) {
    return Arrays.asList(io.boomerang.common.model.RunStatus.values()).stream()
        .filter(value -> value.getStatus().equals(status)).findFirst().orElse(null);
  }
}
