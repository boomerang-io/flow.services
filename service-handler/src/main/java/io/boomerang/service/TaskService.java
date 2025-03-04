package io.boomerang.service;

import io.boomerang.model.TaskResponse;
import io.boomerang.model.ref.TaskRun;

public abstract interface TaskService {

	TaskResponse execute(TaskRun task);

    TaskResponse terminate(TaskRun task);
}
