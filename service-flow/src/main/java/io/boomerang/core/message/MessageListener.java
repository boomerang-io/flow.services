package io.boomerang.core.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
  public void handleGraphEvent(Message message) throws UnknownHostException {
    LOGGER.info("Received message: {} of type: {}", message.getMessage(), message.getType());
    LOGGER.info("-----> Broadcast host: {}", broadcastHost);
    InetAddress[] addresses = InetAddress.getAllByName(broadcastHost);
    for (InetAddress address : addresses) {
      String ip = address.getHostAddress();
      String url = "http://" + ip + ":8080/your-endpoint";
      LOGGER.info("Sending message to: {}", url);
    }
  }
}
