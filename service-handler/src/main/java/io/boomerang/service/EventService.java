package io.boomerang.service;

import static io.cloudevents.core.CloudEventUtils.mapData;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.boomerang.client.EngineClient;
import io.boomerang.common.enums.RunPhase;
import io.boomerang.common.enums.RunStatus;
import io.boomerang.common.enums.TaskType;
import io.boomerang.common.enums.WorkflowStatus;
import io.boomerang.common.model.TaskRun;
import io.boomerang.common.model.TaskRunEndRequest;
import io.boomerang.common.model.WorkflowRun;
import io.boomerang.error.BoomerangException;
import io.boomerang.model.TaskResponse;
import io.boomerang.model.WorkflowSummary;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class EventService {

  @Autowired private WorkflowService workflowService;

  @Autowired private WorkspaceService workspaceService;

  @Autowired private TaskService taskService;

  @Autowired private EngineClient engineClient;

  private static final Logger logger = LogManager.getLogger(EventService.class);

  private static final String TYPE_PREFIX = "io.boomerang.event.status.";

  public ResponseEntity<?> process(CloudEvent event) {
    // Check if event that we support and return with accepted or rejected. Processing will be done
    // async.
    if (event.getType().startsWith(TYPE_PREFIX)) {
      processAsync(event);
    } else {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }
    logger.info("CloudEvent Processed.");
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  private Future<Boolean> processAsync(CloudEvent event) {
    Supplier<Boolean> supplier =
        () -> {
          Boolean isSuccess = Boolean.FALSE;
          String eventType = event.getType().substring(TYPE_PREFIX.length());
          logger.info("Event Type: " + eventType);
          ObjectMapper mapper = new ObjectMapper();
          switch (eventType.toLowerCase()) {
            case "workflowrun" -> {
              try {
                PojoCloudEventData<WorkflowRun> data =
                    mapData(event, PojoCloudEventDataMapper.from(mapper, WorkflowRun.class));
                WorkflowRun workflowRun = data.getValue();
                logger.info(workflowRun.toString());
                if (RunPhase.pending.equals(workflowRun.getPhase())
                    && RunStatus.ready.equals(workflowRun.getStatus())) {
                  logger.info("Executing WorkflowRun...");
                  // The execute is before communicating with the Engine
                  // as starting the workflow will kick off the first task(s) and
                  // dependencies at the workflow level (Workspaces) need to be there prior
                  workflowService.execute(workflowRun);
                  engineClient.startWorkflow(workflowRun.getId());
                } else if (RunPhase.completed.equals(workflowRun.getPhase())) {
                  logger.info("Finalizing WorkflowRun...");
                  workflowService.terminate(workflowRun);
                  engineClient.finalizeWorkflow(workflowRun.getId());
                }
                isSuccess = Boolean.TRUE;
              } catch (BoomerangException e) {
                logger.fatal("A fatal error has occurred while processing the message!", e);
                // TODO catch failure and end workflow with error status
              } catch (Exception e) {
                logger.fatal("A fatal error has occurred while processing the message!", e);
              }
            }
            case "taskrun" -> {
              try {
                PojoCloudEventData<TaskRun> data =
                    mapData(event, PojoCloudEventDataMapper.from(mapper, TaskRun.class));
                TaskRun taskRun = data.getValue();
                logger.info(taskRun.toString());
                try {
                  if ((TaskType.template.equals(taskRun.getType())
                          || TaskType.custom.equals(taskRun.getType())
                          || TaskType.script.equals(taskRun.getType()))
                      && RunPhase.pending.equals(taskRun.getPhase())
                      && RunStatus.ready.equals(taskRun.getStatus())) {
                    logger.info("Executing TaskRun...");
                    // Communicate the start with the Engine
                    // prior to Tekton starting as it is a blocking Watch call.
                    engineClient.startTask(taskRun.getId());
                    TaskResponse response = new TaskResponse();
                    response = taskService.execute(taskRun);
                    TaskRunEndRequest endRequest = new TaskRunEndRequest();
                    endRequest.setStatus(RunStatus.succeeded);
                    endRequest.setStatusMessage(response.getMessage());
                    endRequest.setResults(response.getResults());
                    engineClient.endTask(taskRun.getId(), endRequest);
                  } else if ((TaskType.template.equals(taskRun.getType())
                          || TaskType.custom.equals(taskRun.getType())
                          || TaskType.script.equals(taskRun.getType()))
                      && RunPhase.completed.equals(taskRun.getPhase())
                      && (RunStatus.cancelled.equals(taskRun.getStatus())
                          || RunStatus.timedout.equals(taskRun.getStatus()))) {
                    logger.info("Cancelling TaskRun...");
                    taskService.terminate(taskRun);
                  } else {
                    logger.info(
                        "Skipping TaskRun as criteria not met; (Type: template, custom, or script), (Status: ready, cancelled, timedout), and (Phase: pending, completed).");
                  }
                  isSuccess = Boolean.TRUE;
                } catch (BoomerangException e) {
                  logger.fatal("Failed to execute TaskRun.", e);
                  TaskRunEndRequest endRequest = new TaskRunEndRequest();
                  endRequest.setStatus(RunStatus.failed);
                  endRequest.setStatusMessage(e.getMessage());
                  engineClient.endTask(taskRun.getId(), endRequest);
                }
              } catch (Exception e) {
                logger.fatal("A fatal error has occurred while processing the message!", e);
              }
            }
            case "workflow" -> {
              try {
                PojoCloudEventData<WorkflowSummary> data =
                    mapData(event, PojoCloudEventDataMapper.from(mapper, WorkflowSummary.class));
                WorkflowSummary workflow = data.getValue();
                logger.info(workflow.toString());
                // TODO: update this to work. Currently a deleted event does not get sent through
                if (WorkflowStatus.inactive.equals(workflow.getStatus())) {
                  workspaceService.delete("workflow", workflow.getId());
                }
              } catch (Exception e) {
                logger.fatal("A fatal error has occurred while processing the message!", e);
              }
            }
          }
          return isSuccess;
        };

    return CompletableFuture.supplyAsync(supplier);
  }

  // public ResponseEntity<CloudEvent<AttributesImpl, JsonNode>> routeWebhookEvent(String token,
  // String requestUri, String trigger, String workflowId, JsonNode payload,
  // String workflowActivityId, String topic, String status) {
  //
  // // Validate Token and WorkflowID. Do first.
  // HttpStatus accessStatus = checkAccess(workflowId, token);
  //
  // if (accessStatus != HttpStatus.OK) {
  // return ResponseEntity.status(accessStatus).build();
  // }
  //
  // final String eventId = UUID.randomUUID().toString();
  // final String eventType = TYPE_PREFIX + trigger;
  // final URI uri = URI.create(requestUri);
  // String subject = "/" + workflowId;
  //
  // // Validate WFE Attributes
  // if ("wfe".equals(trigger) && workflowActivityId != null) {
  // subject = subject + "/" + workflowActivityId + "/" + topic;
  // } else if ("wfe".equals(trigger)) {
  //
  // // WFE requires workflowActivityId
  // return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  // }
  //
  // if (!"failure".equals(status)) {
  // status = "success";
  // }
  // CustomAttributeExtension statusCAE = new CustomAttributeExtension("status", status);
  //
  //    // @formatter:off
  //    final CloudEventImpl<JsonNode> cloudEvent = CloudEventBuilder.<JsonNode>builder()
  //        .withType(eventType)
  //        .withExtension(statusCAE)
  //        .withId(eventId)
  //        .withSource(uri)
  //        .withData(payload)
  //        .withSubject(subject)
  //        .withTime(ZonedDateTime.now())
  //        .build();
  //    // @formatter:on
  //
  // logger.debug("routeWebhookEvent() - CloudEvent: " + cloudEvent);
  //
  // forwardCloudEvent(cloudEvent);
  //
  // return ResponseEntity.ok().body(cloudEvent);
  // }
  //
  // @Override
  // public ResponseEntity<CloudEvent<AttributesImpl, JsonNode>> routeCloudEvent(
  // CloudEvent<AttributesImpl, JsonNode> cloudEvent, String token, URI uri) {
  //
  // // Validate Token and WorkflowID. Do first.
  // String subject = cloudEvent.getAttributes().getSubject().orElse("");
  //
  // if (!subject.startsWith("/") || cloudEvent.getData().isEmpty()) {
  // return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  // }
  //
  // HttpStatus accessStatus = checkAccess(getWorkflowIdFromSubject(subject), token);
  // if (accessStatus != HttpStatus.OK) {
  // return ResponseEntity.status(accessStatus).build();
  // }
  //
  // logger.debug("routeCloudEvent() - CloudEvent Attributes: " + cloudEvent.getAttributes());
  // logger.debug("routeCloudEvent() - CloudEvent Data: " + cloudEvent.getData().get());
  //
  // String eventId = UUID.randomUUID().toString();
  // String eventType = TYPE_PREFIX + "custom";
  //
  // String status = "success";
  // if (cloudEvent.getExtensions() != null && cloudEvent.getExtensions().containsKey("status")) {
  // String statusExtension = cloudEvent.getExtensions().get("status").toString();
  // if ("failure".equals(statusExtension)) {
  // status = statusExtension;
  // }
  // }
  // CustomAttributeExtension statusCAE = new CustomAttributeExtension("status", status);
  //
  //    // @formatter:off
  //    final CloudEventImpl<JsonNode> forwardedCloudEvent = CloudEventBuilder.<JsonNode>builder()
  //        .withType(eventType)
  //        .withExtension(statusCAE)
  //        .withId(eventId)
  //        .withSource(uri)
  //        .withData(cloudEvent.getData().get())
  //        .withSubject(subject)
  //        .withTime(ZonedDateTime.now())
  //        .build();
  //    // @formatter:on
  //
  // forwardCloudEvent(forwardedCloudEvent);
  //
  // return ResponseEntity.ok().body(forwardedCloudEvent);
  // }
  //
  // private HttpStatus checkAccess(String workflowId, String token) {
  // if (authorizationEnabled) {
  // logger.debug("checkAccess() - Token: " + token);
  //
  // if (token != null && !token.isEmpty() && workflowId != null && !workflowId.isEmpty()) {
  // return workflowClient.validateWorkflowToken(workflowId, token);
  // } else {
  // logger.error("checkAccess() - Error: no token provided.");
  // return HttpStatus.UNAUTHORIZED;
  // }
  // } else {
  // return HttpStatus.OK;
  // }
  // }
  //
  // private String getWorkflowIdFromSubject(String subject) {
  // // Reference 0 will be an empty string as it is the left hand side of the split
  // String[] splitArr = subject.split("/");
  // if (splitArr.length >= 2) {
  // return splitArr[1].toString();
  // } else {
  // logger.error("processCloudEvent() - Error: No workflow ID found in event");
  // return "";
  // }
  // }
  //
  // private void forwardCloudEvent(CloudEventImpl<JsonNode> cloudEvent) {
  //
  // // If eventing is enabled, try to send the cloud event to it
  // try {
  // pubOnlyTunnel.orElseThrow().publish(jetstreamStreamSubject, Json.encode(cloudEvent));
  //
  // } catch (Exception e) {
  //
  // // The code will get to this point only if eventing is disabled or if it
  // // for some reason it failed to publish the message
  // workflowClient.executeWorkflowPut(cloudEvent);
  // }
  // }
}
