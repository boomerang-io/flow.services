package io.boomerang.core.message;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisher {

  private final ApplicationEventPublisher eventPublisher;

  public MessagePublisher(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  public void publishEvent(String type, String message) {
    eventPublisher.publishEvent(new Message(type, message));
  }
}
