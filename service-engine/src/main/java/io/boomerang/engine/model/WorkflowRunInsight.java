package io.boomerang.engine.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class WorkflowRunInsight {

  private Long totalRuns = 0L;
  private Long concurrentRuns = 0L;
  private Long totalDuration = 0L;
  private Long medianDuration = 0L;
  private List<WorkflowRunSummary> runs = new LinkedList<>();
}
