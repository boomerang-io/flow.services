package io.boomerang.client;

import io.boomerang.model.ref.TaskRunEndRequest;

public interface EngineClient {

  void finalizeWorkflow(String wfRunId);

  void startWorkflow(String wfRunId);

  void startTask(String taskRunId);

  void endTask(String taskRunId, TaskRunEndRequest endRequest);
  
}
