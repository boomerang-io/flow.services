package io.boomerang.core.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.boomerang.core.model.Token;
import java.util.Date;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AuditEvent {

  private AuditAction action;
  private Date date = new Date();
  private AuditActor actor;

  public AuditEvent() {
    // TODO Auto-generated constructor stub
  }

  public AuditEvent(AuditAction action, Token token) {
    this.action = action;
    if (token != null) {
      this.actor = new AuditActor(token);
    }
  }
}
