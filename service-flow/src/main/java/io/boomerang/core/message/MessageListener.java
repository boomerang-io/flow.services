package io.boomerang.core.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {
  private static final Logger LOGGER = LogManager.getLogger();

  @Value("${flow.broadcast.host}")
  private String broadcastHost;

  public MessageListener() {}

  @EventListener
  public void handleGraphEvent(Message message) {
    LOGGER.info("-----> Broadcast host: {}", broadcastHost);
    LOGGER.info("Received message: {} of type: {}", message.getMessage(), message.getType());
  }
}
