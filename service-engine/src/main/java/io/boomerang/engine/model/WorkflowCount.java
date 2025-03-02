package io.boomerang.engine.model;

import lombok.Data;

import java.util.Map;

@Data
public class WorkflowCount {

  private Map<String, Long> status;
}
