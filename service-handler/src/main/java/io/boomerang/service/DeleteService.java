package io.boomerang.service;

import java.util.Map;

public interface DeleteService {

  void deleteTaskRun(String workflowId, String workflowActivityId, String taskActivityId,
      Map<String, String> customLabels);
}
