package io.boomerang.engine.model;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.boomerang.engine.entity.TaskRunEntity;

/*
 * Based on TaskRunEntity
 */
@Data
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
}
