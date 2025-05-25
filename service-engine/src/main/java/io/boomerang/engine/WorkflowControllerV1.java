package io.boomerang.engine;

import io.boomerang.common.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflow")
@Tag(name = "Workflow Management", description = "Submit your Workflow to run.")
public class WorkflowControllerV1 {

  private final WorkflowService workflowService;

  public WorkflowControllerV1(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @PostMapping(value = "/{workflowId}/submit")
  @Operation(
      summary = "Submit a Workflow to be run. Will queue the WorkflowRun ready for execution.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")
      })
  public WorkflowRun submitWorkflow(
      @Parameter(name = "workflowId", description = "ID of Workflow", required = true) @PathVariable
          String workflowId,
      @Parameter(
              name = "start",
              description = "Start the WorkflowRun immediately after submission",
              required = false)
          @RequestParam(required = false, defaultValue = "false")
          boolean start,
      @RequestBody WorkflowSubmitRequest request) {
    return workflowService.submit(workflowId, request, start);
  }
}
