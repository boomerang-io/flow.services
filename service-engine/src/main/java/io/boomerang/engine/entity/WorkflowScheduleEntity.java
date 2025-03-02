package io.boomerang.engine.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.boomerang.engine.model.RunParam;
import io.boomerang.engine.model.enums.WorkflowScheduleStatus;
import io.boomerang.engine.model.enums.WorkflowScheduleType;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@Document(collection = "#{@mongoConfiguration.fullCollectionName('workflow_schedules')}")
public class WorkflowScheduleEntity {

  private String id;
  private String workflowRef;
  private String name;
  private String description;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private Date creationDate = new Date();
  private WorkflowScheduleType type = WorkflowScheduleType.cron;
  private WorkflowScheduleStatus status = WorkflowScheduleStatus.active;
  private Map<String, String> labels = new HashMap<>();
  private String cronSchedule;
  private Date dateSchedule;
  private String timezone;
  private List<RunParam> params = new LinkedList<>();
}
