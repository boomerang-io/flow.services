package io.boomerang.model.ref;

import org.springframework.beans.BeanUtils;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/*
 * Based on TaskRunEntity
 */
@JsonPropertyOrder({"id", "type", "name", "status", "phase", "creationDate", "startTime", "duration", "timeout", "statusMessage", "error", "labels", "params", "tasks" })
public class TaskRun extends TaskRunEntity {
  
  private String workflowName;
  
  public TaskRun() {
    
  }

  public TaskRun(TaskRunEntity entity) {
    BeanUtils.copyProperties(entity, this);
  }

  @Override
  public String toString() {
    return "TaskRun [workflowName=" + workflowName + ", toString()=" + super.toString() + "]";
  }

  public String getWorkflowName() {
    return workflowName;
  }

  public void setWorkflowName(String workflowName) {
    this.workflowName = workflowName;
  }  
}
