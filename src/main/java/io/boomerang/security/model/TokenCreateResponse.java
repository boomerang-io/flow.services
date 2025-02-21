package io.boomerang.security.model;

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
