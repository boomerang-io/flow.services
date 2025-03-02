package io.boomerang.engine.model;

import lombok.Data;

import java.util.Date;

@Data
public class ChangeLog {
  private String author;
  private String reason;
  private Date date;

  public ChangeLog() {
  }

  public ChangeLog(String reason) {
    super();
    this.reason = reason;
    this.date = new Date();
  }

  public ChangeLog(String author, String reason) {
    super();
    this.author = author;
    this.reason = reason;
    this.date = new Date();
  }
}
