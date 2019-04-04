package net.reini.rabbitmq.cdi;

import java.io.IOException;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class EventConsumer implements EnvelopeConsumer {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

  private final Decoder<?> decoder;
  private final Event<Object> eventControl;
  private final Instance<Object> eventPool;

  EventConsumer(Decoder<?> decoder, Event<Object> eventControl, Instance<Object> eventPool) {
    this.decoder = decoder;
    this.eventControl = eventControl;
    this.eventPool = eventPool;
  }

  /**
   * Builds a CDI event from a message. The CDI event instance is retrieved from the injection
   * container.
   *
   * @param messageBody The message
   * @return The CDI event
   */
  Object buildEvent(byte[] messageBody) {
    Object event;
    try {
      event = decoder.decode(messageBody);
    } catch (DecodeException e) {
      LOGGER.error("Unable to read decode event from message: ".concat(new String(messageBody)), e);
      event = eventPool.get();
    }
    return event;
  }

  @Override
  public boolean consume(String consumerTag, Envelope envelope, BasicProperties properties,
      byte[] body) throws IOException {
    LOGGER.debug("Handle delivery: consumerTag: {}, envelope: {}, properties: {}", consumerTag,
        envelope, properties);
    String contentType = properties.getContentType();
    if (decoder.willDecode(contentType)) {
      Object event = buildEvent(body);
      try {
        eventControl.fire(event);
        LOGGER.trace("successfully fired event: {}", event);
        return true;
      } catch (Exception e) {
        LOGGER.error("Failed to fire event: {}", event, e);
        return false;
      }
    } else {
      LOGGER.error("Unable to process unknown message content type: {}", contentType);
      return false;
    }
  }
}
