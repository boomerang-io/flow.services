package io.boomerang.core.entity;

import io.boomerang.core.enums.ConfigurationType;
import io.boomerang.workflow.model.AbstractParam;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "#{@mongoConfiguration.fullCollectionName('settings')}")
public class SettingEntity {

  private List<AbstractParam> config;
  private String description;
  @Id private String id;
  private String key;
  private Date lastModiifed;
  private String name;
  private String tool;
  private ConfigurationType type;
}
