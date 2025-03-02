package io.boomerang.engine.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.boomerang.engine.model.enums.RunStatus;
import lombok.Data;

@Data
public class TaskRunEndRequest {
  
  private RunStatus status;
  private Map<String, String> labels = new HashMap<>();
  private Map<String, Object> annotations = new HashMap<>();
  private List<RunResult> results = new LinkedList<>();
  private String statusMessage;
}
