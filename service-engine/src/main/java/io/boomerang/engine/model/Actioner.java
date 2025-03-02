package io.boomerang.engine.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Actioner {

  private String approverId;
  private String approverEmail;
  private String approverName;
  private String comments;
  private Date date;
  private boolean approved;
}
