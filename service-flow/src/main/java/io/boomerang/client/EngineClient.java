package io.boomerang.client;

import io.boomerang.common.model.TaskRun;
import io.boomerang.common.model.TaskRunEndRequest;
import io.boomerang.common.model.WorkflowRun;
import io.boomerang.common.model.WorkflowRunCount;
import io.boomerang.common.model.WorkflowRunInsight;
import io.boomerang.common.model.WorkflowRunRequest;
import io.boomerang.common.model.WorkflowSubmitRequest;
import io.boomerang.error.BoomerangException;
import io.boomerang.workflow.model.WorkflowRunEventRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Primary
public class EngineClient {

  private static final Logger LOGGER = LogManager.getLogger(EngineClient.class);

  @Value("${flow.engine.workflowrun.query.url}")
  public String queryWorkflowRunURL;

  @Value("${flow.engine.workflowrun.insight.url}")
  public String insightWorkflowRunURL;

  @Value("${flow.engine.workflowrun.count.url}")
  public String countWorkflowRunURL;

  @Value("${flow.engine.workflowrun.get.url}")
  public String getWorkflowRunURL;

  @Value("${flow.engine.workflowrun.start.url}")
  public String startWorkflowRunURL;

  @Value("${flow.engine.workflowrun.finalize.url}")
  public String finalizeWorkflowRunURL;

  @Value("${flow.engine.workflowrun.cancel.url}")
  public String cancelWorkflowRunURL;

  @Value("${flow.engine.workflowrun.retry.url}")
  public String retryWorkflowRunURL;

  @Value("${flow.engine.workflowrun.delete.url}")
  public String deleteWorkflowRunURL;

  @Value("${flow.engine.workflowrun.event.url}")
  public String eventWorkflowRunURL;

  @Value("${flow.engine.workflow.get.url}")
  public String getWorkflowURL;

  @Value("${flow.engine.workflow.query.url}")
  public String queryWorkflowURL;

  @Value("${flow.engine.workflow.count.url}")
  public String countWorkflowURL;

  @Value("${flow.engine.workflow.create.url}")
  public String createWorkflowURL;

  @Value("${flow.engine.workflow.apply.url}")
  public String applyWorkflowURL;

  @Value("${flow.engine.workflow.submit.url}")
  public String submitWorkflowURL;

  @Value("${flow.engine.workflow.changelog.url}")
  public String changelogWorkflowURL;

  @Value("${flow.engine.workflow.enable.url}")
  public String enableWorkflowURL;

  @Value("${flow.engine.workflow.disable.url}")
  public String disableWorkflowURL;

  @Value("${flow.engine.workflow.delete.url}")
  public String deleteWorkflowURL;

  @Value("${flow.engine.taskrun.get.url}")
  public String getTaskRunURL;

  @Value("${flow.engine.taskrun.end.url}")
  public String endTaskRunURL;

  @Value("${flow.engine.taskrun.logstream.url}")
  private String logStreamTaskRunURL;

  @Value("${flow.engine.task.get.url}")
  public String getTaskURL;

  @Value("${flow.engine.task.query.url}")
  public String queryTaskURL;

  @Value("${flow.engine.task.create.url}")
  public String createTaskURL;

  @Value("${flow.engine.task.apply.url}")
  public String applyTaskURL;

  @Value("${flow.engine.task.changelog.url}")
  public String changelogTaskURL;

  @Value("${flow.engine.task.delete.url}")
  public String deleteTaskURL;

  @Value("${flow.engine.workflowtemplate.get.url}")
  public String getWorkflowTemplateURL;

  @Value("${flow.engine.workflowtemplate.query.url}")
  public String queryWorkflowTemplateURL;

  @Value("${flow.engine.workflowtemplate.create.url}")
  public String createWorkflowTemplateURL;

  @Value("${flow.engine.workflowtemplate.apply.url}")
  public String applyWorkflowTemplateURL;

  @Value("${flow.engine.workflowtemplate.delete.url}")
  public String deleteWorkflowTemplateURL;

  @Autowired
  @Qualifier("internalRestTemplate")
  public RestTemplate restTemplate;

  /*
   * ************************************** WorkflowRun endpoints
   * **************************************
   */
  public WorkflowRun getWorkflowRun(String workflowRunId, boolean withTasks) {
    try {
      String url = getWorkflowRunURL.replace("{workflowRunId}", workflowRunId);
      Map<String, String> requestParams = new HashMap<>();
      requestParams.put("withTasks", Boolean.toString(withTasks));

      String encodedURL =
          requestParams.keySet().stream()
              .map(key -> key + "=" + requestParams.get(key))
              .collect(Collectors.joining("&", url + "?", ""));

      LOGGER.info("URL: " + encodedURL);

      ResponseEntity<WorkflowRun> response =
          restTemplate.getForEntity(encodedURL, WorkflowRun.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public WorkflowRunResponsePage queryWorkflowRuns(
      Optional<Long> fromDate,
      Optional<Long> toDate,
      Optional<Integer> queryLimit,
      Optional<Integer> queryPage,
      Optional<Direction> querySort,
      Optional<List<String>> queryLabels,
      Optional<List<String>> queryStatus,
      Optional<List<String>> queryPhase,
      Optional<List<String>> queryWorkflowRuns,
      Optional<List<String>> queryWorkflows,
      Optional<List<String>> queryTriggers) {
    try {
      UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(queryWorkflowRunURL);
      if (queryPage.isPresent()) {
        urlBuilder.queryParam("page", Integer.toString(queryPage.get()));
      }
      if (queryLimit.isPresent()) {
        urlBuilder.queryParam("limit", Integer.toString(queryLimit.get()));
      }
      if (querySort.isPresent()) {
        urlBuilder.queryParam("sort", querySort.get());
      }
      if (fromDate.isPresent()) {
        urlBuilder.queryParam("fromDate", fromDate.get());
      }
      if (toDate.isPresent()) {
        urlBuilder.queryParam("toDate", toDate.get());
      }
      if (queryLabels.isPresent()) {
        urlBuilder.queryParam("labels", queryLabels.get());
      }
      if (queryStatus.isPresent()) {
        urlBuilder.queryParam("status", queryStatus.get());
      }
      if (queryPhase.isPresent()) {
        urlBuilder.queryParam("phase", queryPhase.get());
      }
      if (queryWorkflowRuns.isPresent() && !queryWorkflowRuns.get().isEmpty()) {
        urlBuilder.queryParam("workflowruns", queryWorkflowRuns.get());
      }
      if (queryWorkflows.isPresent() && !queryWorkflows.get().isEmpty()) {
        urlBuilder.queryParam("workflows", queryWorkflows.get());
      }
      if (queryTriggers.isPresent() && !queryTriggers.get().isEmpty()) {
        urlBuilder.queryParam("triggers", queryTriggers.get());
      }
      URI encodedURI = urlBuilder.build().encode().toUri();

      LOGGER.info("Query URL: " + encodedURI);
      // final HttpHeaders headers = new HttpHeaders();
      // headers.setContentType(MediaType.APPLICATION_JSON);
      // HttpEntity<String> entity = new HttpEntity<String>("{}", headers);
      // ResponseEntity<Page<WorkflowRunEntity>> response =
      // restTemplate.exchange(encodedURL, HttpMethod.GET, null, Page.class);

      ResponseEntity<WorkflowRunResponsePage> response =
          restTemplate.getForEntity(encodedURI, WorkflowRunResponsePage.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().getContent().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public WorkflowRunInsight insightWorkflowRuns(
      Optional<List<String>> queryLabels,
      Optional<List<String>> queryWorkflowRuns,
      Optional<List<String>> queryWorkflows,
      Optional<Long> fromDate,
      Optional<Long> toDate) {
    try {
      UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(insightWorkflowRunURL);
      if (fromDate.isPresent()) {
        urlBuilder.queryParam("fromDate", fromDate.get());
      }
      if (toDate.isPresent()) {
        urlBuilder.queryParam("toDate", toDate.get());
      }
      if (queryLabels.isPresent()) {
        urlBuilder.queryParam("labels", queryLabels.get());
      }
      if (queryWorkflowRuns.isPresent() && !queryWorkflowRuns.get().isEmpty()) {
        urlBuilder.queryParam("workflowruns", queryWorkflowRuns.get());
      }
      if (queryWorkflows.isPresent() && !queryWorkflows.get().isEmpty()) {
        urlBuilder.queryParam("workflows", queryWorkflows.get());
      }
      URI encodedURI = urlBuilder.build().encode().toUri();

      LOGGER.info("Query URL: " + encodedURI);

      ResponseEntity<WorkflowRunInsight> response =
          restTemplate.getForEntity(encodedURI, WorkflowRunInsight.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public WorkflowRunCount countWorkflowRuns(
      Optional<List<String>> queryLabels,
      Optional<List<String>> queryWorkflows,
      Optional<Long> fromDate,
      Optional<Long> toDate) {
    try {
      UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(countWorkflowRunURL);
      if (fromDate.isPresent()) {
        urlBuilder.queryParam("fromDate", fromDate.get());
      }
      if (toDate.isPresent()) {
        urlBuilder.queryParam("toDate", toDate.get());
      }
      if (queryLabels.isPresent()) {
        urlBuilder.queryParam("labels", queryLabels.get());
      }
      if (queryWorkflows.isPresent() && !queryWorkflows.get().isEmpty()) {
        urlBuilder.queryParam("workflows", queryWorkflows.get());
      }
      URI encodedURI = urlBuilder.build().encode().toUri();

      LOGGER.info("Query URL: " + encodedURI);

      ResponseEntity<WorkflowRunCount> response =
          restTemplate.getForEntity(encodedURI, WorkflowRunCount.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public WorkflowRun startWorkflowRun(String workflowRunId, Optional<WorkflowRunRequest> request) {
    try {
      String url = startWorkflowRunURL.replace("{workflowRunId}", workflowRunId);

      LOGGER.info("URL: " + url);

      ResponseEntity<WorkflowRun> response =
          restTemplate.postForEntity(url, request, WorkflowRun.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public WorkflowRun finalizeWorkflowRun(String workflowRunId) {
    try {
      String url = finalizeWorkflowRunURL.replace("{workflowRunId}", workflowRunId);

      LOGGER.info("URL: " + url);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>("{}", headers);
      ResponseEntity<WorkflowRun> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, WorkflowRun.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public WorkflowRun cancelWorkflowRun(String workflowRunId) {
    try {
      String url = cancelWorkflowRunURL.replace("{workflowRunId}", workflowRunId);

      LOGGER.info("URL: " + url);
      ResponseEntity<WorkflowRun> response =
          restTemplate.exchange(url, HttpMethod.DELETE, null, WorkflowRun.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public WorkflowRun retryWorkflowRun(String workflowRunId) {
    try {
      String url = retryWorkflowRunURL.replace("{workflowRunId}", workflowRunId);

      LOGGER.info("URL: " + url);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>("{}", headers);
      ResponseEntity<WorkflowRun> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, WorkflowRun.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public void deleteWorkflowRun(String workflowRunId) {
    try {
      String url = deleteWorkflowRunURL.replace("{workflowRunId}", workflowRunId);

      LOGGER.info("URL: " + url);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public void eventWorkflowRun(String workflowRunId, WorkflowRunEventRequest request) {
    try {
      String url = eventWorkflowRunURL.replace("{workflowRunId}", workflowRunId);

      LOGGER.info("Query URL: " + url);
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<WorkflowRunEventRequest> entity =
          new HttpEntity<WorkflowRunEventRequest>(request, headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /*
   * ************************************** Workflow endpoints
   * **************************************
   */
  public WorkflowRun submitWorkflow(
      String workflowId, WorkflowSubmitRequest request, boolean start) {
    try {
      String url = submitWorkflowURL.replace("{workflowId}", workflowId);
      Map<String, String> requestParams = new HashMap<>();
      requestParams.put("start", Boolean.toString(start));
      String encodedURL =
          requestParams.keySet().stream()
              .map(key -> key + "=" + requestParams.get(key))
              .collect(Collectors.joining("&", url + "?", ""));

      LOGGER.info("URL: " + encodedURL);

      ResponseEntity<WorkflowRun> response =
          restTemplate.postForEntity(encodedURL, request, WorkflowRun.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /*
   * ************************************** TaskRun endpoints **************************************
   */
  public TaskRun getTaskRun(String taskRunId) {
    try {
      String url = getTaskRunURL.replace("{taskRunId}", taskRunId);
      LOGGER.info("URL: " + url);

      ResponseEntity<TaskRun> response = restTemplate.getForEntity(url, TaskRun.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public TaskRun endTaskRun(String taskRunId, TaskRunEndRequest request) {
    try {
      String url = endTaskRunURL.replace("{taskRunId}", taskRunId);
      LOGGER.info("URL: " + url);

      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<TaskRunEndRequest> entity = new HttpEntity<TaskRunEndRequest>(request, headers);
      ResponseEntity<TaskRun> response =
          restTemplate.exchange(url, HttpMethod.PUT, entity, TaskRun.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(
          ex,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(),
          "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public StreamingResponseBody streamTaskRunLog(String taskRunId) {
    String url = logStreamTaskRunURL.replace("{taskRunId}", taskRunId);
    LOGGER.info("URL: " + url);

    return outputStream -> {
      RequestCallback requestCallback =
          request ->
              request
                  .getHeaders()
                  .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
      PrintWriter printWriter = new PrintWriter(outputStream);
      ResponseExtractor<Void> responseExtractor = getResponseExtractor(outputStream, printWriter);
      LOGGER.info("Starting TaskRun[{}] log stream...", taskRunId);
      try {
        restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
      } catch (Exception ex) {
        LOGGER.error(ex.toString());
        throw new BoomerangException(
            ex,
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getClass().getSimpleName(),
            "Exception in communicating with internal services.",
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
      LOGGER.info("Finished TaskRun[{}] log stream.", taskRunId);
    };
  }

  private ResponseExtractor<Void> getResponseExtractor(
      OutputStream outputStream, PrintWriter printWriter) {
    return restTemplateResponse -> {
      InputStream is = restTemplateResponse.getBody();
      int nRead;
      byte[] data = new byte[1024];
      while ((nRead = is.read(data, 0, data.length)) != -1) {
        outputStream.write(data, 0, nRead);
      }
      return null;
    };
  }
}
