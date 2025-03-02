package io.boomerang.engine.model;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class TaskRunStartRequest {

  private Map<String, String> labels = new HashMap<>();
  private Map<String, Object> annotations = new HashMap<>();
  private List<RunParam> params = new LinkedList<>();
  private Map<String, String> workspaces;
  private Long timeout;
  private boolean preApproved;
}
