package io.boomerang.core.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class MessageListener {
  private static final Logger LOGGER = LogManager.getLogger();

  @Value("${flow.broadcast.host}")
  private String broadcastHost;

  private final RestTemplate restTemplate;

  public MessageListener(@Qualifier("insecureRestTemplate") RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * This method will listen for messages being produced throughout the system.
   *
   * <p>TODO: wrap in a mode, if a particular mode is enabled i.e. standalone, then just do the
   * action, if not broadcast
   */
  @EventListener
  public void handleMessage(Message message) throws UnknownHostException {
    LOGGER.info("Received message: {} of type: {}", message.getMessage(), message.getType());
    LOGGER.info("-----> Broadcast host: {}", broadcastHost);
    InetAddress[] addresses = InetAddress.getAllByName(broadcastHost);
    for (InetAddress address : addresses) {
      String ip = address.getHostAddress();
      String url = "http://" + ip + ":80/internal/broadcast";
      LOGGER.info("Broadcasting message to: {}", url);
      try {
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity(url, message, Void.class);
        LOGGER.debug("httpSink() - Status Code: " + responseEntity.getStatusCode());
        if (responseEntity.getBody() != null) {
          LOGGER.debug("httpSink() - Body: " + responseEntity.getBody().toString());
        }
      } catch (ResourceAccessException rae) {
        LOGGER.fatal("A fatal error has occurred while publishing the message!", rae.getMessage());
        // eventRepository.save(new EventQueueEntity(sinkUrl, req));
      }
    }
  }
}
