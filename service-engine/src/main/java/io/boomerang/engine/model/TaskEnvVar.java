package io.boomerang.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/*
 * Partially replicates Tekton EnvVar but ensures that the SDK Model is not exposed
 * as the controllers model
 * 
 * Reference:
 * - import io.fabric8.kubernetes.api.model.EnvVar;
 */
@Data
@JsonIgnoreProperties
public class TaskEnvVar {

  private String name;
  private String value;

  public TaskEnvVar() {
  }

  public TaskEnvVar(String name, String value) {
      super();
      this.name = name;
      this.value = value;
  }
  
}
