package io.boomerang.engine;

import io.boomerang.common.model.AgentQueueResponse;
import io.boomerang.common.model.AgentRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
@Tag(
    name = "Agent",
    description = "Manage Agent operations. Register agent. Check for WorkflowRuns and TaskRuns")
public class AgentControllerV1 {
  private final AgentService agentService;

  public AgentControllerV1(AgentService agentService) {
    this.agentService = agentService;
  }

  @PostMapping(value = "/register")
  @Operation(summary = "Register an Agent")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")
      })
  // TODO when these are exposed externally for public / private agents, require token
  // authentication
  public String registerAgent(@RequestBody AgentRegistrationRequest request) {
    return agentService.register(request);
  }

  @GetMapping(value = "/{id}/queue")
  @Operation(summary = "Register an Agent")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "204", description = "No Items Found"),
        @ApiResponse(responseCode = "400", description = "Bad Request")
      })
  // TODO when these are exposed externally for public / private agents, require token
  // authentication
  public ResponseEntity<AgentQueueResponse> agentQueue(
      @Parameter(name = "id", description = "Agent ID", required = true) @PathVariable String id) {
    return agentService.getQueue(id);
  }
}
