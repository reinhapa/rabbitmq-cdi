package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;

/**
 * A publisher configuration stores all important settings and options used for publishing and
 * event.
 *
 * @author Patrick Reinhart
 */
final class PublisherConfiguration<T> implements BiConsumer<T, PublishException> {
  private final ConnectionConfig config;
  private final BasicProperties basicProperties;
  private final Encoder<T> messageEncoder;
  private final String exchange;
  private final Function<T, String> routingKeyFunction;
  private final BiConsumer<T, PublishException> errorHandler;
  private final List<ExchangeDeclaration> declarations;

  PublisherConfiguration(ConnectionConfig config, String exchange,
      Function<T, String> routingKeyFunction,
      Builder basicPropertiesBuilder, Encoder<T> encoder,
      BiConsumer<T, PublishException> errorHandler, List<ExchangeDeclaration> declarations) {
    this.config = config;
    this.exchange = exchange;
    this.routingKeyFunction = routingKeyFunction;
    this.messageEncoder = encoder;
    this.errorHandler = errorHandler;
    this.declarations = declarations;
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

  List<ExchangeDeclaration> getDeclarations() {
    return declarations;
  }

  @Override
  public String toString() {
    return config.toString();
  }

  void publish(Channel channel, T event) throws EncodeException, IOException {
    byte[] data = messageEncoder.encode(event);
    channel.basicPublish(exchange, routingKeyFunction.apply(event), basicProperties, data);
  }

  @Override
  public void accept(T event, PublishException publishError) {
    errorHandler.accept(event, publishError);
  }
}
