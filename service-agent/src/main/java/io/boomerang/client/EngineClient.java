package io.boomerang.client;

import io.boomerang.agent.QueueService;
import io.boomerang.common.model.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class EngineClient {

  private static final Logger LOGGER = LogManager.getLogger(EngineClient.class);

  private static final Integer HEARTBEAT_INTERVAL = 5000; // 5 seconds

  private String agentHost;

  private String agentId;

  @Value("${flow.engine.workflowrun.start.url}")
  private String startWorkflowRunURL;

  @Value("${flow.engine.workflowrun.finalize.url}")
  private String finalizeWorkflowRunURL;

  @Value("${flow.engine.taskrun.start.url}")
  private String startTaskRunURL;

  @Value("${flow.engine.taskrun.end.url}")
  private String endTaskRunURL;

  @Value("${flow.engine.agent.register.url}")
  private String agentRegisterURL;

  @Value("${flow.engine.agent.queue.url}")
  private String agentQueueURL;

  @Value("${flow.agent.task-types}")
  private List<String> taskTypes;

  @Value("${flow.agent.name}")
  private String agentName;

  @Autowired
  @Qualifier("internalRestTemplate")
  public RestTemplate restTemplate;

  @Autowired public QueueService queueService;

  public void startWorkflow(String wfRunId) {
    try {
      String url = startWorkflowRunURL.replace("{workflowRunId}", wfRunId);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>("{}", headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

      LOGGER.info(response.getStatusCode());
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
    }
  }

  public void finalizeWorkflow(String wfRunId) {
    try {
      String url = finalizeWorkflowRunURL.replace("{workflowRunId}", wfRunId);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>("{}", headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

      LOGGER.info(response.getStatusCode());
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
    }
  }

  public void startTask(String taskRunId) {
    try {
      String url = startTaskRunURL.replace("{taskRunId}", taskRunId);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>("{}", headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

      LOGGER.info(response.getStatusCode());
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
    }
  }

  public void endTask(String taskRunId, TaskRunEndRequest endRequest) {
    try {
      String url = endTaskRunURL.replace("{taskRunId}", taskRunId);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<TaskRunEndRequest> entity = new HttpEntity<TaskRunEndRequest>(endRequest, headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

      LOGGER.info(response.getStatusCode());
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
    }
  }

  /**
   * Registers the agent and its capabilities with the engine
   *
   * <p>This should block and cause the service to exit if it cannot register
   */
  public void registerAgent() {
    try {
      // Retrieve the hostname as the machine ID
      agentHost = InetAddress.getLocalHost().getHostName();
      LOGGER.debug("Registering Agent({})", agentHost);

      AgentRegistrationRequest request =
          new AgentRegistrationRequest(agentName, agentHost, taskTypes);

      // Send the registration request
      ResponseEntity<String> response =
          restTemplate.postForEntity(agentRegisterURL, request, String.class);
      if (!response.getStatusCode().is2xxSuccessful()) {
        LOGGER.error(
            "Failed to register Agent({}). Status: {}", agentHost, response.getStatusCode());
        throw new RuntimeException(
            "Failed to register Agent: " + agentHost + ". Status: " + response.getStatusCode());
      }
      agentId = response.getBody();
      LOGGER.debug("Agent {}({}) registered successfully.", agentId, agentHost);
    } catch (UnknownHostException e) {
      throw new RuntimeException("Failed to retrieve hostname for machine ID", e);
    } catch (Exception e) {
      throw new RuntimeException("Error during Agent registration: " + e.getMessage());
    }
  }

  /**
   * Implements a heartbeat style workflows check. This should be run in a background thread and
   * handle failure
   *
   * <p>200 means there are workflow runs available
   *
   * <p>204 means there are no workflow runs available
   *
   * <p>TODO in the future optimise the Async to have a LinkedBlockingQueue with maximum size of
   * what it can achieve
   *
   * <p>TODO: Separate the queues so that WorkflowRuns and TaskRuns are processed independently
   */
  @Async
  public void retrieveAgentQueue() {
    String url = agentQueueURL.replace("{agentId}", agentId);
    while (true) {
      try {
        ResponseEntity<AgentQueueResponse> response =
            restTemplate.getForEntity(url, AgentQueueResponse.class);
        if (response.getStatusCode().is2xxSuccessful()) {
          if (response.getBody() != null && !response.getBody().getWorkflowRuns().isEmpty()) {
            List<WorkflowRun> workflowRuns = response.getBody().getWorkflowRuns();
            LOGGER.info("Received {} WorkflowRuns.", workflowRuns.size());
            workflowRuns.forEach((workflowRun) -> queueService.processWorkflowRun(workflowRun));
          } else {
            LOGGER.debug("No WorkflowRuns available.");
          }
          if (response.getBody() != null && !response.getBody().getTaskRuns().isEmpty()) {
            List<TaskRun> taskRuns = response.getBody().getTaskRuns();
            LOGGER.info("Received {} TaskRuns.", taskRuns.size());
            taskRuns.forEach((taskRun) -> queueService.processTaskRun(taskRun));
          } else {
            LOGGER.debug("No TaskRuns available.");
          }
        } else if (response.getStatusCodeValue() == 204) {
          LOGGER.debug("Queue returned 204 - No content.");
        }
      } catch (Exception e) {
        LOGGER.warn("Error retrieving queue: {}", e.getMessage());
      }
      try {
        Thread.sleep(HEARTBEAT_INTERVAL); // Heartbeat interval
      } catch (InterruptedException e) {
        LOGGER.error("Error sleeping queue check: {}", e.getMessage());
        Thread.currentThread().interrupt();
      }
    }
  }
}
