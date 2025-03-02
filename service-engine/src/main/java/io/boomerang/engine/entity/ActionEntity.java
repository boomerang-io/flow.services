package io.boomerang.engine.entity;

import java.util.Date;
import java.util.List;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.boomerang.engine.model.Actioner;
import io.boomerang.engine.model.enums.ActionStatus;
import io.boomerang.engine.model.enums.ActionType;

/*
 * Entity for Manual Action and Approval Action
 * 
 * Shared with the Workflow Service
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "#{@mongoConfiguration.fullCollectionName('actions')}")
public class ActionEntity {

  @Id
  private String id;
  private String workflowRef;
  private String workflowRunRef;
  private String taskRunRef;
  private List<Actioner> actioners;
  private ActionStatus status;
  private ActionType type;
  private String instructions;
  private Date creationDate;
  private int numberOfApprovers;
  private String approverGroupRef;
}
