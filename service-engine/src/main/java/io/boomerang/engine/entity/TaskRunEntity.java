package io.boomerang.engine.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.boomerang.engine.model.RunParam;
import io.boomerang.engine.model.RunResult;
import io.boomerang.engine.model.WorkflowTaskDependency;
import io.boomerang.engine.model.TaskRunSpec;
import io.boomerang.engine.model.TaskWorkspace;
import io.boomerang.engine.model.enums.RunPhase;
import io.boomerang.engine.model.enums.RunStatus;
import io.boomerang.engine.model.enums.TaskType;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@Document(collection = "#{@mongoConfiguration.fullCollectionName('task_runs')}")
public class TaskRunEntity {

  @Id
  private String id;
  private TaskType type;
  private String name;
  private Map<String, String> labels = new HashMap<>();
  private Map<String, Object> annotations = new HashMap<>();
  private Date creationDate;
  private Date startTime;
  private long duration;
  private Long timeout;
//  private Long retries;
  private List<RunParam> params = new LinkedList<>();
  private List<RunResult> results = new LinkedList<>();
  private List<TaskWorkspace> workspaces = new LinkedList<>();
  private TaskRunSpec spec = new TaskRunSpec();
  private RunStatus status;
  private RunPhase phase;
  private String statusMessage;
  @JsonIgnore
  private boolean preApproved;
  @JsonIgnore
  private String decisionValue;
  @JsonIgnore
  private List<WorkflowTaskDependency> dependencies;
  private String taskRef;
  private Integer taskVersion;
  private String workflowRef;
  private String workflowRevisionRef;
  private String workflowRunRef;

  @Override
  public String toString() {
    return "TaskRunEntity [id=" + id + ", type=" + type + ", name=" + name + ", labels=" + labels
        + ", annotations=" + annotations + ", creationDate=" + creationDate + ", startTime="
        + startTime + ", params=" + params + ", status=" + status + ", phase=" + phase + "]";
  }
}
