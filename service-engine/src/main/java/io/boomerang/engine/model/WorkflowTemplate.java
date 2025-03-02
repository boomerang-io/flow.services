package io.boomerang.engine.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.boomerang.engine.entity.WorkflowTemplateEntity;

/*
 * Workflow Model joining Workflow Entity and Workflow Revision Entity
 * 
 * A number of the Workflow Revision elements are put under metadata
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "name", "displayName", "version", "creationDate", "timeout", "retries", "description", "labels", "annotations", "params", "tasks" })
public class WorkflowTemplate extends WorkflowTemplateEntity {
  
  private boolean upgradesAvailable = false;
  private Map<String, Object> unknownFields = new HashMap<>();
  @JsonAnyGetter
  @JsonPropertyOrder(alphabetic = true)
  public Map<String, Object> otherFields() {
    return unknownFields;
  }
  @JsonAnySetter
  public void setOtherField(String name, Object value) {
    unknownFields.put(name, value);
  }
  
  public WorkflowTemplate() {
    
  }

  /*
   * Creates a WorkflowTemplate from WorkflowTemplateEntity
   */
  public WorkflowTemplate(WorkflowTemplateEntity entity) {
    BeanUtils.copyProperties(entity, this);
  }
}
