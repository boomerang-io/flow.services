package io.boomerang.engine.model;

import lombok.Data;

import java.util.Map;

@Data
public class WorkflowRunCount {

  private Map<String, Long> status;
}
