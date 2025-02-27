package io.boomerang.security.model;

import io.boomerang.security.enums.PermissionAction;
import io.boomerang.security.enums.PermissionScope;
import lombok.Data;

// TODO transform to the new type of permission made up of assigned scope and then array of actions
@Data
public class Permission {
  private PermissionScope scope;
  private String principal;
  private PermissionAction action;

  public Permission() {}

  public Permission(String permission) {
    String[] spread = permission.split("/");
    PermissionScope.valueOf(spread[0]);
  }

  public Permission(PermissionScope scope, String principal, PermissionAction action) {
    this.scope = scope;
    this.principal = principal;
    this.action = action;
  }

  @Override
  public String toString() {
    return scope + "\\" + principal + "\\" + action;
  }
}
