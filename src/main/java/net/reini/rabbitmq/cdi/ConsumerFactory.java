package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

class ConsumerFactory {
  public Consumer create(EventConsumer consumer) {
    return ConsumerImpl.create(consumer);
  }

  public Consumer createAcknowledged(EventConsumer consumer, Channel channel) {
    return ConsumerImpl.createAcknowledged(consumer, channel);
  }
}