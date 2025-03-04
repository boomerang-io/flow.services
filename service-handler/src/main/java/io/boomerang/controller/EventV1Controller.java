package io.boomerang.controller;

import io.boomerang.service.EventService;
import io.cloudevents.CloudEvent;
import io.cloudevents.spring.http.CloudEventHttpUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class EventV1Controller {
  Logger logger = LogManager.getLogger(EventV1Controller.class);

  @Autowired private EventService eventService;

  /**
   * Accepts any JSON Cloud Event. This will map to the custom trigger but the topic will come from
   * the CloudEvent subject.
   *
   * <p>ce attributes are in the body
   *
   * @see https://github.com/cloudevents/spec/blob/v1.0/json-format.md
   * @see https://github.com/cloudevents/spec/blob/v1.0/http-protocol-binding.md
   */
  @PostMapping(value = "/event", consumes = "application/cloudevents+json; charset=utf-8")
  public ResponseEntity<?> accept(@RequestBody CloudEvent event) {
    logger.info(event.toString());

    return eventService.process(event);
  }

  /** Accepts a Cloud Event with ce attributes are in the header */
  @PostMapping("/event")
  public ResponseEntity<?> acceptEvent(
      @RequestHeader HttpHeaders headers, @RequestBody String data) {
    CloudEvent event = CloudEventHttpUtils.toReader(headers, () -> data.getBytes()).toEvent();
    logger.info(event.toString());

    return eventService.process(event);
  }
}
