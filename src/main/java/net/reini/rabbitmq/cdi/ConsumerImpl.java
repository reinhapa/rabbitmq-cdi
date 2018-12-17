package net.reini.rabbitmq.cdi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Consumer implementation with and without acknowledged behavior.
 *
 * @author Patrick Reinhart
 */
final class ConsumerImpl implements Consumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerImpl.class);

  private final EnvelopeConsumer envelopeConsumer;

  static Consumer createAcknowledged(EnvelopeConsumer consumer, Channel channel) {
    return create((consumerTag, envelope, properties, body) -> acknowledgedConsume(consumer,
        channel, consumerTag, envelope, properties, body));
  }

  static Consumer create(EnvelopeConsumer consumer) {
    return new ConsumerImpl(consumer);
  }

  static boolean acknowledgedConsume(EnvelopeConsumer consumer, Channel channel, String consumerTag,
      Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
    long deliveryTag = envelope.getDeliveryTag();
    try {
      if (consumer.consume(consumerTag, envelope, properties, body)) {
        channel.basicAck(deliveryTag, false);
        LOGGER.debug("Acknowledged {}", envelope);
        return true;
      }
      channel.basicNack(deliveryTag, false, false);
      LOGGER.debug("Not acknowledged {}", envelope);
    } catch (IOException e) {
      LOGGER.warn("Consume failed for {}", envelope, e);
      channel.basicNack(deliveryTag, false, true);
      LOGGER.debug("Not acknowledged {} (re-queue)", envelope);
    }
    return false;
  }

  private ConsumerImpl(EnvelopeConsumer envelopeConsumer) {
    this.envelopeConsumer = envelopeConsumer;
  }

  @Override
  public void handleConsumeOk(String consumerTag) {
  }

  @Override
  public void handleCancelOk(String consumerTag) {
  }

  @Override
  public void handleCancel(String consumerTag) throws IOException {
  }

  @Override
  public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
  }

  @Override
  public void handleRecoverOk(String consumerTag) {
  }


  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
      byte[] body) throws IOException {
    envelopeConsumer.consume(consumerTag, envelope, properties, body);
  }
}
