package io.boomerang.service;

import io.boomerang.model.Response;
import io.boomerang.model.ref.WorkflowRun;

public abstract interface WorkflowService {

  Response execute(WorkflowRun workflow);

  Response terminate(WorkflowRun workflow);
}
