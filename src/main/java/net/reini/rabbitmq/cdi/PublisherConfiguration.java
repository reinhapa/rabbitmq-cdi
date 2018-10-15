package net.reini.rabbitmq.cdi;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;

/**
 * A publisher configuration stores all important settings and options used for publishing and
 * event.
 *
 * @author Patrick Reinhart
 */
final class PublisherConfiguration {
  private final ConnectionConfig config;
  private final BasicProperties basicProperties;
  private final Encoder<?> messageEncoder;
  private final String exchange;
  private final String routingKey;

  PublisherConfiguration(ConnectionConfig config, String exchange, String routingKey,
      Builder basicPropertiesBuilder,
      Encoder<?> encoder) {
    this.config = config;
    this.exchange = exchange;
    this.routingKey = routingKey;
    this.messageEncoder = encoder;
    String contentType = messageEncoder.contentType();
    if (contentType != null) {
      basicPropertiesBuilder.contentType(contentType);
    }
    basicProperties = basicPropertiesBuilder.build();
  }

  /**
   * @return the connection configuration
   */
  ConnectionConfig getConfig() {
    return config;
  }

  @Override
  public String toString() {
    return config.toString();
  }

  void publish(Channel channel, Object event) throws EncodeException, IOException {
    @SuppressWarnings("unchecked")
    byte[] data = ((Encoder<Object>) messageEncoder).encode(event);
    channel.basicPublish(exchange, routingKey, basicProperties, data);
  }
}
