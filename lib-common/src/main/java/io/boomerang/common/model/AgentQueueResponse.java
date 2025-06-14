package io.boomerang.common.model;

import java.util.List;
import lombok.Data;

@Data
public class AgentQueueResponse {
  private List<WorkflowRun> workflowRuns;
  private List<TaskRun> taskRuns;

  public AgentQueueResponse() {
    // Default constructor
  }

  public AgentQueueResponse(List<WorkflowRun> workflowRuns, List<TaskRun> taskRuns) {
    this.taskRuns = taskRuns;
    this.workflowRuns = workflowRuns;
  }
}
