package io.boomerang.engine.model;

import java.util.Date;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import io.boomerang.engine.entity.WorkflowScheduleEntity;

@Data
public class WorkflowSchedule extends WorkflowScheduleEntity {
  
  private Date nextScheduleDate;

  public WorkflowSchedule() {
    
  }

  public WorkflowSchedule(WorkflowScheduleEntity entity) {
    BeanUtils.copyProperties(entity, this);
  }

  public WorkflowSchedule(WorkflowScheduleEntity entity, Date nextScheduleDate) {
    BeanUtils.copyProperties(entity, this);
    this.nextScheduleDate = nextScheduleDate;
  }
}
