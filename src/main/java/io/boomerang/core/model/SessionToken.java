package io.boomerang.core.model;

import io.boomerang.core.entity.TokenEntity;
import io.boomerang.security.enums.AuthType;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class SessionToken {

  //  @JsonIgnore
  private String id;
  private AuthType type;
  private String name;
  private String description;
  private Date creationDate = new Date();
  private Date expirationDate;
  private String principal;
  private List<String> permissions = new LinkedList<>();

  public SessionToken() {}

  public SessionToken(AuthType type) {
    super();
    this.setType(type);
  }

  public SessionToken(TokenEntity entity) {
    BeanUtils.copyProperties(entity, this);
  }
}
