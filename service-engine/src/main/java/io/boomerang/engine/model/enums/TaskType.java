package io.boomerang.engine.model.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonValue;

/*
 * TaskTypes map to Tasks and also determine the logic gates for TaskExecution.
 * 
 * If new TaskTypes are added, additional logic is needed in TaskExecutionServiceImpl
 * 
 * If TaskTypes are altered, logic will need to be checked in TaskExecutionServiceImpl
 */
public enum TaskType {
  start("start"), end("end"), template("template"), custom("custom"), generic("generic"), decision("decision"), // NOSONAR
  approval("approval"), setwfproperty("setwfproperty"), manual("manual"), eventwait("eventwait"), acquirelock("acquirelock"), // NOSONAR 
  releaselock("releaselock"), runworkflow("runworkflow"), runscheduledworkflow("runscheduledworkflow"), script("script"), // NOSONAR 
  setwfstatus("setwfstatus"), sleep("sleep"); // NOSONAR
  
  private String type;

  TaskType(String type) {
    this.type = type;
  }

  @JsonValue
  public String getType() {
    return type;
  }

  public static TaskType getRunType(String type) {
    return Arrays.asList(TaskType.values()).stream()
        .filter(value -> value.getType().equals(type)).findFirst().orElse(null);
  }
}
