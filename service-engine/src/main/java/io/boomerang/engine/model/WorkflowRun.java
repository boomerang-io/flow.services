package io.boomerang.engine.model;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.boomerang.engine.model.enums.RunPhase;
import io.boomerang.engine.model.enums.RunStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Data
@JsonPropertyOrder({"id", "creationDate", "status", "phase", "startTime", "duration", "statusMessage", "error", "timeout", "retries", "workflowRef", "workflowRevisionRef", "labels", "annotations", "params", "tasks" })
public class WorkflowRun {
  
  @Id
  private String id;
  private Map<String, String> labels = new HashMap<>();
  private Map<String, Object> annotations = new HashMap<>();
  private Date creationDate;
  private Date startTime;
  private long duration = 0;
  private Long timeout;
  private Long retries;
  private Boolean debug;
  private RunStatus status = RunStatus.notstarted;
  private RunPhase phase = RunPhase.pending;
  private String statusMessage;
  private boolean isAwaitingApproval;
  private String workflowRef;
  private String workflowName;
  private Integer workflowVersion;
  private String workflowRevisionRef;
  private String trigger;
  private String initiatedByRef;
  private List<RunParam> params = new LinkedList<>();
  private List<RunResult> results = new LinkedList<>();
  private List<WorkflowWorkspace> workspaces = new LinkedList<>();
  private List<TaskRun> tasks;

  @Override
  public String toString() {
    return "WorkflowRun [id=" + id + ", creationDate=" + creationDate + ", startTime=" + startTime
        + ", duration=" + duration + ", timeout=" + timeout + ", retries=" + retries + ", debug="
        + debug + ", status=" + status + ", phase=" + phase + ", statusMessage=" + statusMessage
        + ", isAwaitingApproval=" + isAwaitingApproval + ", workflowRef="
        + workflowRef + ", workflowName=" + workflowName + ", workflowVersion=" + workflowVersion
        + ", workflowRevisionRef=" + workflowRevisionRef + ", trigger=" + trigger + ", params="
        + params + ", results=" + results + ", workspaces=" + workspaces + "]";
  }
}
