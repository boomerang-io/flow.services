package io.boomerang.common.model;

import io.boomerang.common.enums.ParamType;
import lombok.Data;

@Data
public class ParamSpec {

  private String name;
  private ParamType type;
  private String description;
  private Object defaultValue;

  @Override
  public String toString() {
    return "ParamSpec [name="
        + name
        + ", type="
        + type
        + ", description="
        + description
        + ", defaultValue="
        + defaultValue
        + "]";
  }
}
