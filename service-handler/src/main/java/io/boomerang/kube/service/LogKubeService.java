package io.boomerang.kube.service;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface LogKubeService {
  StreamingResponseBody streamPodLog(HttpServletResponse response, String workflowId,
      String workflowActivityId, String taskActivityId, Map<String, String> customLabels);

  String getPodLog(String workflowId, String workflowActivityId, String taskActivityId,
      Map<String, String> customLabels);

}
