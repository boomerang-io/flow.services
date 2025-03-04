package io.boomerang.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract interface LogService {

  String getLogForTask(String workflowRef, String workflowRunRef, String taskRunRef);

  StreamingResponseBody streamLogForTask(HttpServletResponse response, String workflowRef,
      String workflowRunRef, String taskRunRef);
}