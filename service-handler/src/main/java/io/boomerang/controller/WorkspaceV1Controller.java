package io.boomerang.controller;

import io.boomerang.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.boomerang.model.WorkspaceRequest;
import io.boomerang.model.Response;

@RestController
@RequestMapping("/api/v1/workspace")
public class WorkspaceV1Controller {

  @Autowired private WorkspaceService workspaceService;

  @PostMapping(value = "/create")
  public Response createWorkspace(@RequestBody WorkspaceRequest request) {
    return workspaceService.create(request);
  }

  @PostMapping(value = "/delete")
  public Response deleteWorkspace(@RequestBody WorkspaceRequest request) {
    return workspaceService.delete(request);
  }
}
