package io.boomerang.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.boomerang.model.ref.RunResult;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskResponse extends Response {

  private List<RunResult> results = new ArrayList<>();

  public TaskResponse() {
    // Do nothing
  }

  public TaskResponse(String code, String desc, List<RunResult> results) {
    super(code, desc);
    this.results = results;
  }

  public List<RunResult> getResults() {
    return results;
  }

  public void setResults(List<RunResult> results) {
    this.results = results;
  }
}
