package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static net.reini.rabbitmq.cdi.ConsumerImpl.create;
import static net.reini.rabbitmq.cdi.ConsumerImpl.createAcknowledged;

class ConsumerHolder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerHolder.class);
  private final boolean autoAck;
  private final String queueName;

  private volatile boolean active;
  private final Object activeLock;
  private final EventConsumer consumer;
  private final ShutdownListener shutdownListener;
  private Channel channel;
  private final ConsumerChannelFactory consumerChannelFactory;
  private final ConsumerExchangeAndQueueDeclarer consumerExchangeAndQueueDeclarer;
  private final ResourceCloser resourceCloser;

  ConsumerHolder(EventConsumer consumer, String queueName, boolean autoAck, ConsumerChannelFactory consumerChannelFactory, ConsumerExchangeAndQueueDeclarer consumerExchangeAndQueueDeclarer) {
    this.consumer = consumer;
    this.queueName = queueName;
    this.autoAck = autoAck;
    this.consumerChannelFactory = consumerChannelFactory;
    this.consumerExchangeAndQueueDeclarer = consumerExchangeAndQueueDeclarer;
    this.activeLock = new Object();
    this.resourceCloser = new ResourceCloser();
    shutdownListener = new ConsumerHolderChannelShutdownListener(this);
  }

  void deactivate() {
    synchronized (activeLock) {
      if (active == true) {
        LOGGER.debug("Deactivating consumer of class {}", consumer.getClass());
        LOGGER.debug("Closing channel for consumer of class {}", consumer.getClass());
        ensureCompleteShutdown();
      }
      LOGGER.info("Deactivated consumer of class {}", consumer.getClass());
    }
  }


  void activate() throws IOException {
    synchronized (activeLock) {
      if (active == false) {
        LOGGER.debug("Activating consumer of class {}", consumer.getClass());
        // Start the consumer
        try {
          channel = this.consumerChannelFactory.createChannel();
          channel.addShutdownListener(shutdownListener);
          this.consumerExchangeAndQueueDeclarer.declareQueuesAndExchanges(channel);
          channel.basicConsume(queueName, autoAck,
              autoAck ? create(consumer) : createAcknowledged(consumer, channel));
          LOGGER.info("Activated consumer of class {}", consumer.getClass());
          active = true;
        } catch (IOException e) {
          LOGGER.error("Failed to activate consumer of class {}", consumer.getClass(), e);
          ensureCompleteShutdown();
          throw e;
        }
      }
    }
  }

  void ensureCompleteShutdown() {
    if (channel != null) {
      channel.removeShutdownListener(this.shutdownListener);
    }
    channel = null;
    resourceCloser.closeResource(channel, "closing channel for consumer " + consumer.getClass());
    active = false;
  }

  boolean isAutoAck() {
    return autoAck;
  }

  String getQueueName() {
    return queueName;
  }


}