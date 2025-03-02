package io.boomerang.engine.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.boomerang.engine.model.enums.ExecutionCondition;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowTaskDependency {

  private String taskRef;
  private String decisionCondition;
  private ExecutionCondition executionCondition = ExecutionCondition.always;
  
  @Override
  public String toString() {
    return "TaskDependency [taskRef=" + taskRef + ", decisionCondition=" + decisionCondition
        + ", executionCondition=" + executionCondition + "]";
  }
}
