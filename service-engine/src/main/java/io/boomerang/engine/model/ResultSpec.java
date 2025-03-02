package io.boomerang.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/*
 * Partially replicates Tekton TaskResult but ensures that the SDK Model is not exposed
 * as the controllers model
 * 
 * Reference:
 * - io.fabric8.tekton.pipeline.v1beta1.TaskResult;
 */
@Data
@JsonIgnoreProperties
public class ResultSpec {

  private String description;
  private String name;

  public ResultSpec() {
  }

  public ResultSpec(String description, String name) {
      super();
      this.description = description;
      this.name = name;
  }
}
