package io.boomerang.workflow;

import io.boomerang.common.model.AbstractParam;
import io.boomerang.core.*;
import io.boomerang.security.AuthScope;
import io.boomerang.security.enums.AuthType;
import io.boomerang.security.enums.PermissionAction;
import io.boomerang.security.enums.PermissionScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/parameters")
@Tag(
    name = "Parameters",
    description =
        "Create, Update, and Delete global parameters available to all Teams and Workflows.")
public class ParameterControllerV2 {

  @Autowired private ParameterService paramService;

  @GetMapping(value = "")
  @AuthScope(
      action = PermissionAction.READ,
      scope = PermissionScope.SYSTEM,
      types = {AuthType.session, AuthType.user, AuthType.global})
  @Operation(summary = "Get all global Params")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")
      })
  public List<AbstractParam> getAll() {
    return paramService.getAll();
  }

  @PostMapping(value = "")
  @AuthScope(
      action = PermissionAction.WRITE,
      scope = PermissionScope.SYSTEM,
      types = {AuthType.session, AuthType.user, AuthType.global})
  @Operation(summary = "Create new global Param")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")
      })
  public AbstractParam create(@RequestBody AbstractParam request) {
    return paramService.create(request);
  }

  @PutMapping(value = "")
  @AuthScope(
      action = PermissionAction.WRITE,
      scope = PermissionScope.SYSTEM,
      types = {AuthType.session, AuthType.user, AuthType.global})
  public AbstractParam update(@RequestBody AbstractParam request) {
    return paramService.update(request);
  }

  @DeleteMapping(value = "/{name}")
  @AuthScope(
      action = PermissionAction.DELETE,
      scope = PermissionScope.SYSTEM,
      types = {AuthType.session, AuthType.user, AuthType.global})
  @Operation(summary = "Delete specific global Param")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")
      })
  public void delete(@PathVariable String name) {
    paramService.delete(name);
  }
}
