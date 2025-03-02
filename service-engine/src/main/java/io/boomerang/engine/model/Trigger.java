package io.boomerang.engine.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
// TODO: implement a more generic trigger with list of triggers and a type rather than fixed.
public class Trigger {

  private Boolean enabled = Boolean.FALSE;
  // private TriggerEnum type;
  private List<TriggerCondition> conditions = new LinkedList<>();

  public Trigger() {}

  public Trigger(Boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return "Trigger [enabled=" + enabled + ", conditions=" + conditions + "]";
  }
}
