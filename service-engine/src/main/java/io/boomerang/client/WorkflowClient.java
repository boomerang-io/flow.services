package io.boomerang.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import io.boomerang.error.BoomerangException;
import io.boomerang.engine.model.WorkflowSchedule;

@Service
@Primary
public class WorkflowClient {

  private static final Logger LOGGER = LogManager.getLogger();

  @Value("${flow.workflow.paramlayers.url}")
  private String workflowParamsURL;
  
  @Value("${flow.workflow.createschedule.url}")
  private String workflowCreateScheduleURL;

  @Autowired
  @Qualifier("insecureRestTemplate")
  public RestTemplate restTemplate;

  public WorkflowSchedule createSchedule(WorkflowSchedule workflowSchedule) {
    try {
      LOGGER.info("URL: " + workflowCreateScheduleURL);

      ResponseEntity<WorkflowSchedule> response = restTemplate.postForEntity(workflowCreateScheduleURL, workflowSchedule, WorkflowSchedule.class);

      LOGGER.info("Status Response: " + response.getStatusCode());
      LOGGER.info("Content Response: " + response.getBody().toString());

      return response.getBody();
    } catch (RestClientException ex) {
      LOGGER.error(ex.toString());
      throw new BoomerangException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ex.getClass().getSimpleName(), "Exception in communicating with internal services.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
