package io.boomerang.common.util;

import io.boomerang.common.error.BoomerangError;
import io.boomerang.common.error.BoomerangException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.springframework.beans.BeanUtils;

/**
 * This class will do the BeanUtils.copyproperties from Entity to Model ensuring its not in the
 * shared models and the entities don't need to be copied to the other service
 */
public class ConvertUtil {

  /**
   * Generic method to convert from entity to specified Model and copy elements. public static <E,
   * M> M entityToModel(E entity, Class<M> modelClass) { return entityToModel(entity, modelClass,
   * (String[]) null); }
   *
   * <p>/** Generic method to convert from entity to specified Model and copy elements.
   *
   * <p>Includes the ability to ignore properties
   */
  public static <E, M> M entityToModel(E entity, Class<M> modelClass, String... ignoreProperties) {
    if (Objects.isNull(entity) || Objects.isNull(modelClass)) {
      throw new BoomerangException(BoomerangError.DATA_CONVERSION_FAILED);
    }

    try {
      M model = modelClass.getDeclaredConstructor().newInstance();
      BeanUtils.copyProperties(entity, model);
      return model;
    } catch (NoSuchMethodException
        | SecurityException
        | InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException ex) {
      throw new BoomerangException(ex, BoomerangError.DATA_CONVERSION_FAILED);
    }
  }
}
