package io.boomerang.workflow.model.ref;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.boomerang.model.ResultSpec;
import io.boomerang.model.RunParam;
import io.boomerang.model.TaskType;
import io.boomerang.model.TaskWorkspace;
import io.boomerang.model.WorkflowTaskDependency;
import org.springframework.data.annotation.Transient;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "type", "templateRef", "templateVersion", "templateUpgradesAvailable", "timeout", "retries", "dependencies" })
public class WorkflowTask {
  
  private String name;
  
  private io.boomerang.model.TaskType type;
  
  private String taskRef;
  
  private Integer taskVersion;
  
  @JsonInclude()
  @Transient
  private Boolean upgradesAvailable = false;
  
  private Long timeout;
  
  //private long retries = -1;
  
  private List<io.boomerang.model.WorkflowTaskDependency> dependencies = new LinkedList<>();;
  
  private Map<String, String> labels = new HashMap<>();
  
  private Map<String, Object> annotations = new HashMap<>();
  
  //Uses RunParam as the ParamSpec comes from the TaskTemplate
  private List<io.boomerang.model.RunParam> params = new LinkedList<>();
  
  //This is needed as some of our Tasks allow you to define Result Definitions on the fly
  private List<io.boomerang.model.ResultSpec> results = new LinkedList<>();;
  
  //Optional - the default is that the workspace goes to all Tasks
  //Not supported by all integrations
  private List<io.boomerang.model.TaskWorkspace> workspaces;

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

  public io.boomerang.model.TaskType getType() {
    return type;
  }

  public void setType(TaskType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTaskRef() {
    return taskRef;
  }

  public void setTaskRef(String taskRef) {
    this.taskRef = taskRef;
  }

  public Integer getTaskVersion() {
    return taskVersion;
  }

  public void setTaskVersion(Integer taskVersion) {
    this.taskVersion = taskVersion;
  }

  public Boolean getUpgradesAvailable() {
    return upgradesAvailable;
  }

  public void setUpgradesAvailable(Boolean upgradesAvailable) {
    this.upgradesAvailable = upgradesAvailable;
  }

  public Long getTimeout() {
    return timeout;
  }

  public void setTimeout(Long timeout) {
    this.timeout = timeout;
  }

  public List<io.boomerang.model.RunParam> getParams() {
    return params;
  }

  public void setParams(List<RunParam> params) {
    this.params = params;
  }

  public Map<String, Object> getAnnotations() {
    return annotations;
  }

  public void setAnnotations(Map<String, Object> annotations) {
    this.annotations = annotations;
  }

  public List<io.boomerang.model.WorkflowTaskDependency> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<WorkflowTaskDependency> dependencies) {
    this.dependencies = dependencies;
  }

  public List<io.boomerang.model.ResultSpec> getResults() {
    return results;
  }

  public void setResults(List<ResultSpec> results) {
    this.results = results;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public List<io.boomerang.model.TaskWorkspace> getWorkspaces() {
    return workspaces;
  }

  public void setWorkspaces(List<TaskWorkspace> workspaces) {
    this.workspaces = workspaces;
  }
}
