package io.boomerang.workflow.model.ref;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.boomerang.model.ChangeLog;
import io.boomerang.model.TaskSpec;
import io.boomerang.model.TaskTemplateStatus;
import io.boomerang.model.TaskType;
import io.boomerang.workflow.model.AbstractParam;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;

@JsonInclude(Include.NON_NULL)
public class Task {
  
  @Id
  private String id;
  private String name;
  private String displayName;
  private io.boomerang.model.TaskType type;
  private Integer version;
  private io.boomerang.model.TaskTemplateStatus status = io.boomerang.model.TaskTemplateStatus.active;
  private Date creationDate = new Date();
  private boolean verified;
  private String description;
  private Map<String, String> labels = new HashMap<>();
  private Map<String, Object> annotations = new HashMap<>();
  private io.boomerang.model.ChangeLog changelog;
  private String category;
  private io.boomerang.model.TaskSpec spec = new io.boomerang.model.TaskSpec();
  private List<AbstractParam> config;
  private String icon;

  @Override
  public String toString() {
    return "Task{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", displayName='" + displayName + '\'' + ", type=" + type + ", version=" + version + ", status=" + status + ", creationDate=" + creationDate + ", verified=" + verified + ", description='" + description + '\'' + ", labels=" + labels + ", annotations=" + annotations + ", changelog=" + changelog + ", category='" + category + '\'' + ", spec=" + spec + ", config=" + config + ", icon='" + icon + '\'' + '}';
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public io.boomerang.model.TaskType getType() {
    return type;
  }

  public void setType(TaskType type) {
    this.type = type;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public io.boomerang.model.TaskTemplateStatus getStatus() {
    return status;
  }

  public void setStatus(TaskTemplateStatus status) {
    this.status = status;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public boolean isVerified() {
    return verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public Map<String, Object> getAnnotations() {
    return annotations;
  }

  public void setAnnotations(Map<String, Object> annotations) {
    this.annotations = annotations;
  }

  public io.boomerang.model.ChangeLog getChangelog() {
    return changelog;
  }

  public void setChangelog(ChangeLog changelog) {
    this.changelog = changelog;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public io.boomerang.model.TaskSpec getSpec() {
    return spec;
  }

  public void setSpec(TaskSpec spec) {
    this.spec = spec;
  }

  public List<AbstractParam> getConfig() {
    return config;
  }

  public void setConfig(List<AbstractParam> config) {
    this.config = config;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }
}
