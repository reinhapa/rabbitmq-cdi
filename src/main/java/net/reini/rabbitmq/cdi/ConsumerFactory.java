package net.reini.rabbitmq.cdi;

import java.util.function.Supplier;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

/**
 * Factory responsible creating {@link Consumer} instances.
 *
 * @author Patrick Reinhart
 */
class ConsumerFactory {
  /**
   * Creates a simple consumer that does not acknowledge the message received.
   * 
   * @param consumer the event consumer
   * @return the message consumer instance
   */
  public Consumer create(EventConsumer<?> consumer) {
    return ConsumerImpl.create(consumer);
  }

  /**
   * Creates a acknowledge aware message consumer that only do acknowledge messages when the event
   * has been sent successfully.
   * 
   * @param consumer the event consumer
   * @param channelSupplier the supplier for a open channel
   * @return the message consumer instance
   */
  public Consumer createAcknowledged(EventConsumer<?> consumer, Supplier<Channel> channelSupplier) {
    return ConsumerImpl.createAcknowledged(consumer, channelSupplier);
  }
}
