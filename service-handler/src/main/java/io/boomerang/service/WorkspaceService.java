package io.boomerang.service;

import io.boomerang.model.WorkspaceRequest;
import io.boomerang.model.Response;

public abstract interface WorkspaceService {

    Response create(WorkspaceRequest workspace);

    Response delete(WorkspaceRequest workspace);

    String getWorkspaceRef(String workspaceType, String workflowRef, String workflowRunRef);

    Response delete(String type, String ref);
}
