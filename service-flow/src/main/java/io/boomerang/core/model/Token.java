package io.boomerang.core.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.boomerang.security.enums.AuthType;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import io.boomerang.core.entity.TokenEntity;

@Data
public class Token {

  //  @JsonIgnore
  private String id;
  private AuthType type;
  private String name;
  private String description;
  private Date creationDate = new Date();
  private Date expirationDate;
  private boolean valid;
  private String principal;
  private List<String> permissions = new LinkedList<>();

  public Token() {}

  public Token(AuthType type) {
    super();
    this.setType(type);
  }

  public Token(TokenEntity entity) {
    BeanUtils.copyProperties(entity, this);
  }
}
