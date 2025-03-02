package io.boomerang.workflow.model.ref;

import org.springframework.beans.BeanUtils;
import io.boomerang.entity.TaskRunEntity;

/*
 * Based on TaskRunEntity
 */
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
