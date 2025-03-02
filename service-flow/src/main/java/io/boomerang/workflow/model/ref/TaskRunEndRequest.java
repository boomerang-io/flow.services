package io.boomerang.workflow.model.ref;

import io.boomerang.model.RunResult;
import io.boomerang.model.RunStatus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TaskRunEndRequest {
  
  private io.boomerang.model.RunStatus status;

  private Map<String, String> labels = new HashMap<>();

  private Map<String, Object> annotations = new HashMap<>();
  
  private List<io.boomerang.model.RunResult> results = new LinkedList<>();

  private String statusMessage;

  public io.boomerang.model.RunStatus getStatus() {
    return status;
  }

  public void setStatus(RunStatus status) {
    this.status = status;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public Map<String, Object> getAnnotations() {
    return annotations;
  }

  public void setAnnotations(Map<String, Object> annotations) {
    this.annotations = annotations;
  }

  public List<io.boomerang.model.RunResult> getResults() {
    return results;
  }

  public void setResults(List<RunResult> results) {
    this.results = results;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public void setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
  }
}
