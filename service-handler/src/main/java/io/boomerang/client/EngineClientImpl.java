package io.boomerang.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import io.boomerang.model.ref.TaskRunEndRequest;

@Service
public class EngineClientImpl implements EngineClient {

  private static final Logger logger = LogManager.getLogger(EngineClientImpl.class);

  @Value("${flow.engine.workflowrun.start.url}")
  private String startWorkflowRunURL;

  @Value("${flow.engine.workflowrun.finalize.url}")
  private String finalizeWorkflowRunURL;
  
  @Value("${flow.engine.taskrun.start.url}")
  private String startTaskRunURL;

  @Value("${flow.engine.taskrun.end.url}")
  private String endTaskRunURL;

  @Autowired
  @Qualifier("internalRestTemplate")
  public RestTemplate restTemplate;

  @Override
  public void startWorkflow(String wfRunId) {
    try {
      String url = startWorkflowRunURL.replace("{workflowRunId}", wfRunId);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>("{}", headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

      logger.info(response.getStatusCode());
    } catch (RestClientException ex) {
      logger.error(ex.toString());
    }
  }

  @Override
  public void finalizeWorkflow(String wfRunId) {
    try {
      String url = finalizeWorkflowRunURL.replace("{workflowRunId}", wfRunId);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>("{}", headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

      logger.info(response.getStatusCode());
    } catch (RestClientException ex) {
      logger.error(ex.toString());
    }
  }

  @Override
  public void startTask(String taskRunId) {
    try {
      String url = startTaskRunURL.replace("{taskRunId}", taskRunId);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>("{}", headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

      logger.info(response.getStatusCode());
    } catch (RestClientException ex) {
      logger.error(ex.toString());
    }
  }

  @Override
  public void endTask(String taskRunId, TaskRunEndRequest endRequest) {
    try {
      String url = endTaskRunURL.replace("{taskRunId}", taskRunId);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<TaskRunEndRequest> entity = new HttpEntity<TaskRunEndRequest>(endRequest, headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

      logger.info(response.getStatusCode());
    } catch (RestClientException ex) {
      logger.error(ex.toString());
    }
  }
}
