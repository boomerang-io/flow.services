package io.boomerang.workflow.model.ref;

import io.boomerang.model.RunResult;
import io.boomerang.model.RunStatus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkflowRunEventRequest {

  private Map<String, String> labels = new HashMap<>();

  private Map<String, Object> annotations = new HashMap<>();
  
  private List<io.boomerang.model.RunResult> results = new LinkedList<>();
  
  private io.boomerang.model.RunStatus status = io.boomerang.model.RunStatus.succeeded;
  
  private String topic;

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

  public io.boomerang.model.RunStatus getStatus() {
    return status;
  }

  public void setStatus(RunStatus status) {
    this.status = status;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }
}
