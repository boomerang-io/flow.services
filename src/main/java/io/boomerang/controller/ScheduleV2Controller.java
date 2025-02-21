package io.boomerang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.boomerang.model.CronValidationResponse;
import io.boomerang.security.AuthScope;
import io.boomerang.security.model.AuthType;
import io.boomerang.security.model.PermissionAction;
import io.boomerang.security.model.PermissionScope;
import io.boomerang.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v2/schedule")
@Tag(name = "Schedule Management",
description = "Provide the ability to create and update Schedules.")
public class ScheduleV2Controller {

  @Autowired
  private ScheduleService workflowScheduleService;
  
  @GetMapping(value = "/validate-cron")
  @AuthScope(action = PermissionAction.READ, scope = PermissionScope.SCHEDULE, types = {AuthType.team})
  @Operation(summary = "Validate a Schedules CRON.")
  public CronValidationResponse validateCron(@Parameter(name = "cron",
      description = "A CRON expression to validate",
      required = true) @RequestParam String cron) {
    return workflowScheduleService.validateCron(cron);
  }
}
