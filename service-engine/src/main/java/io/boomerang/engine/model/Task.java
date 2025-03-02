package io.boomerang.engine.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.boomerang.engine.model.enums.TaskStatus;
import io.boomerang.engine.model.enums.TaskType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class Task {
  
  @Id
  private String id;
  private String name;
  private String displayName;
  private TaskType type;
  private Integer version;
  private TaskStatus status = TaskStatus.active;
  private Date creationDate = new Date();
  private boolean verified;
  private String description;
  private Map<String, String> labels = new HashMap<>();
  private Map<String, Object> annotations = new HashMap<>();
  private ChangeLog changelog;
  private String category;
  private TaskSpec spec = new TaskSpec();
  private List<AbstractParam> config;
  private String icon;
}
