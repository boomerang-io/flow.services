package io.boomerang.core.message;

import io.boomerang.core.InternalController;
import io.boomerang.core.RelationshipService;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class MessageListener {
  private static final Logger LOGGER = LogManager.getLogger();

  @Value("${flow.broadcast.host:127.0.0.1}")
  private String broadcastHost;

  @Value("${server.port:80}")
  private String broadcastPort;

  private final RestTemplate restTemplate;

  private final RelationshipService relationshipService;

  private final InternalController internalController;

  public MessageListener(
      @Qualifier("insecureRestTemplate") RestTemplate restTemplate,
      RelationshipService relationshipService,
      InternalController internalController) {
    this.restTemplate = restTemplate;
    this.relationshipService = relationshipService;
    this.internalController = internalController;
  }

  /** This method will listen for messages being produced throughout the system. */
  @EventListener
  public void handleMessage(Message message) throws UnknownHostException {
    LOGGER.info(
        "MessageListener - Received message of type: {}", message.getMessage(), message.getType());
    InetAddress[] addresses = InetAddress.getAllByName(broadcastHost);
    for (InetAddress address : addresses) {
      String ip = address.getHostAddress();
      if (ip.equals(InetAddress.getLocalHost().getHostAddress()) || ip.equals("127.0.0.1")) {
        LOGGER.debug("MessageListener - Skipping broadcast to self: {}", ip);
        internalController.handleBroadcastMessage(message);
      } else {
        String url = "http://" + ip + ":" + broadcastPort + "/internal/broadcast";
        LOGGER.debug("MessageListener - Broadcasting to: {}", url);
        try {
          restTemplate.postForEntity(url, message, Void.class);
        } catch (ResourceAccessException rae) {
          LOGGER.fatal(
              "MessageListener - A fatal error has occurred while publishing the message! {}",
              rae.getMessage());
        }
      }
    }
  }
}
