package io.boomerang.core.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.boomerang.core.model.Token;
import io.boomerang.security.enums.AuthScope;

@JsonInclude(Include.NON_NULL)
public class AuditActor {

  private String principal;
  private AuthScope type;
  private String tokenRef;

  public AuditActor() {
    // TODO Auto-generated constructor stub
  }

  public AuditActor(Token token) {
    this.principal = token.getPrincipal() != null ? token.getPrincipal() : "";
    this.type = token.getType();
    this.tokenRef = token.getId();
  }

  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal(String principal) {
    this.principal = principal;
  }

  public AuthScope getType() {
    return type;
  }

  public void setType(AuthScope type) {
    this.type = type;
  }

  public String getTokenRef() {
    return tokenRef;
  }

  public void setTokenRef(String tokenRef) {
    this.tokenRef = tokenRef;
  }
}
