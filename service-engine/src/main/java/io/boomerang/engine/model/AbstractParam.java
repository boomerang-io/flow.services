package io.boomerang.engine.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AbstractParam {

  private String key;
  private String description;
  private String label;
  private String type;
  private Integer minValueLength;
  private Integer maxValueLength;
  private List<KeyValuePair> options;
  private Boolean required;
  private String placeholder;
  @JsonProperty("helperText")
  private String helpertext;
  private String language;
  private Boolean disabled;
  private String defaultValue;
  private String value;
  private List<String> values;
  private boolean readOnly;
  private Boolean hiddenValue;

  public AbstractParam() {
  }

  @Override
  public String toString() {
    return "AbstractParam [key=" + key + ", description=" + description + ", label=" + label
        + ", type=" + type + "]";
  }

  @JsonIgnore
  public boolean getBooleanValue() {
    if ("boolean".equals(this.getType())) {
      return Boolean.parseBoolean(this.getValue());
    } else {
      throw new IllegalArgumentException("Configuration object is not of type boolean.");
    }
  }
}
