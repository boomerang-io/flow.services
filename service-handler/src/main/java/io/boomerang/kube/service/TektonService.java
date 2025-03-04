package io.boomerang.kube.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import io.boomerang.model.ref.RunParam;
import io.boomerang.model.ref.RunResult;
import io.boomerang.model.ref.TaskEnvVar;
import io.boomerang.model.ref.TaskWorkspace;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;

public interface TektonService {

  void cancelTaskRun(String workflowId, String workflowActivityId, String taskActivityId,
      Map<String, String> customLabels);

  TaskRun createTaskRun(String workflowId, String workflowActivityId, String taskActivityId,
      String taskName, Map<String, String> customLabels, String image, List<String> command,
      String script, List<String> arguments, List<RunParam> parameters, List<TaskEnvVar> envVars,
      List<io.boomerang.model.ref.RunResult> results, String workingDir,
      List<TaskWorkspace> workspaces, long waitSeconds, Long timeout, Boolean debug)
      throws InterruptedException, ParseException;

  List<RunResult> watchTaskRun(String workflowId, String workflowActivityId,
      String taskActivityId, Map<String, String> customLabels, Long timeout)
      throws InterruptedException;

  void deleteTaskRun(String workflowId, String workflowActivityId, String taskActivityId,
      Map<String, String> customLabels);

}
