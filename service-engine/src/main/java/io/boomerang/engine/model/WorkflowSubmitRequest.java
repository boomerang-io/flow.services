package io.boomerang.engine.model;

import lombok.Data;

@Data
public class WorkflowSubmitRequest extends WorkflowRunRequest {
  
  private Integer workflowVersion;
  private String trigger;
}
