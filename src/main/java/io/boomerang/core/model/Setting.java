package io.boomerang.core.model;

import org.springframework.beans.BeanUtils;
import io.boomerang.core.entity.SettingEntity;


public class Setting extends SettingEntity{

  public Setting() {
    super();
  }

  public Setting(SettingEntity entity) {
    BeanUtils.copyProperties(entity, this); // NOSONAR
  }
}
