package io.boomerang.workflow.model.ref;

import io.boomerang.common.model.ChangeLog;

public class ChangeLogVersion extends ChangeLog {
  private Integer version;

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }
}
