package io.boomerang.engine.model;

import java.util.List;

import io.boomerang.engine.model.enums.TriggerConditionOperation;
import lombok.Data;

@Data
public class TriggerCondition {
  
  private String field;
  private TriggerConditionOperation operation;
  private String value;
  private List<String> values;
  
  @Override
  public String toString() {
    return "TriggerCondition [field=" + field + ", operation=" + operation + ", value=" + value
        + ", values=" + values + ", getField()=" + getField() + ", getOperation()=" + getOperation()
        + ", getValue()=" + getValue() + ", getValues()=" + getValues() + ", getClass()="
        + getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
  }
}
