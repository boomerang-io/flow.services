package io.boomerang.core.model;

import io.boomerang.security.enums.AuthType;
import lombok.Data;

import java.util.Date;

@Data
public class TokenCreateResponse {

  //  @JsonIgnore
  private String id;
  private AuthType type;
  private String token;
  private Date expirationDate;
}
