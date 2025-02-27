package io.boomerang.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.boomerang.security.enums.PermissionAction;
import io.boomerang.security.enums.PermissionScope;
import io.boomerang.security.enums.AuthType;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthScope {
  PermissionScope scope();

  PermissionAction action();

  AuthType[] types();
}
