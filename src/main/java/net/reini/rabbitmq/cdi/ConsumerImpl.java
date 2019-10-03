package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.function.Supplier;

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

  static Consumer createAcknowledged(EnvelopeConsumer consumer, Supplier<Channel> channelSupplier) {
    return create((consumerTag, envelope, properties, body) -> acknowledgedConsume(consumer,
        channelSupplier, consumerTag, envelope, properties, body));
  }

  static Consumer create(EnvelopeConsumer consumer) {
    return new ConsumerImpl(consumer);
  }

  static boolean acknowledgedConsume(EnvelopeConsumer consumer, Supplier<Channel> channelSupplier,
      String consumerTag,
      Envelope envelope, BasicProperties properties, byte[] body) {
    long deliveryTag = envelope.getDeliveryTag();
    try {
      if (consumer.consume(consumerTag, envelope, properties, body)) {
        channelSupplier.get().basicAck(deliveryTag, false);
        LOGGER.debug("Acknowledged {}", envelope);
        return true;
      }
      channelSupplier.get().basicNack(deliveryTag, false, false);
      LOGGER.debug("Not acknowledged {}", envelope);
    } catch (IOException e) {
      LOGGER.warn("Consume failed for {}", envelope, e);
      nackWithRequeue(channelSupplier.get(), envelope, deliveryTag);
    }
    return false;
  }

  static void nackWithRequeue(Channel channel, Envelope envelope, long deliveryTag) {
    try {
      channel.basicNack(deliveryTag, false, true);
      LOGGER.debug("Not acknowledged {} (re-queue)", envelope);
    } catch (IOException e) {
      LOGGER.error("Unable to not acknowledge {} for re-queue", envelope, e);
    }
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
