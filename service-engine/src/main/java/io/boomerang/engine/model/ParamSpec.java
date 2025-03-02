package io.boomerang.engine.model;

import io.boomerang.engine.model.enums.ParamType;
import lombok.Data;

@Data
public class ParamSpec {
  
  private String name;
  private ParamType type;
  private String description;
  private Object defaultValue;
  
  @Override
  public String toString() {
    return "ParamSpec [name=" + name + ", type=" + type + ", description=" + description
        + ", defaultValue=" + defaultValue + "]";
  }
}
