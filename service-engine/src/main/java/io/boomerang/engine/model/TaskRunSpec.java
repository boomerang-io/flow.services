package io.boomerang.engine.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.boomerang.engine.model.enums.TaskDeletion;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class TaskRunSpec {

  private List<String> arguments;
  private List<String> command;
  private List<TaskEnvVar> envs;
  private String image;
  private String script;
  private String workingDir;
  private Boolean debug = false;
  private TaskDeletion deletion;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }
}
