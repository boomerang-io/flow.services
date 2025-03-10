package io.boomerang.workflow;

import io.boomerang.common.model.AbstractParam;
import io.boomerang.common.util.DataAdapterUtil;
import io.boomerang.common.util.DataAdapterUtil.FieldType;
import io.boomerang.error.BoomerangError;
import io.boomerang.error.BoomerangException;
import io.boomerang.workflow.entity.GlobalParamEntity;
import io.boomerang.workflow.repository.GlobalParamRepository;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * CRUD for Global Params
 *
 * TODO: check if this is feature gated in settings
 */
@Service
public class GlobalParamService {

  private final GlobalParamRepository paramRepository;

  public GlobalParamService(@Autowired GlobalParamRepository paramRepository) {
    this.paramRepository = paramRepository;
  }

  public List<AbstractParam> getAll() {
    List<GlobalParamEntity> entities = paramRepository.findAll();
    List<AbstractParam> params = new LinkedList<>();
    for (GlobalParamEntity entity : entities) {
      params.add(convertToAbstractParamAndFilter(entity));
    }
    return params;
  }

  public List<AbstractParam> getAllUnfiltered() {
    List<GlobalParamEntity> entities = paramRepository.findAll();
    List<AbstractParam> params = new LinkedList<>();
    for (GlobalParamEntity entity : entities) {
      AbstractParam param = new AbstractParam();
      BeanUtils.copyProperties(entity, param, "id");
      params.add(param);
    }
    return params;
  }

  public AbstractParam update(AbstractParam param) {
    if (!Objects.isNull(param) && param.getKey() != null) {
      Optional<GlobalParamEntity> optParamEntity = paramRepository.findOneByKey(param.getKey());
      if (!optParamEntity.isEmpty()) {
        // Copy updatedParam to ParamEntity except for ID (requester should not know ID);
        BeanUtils.copyProperties(param, optParamEntity.get(), "id");
        GlobalParamEntity entity = paramRepository.save(optParamEntity.get());
        return convertToAbstractParamAndFilter(entity);
      }
    }
    throw new BoomerangException(BoomerangError.PARAMS_INVALID_REFERENCE);
  }

  public AbstractParam create(AbstractParam param) {
    if (!Objects.isNull(param) && param.getKey() != null) {

      // Ensure key is unique
      if (paramRepository.countByKey(param.getKey()) > 0) {
        throw new BoomerangException(BoomerangError.PARAMS_NON_UNIQUE_KEY);
      }

      GlobalParamEntity entity = new GlobalParamEntity();
      BeanUtils.copyProperties(param, entity, "id");
      entity = paramRepository.save(entity);
      return convertToAbstractParamAndFilter(entity);
    }
    throw new BoomerangException(BoomerangError.PARAMS_INVALID_REFERENCE);
  }

  public void delete(String key) {
    if (!Objects.isNull(key) && key != null && paramRepository.countByKey(key) > 0) {
      paramRepository.deleteByKey(key);
    }
    throw new BoomerangException(BoomerangError.PARAMS_INVALID_REFERENCE);
  }

  /*
   * Converts from GlobalParamEntity to AbstractParam and filters out secure values
   */
  private AbstractParam convertToAbstractParamAndFilter(GlobalParamEntity entity) {
    AbstractParam param = new AbstractParam();
    BeanUtils.copyProperties(entity, param, "id");
    return DataAdapterUtil.filterAbstractParam(param, false, FieldType.PASSWORD.value());
  }
}
