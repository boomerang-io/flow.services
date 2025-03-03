package io.boomerang.common.model;

import lombok.Data;

// TODO: figure out if we need this - can we just use the name jsonPath on the param.
@Data
public class WorkflowAbstractParam extends AbstractParam {

  private String jsonPath;
}
