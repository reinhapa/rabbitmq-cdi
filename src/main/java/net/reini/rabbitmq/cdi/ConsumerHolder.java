package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;

class ConsumerHolder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerHolder.class);

  private final boolean autoAck;
  private final String queueName;
  private final Object activeLock;
  private final EventConsumer<?> consumer;
  private final ShutdownListener shutdownListener;
  private final ConsumerChannelFactory consumerChannelFactory;
  private final ResourceCloser resourceCloser;
  private final ConsumerFactory consumerFactory;
  private final DeclarerRepository declarerRepository;
  private final List<Declaration> declarations;
  private final int prefetchCount;
  private Channel channel;

  private volatile boolean active;

  ConsumerHolder(EventConsumer<?> consumer, String queueName, boolean autoAck, int prefetchCount,
      ConsumerChannelFactory consumerChannelFactory, ConsumerFactory consumerFactory,
      List<Declaration> declarations,
      DeclarerRepository declarerRepository) {
    this.consumer = consumer;
    this.queueName = queueName;
    this.autoAck = autoAck;
    this.prefetchCount = prefetchCount;
    this.consumerChannelFactory = consumerChannelFactory;
    this.declarations = declarations;
    this.declarerRepository = declarerRepository;
    this.activeLock = new Object();
    this.resourceCloser = new ResourceCloser();
    this.shutdownListener = new ConsumerHolderChannelShutdownListener(this);
    this.consumerFactory = consumerFactory;
  }

  void deactivate() {
    synchronized (activeLock) {
      if (active) {
        LOGGER.debug("Deactivating consumer of class {}", consumer.getClass());
        LOGGER.debug("Closing channel for consumer of class {}", consumer.getClass());
        ensureCompleteShutdown();
      }
      LOGGER.info("Deactivated consumer of class {}", consumer.getClass());
    }
  }

  void activate() throws IOException {
    synchronized (activeLock) {
      if (!active) {
        LOGGER.debug("Activating consumer of class {}", consumer.getClass());
        // Start the consumer
        try {
          Channel ch = ensureOpenChannel();
          declarerRepository.declare(ch, declarations);
          channel.basicConsume(queueName, autoAck, autoAck ? consumerFactory.create(consumer)
              : consumerFactory.createAcknowledged(consumer, this::ensureOpenChannel));
          LOGGER.info("Activated consumer of class {}", consumer.getClass());
          active = true;
        } catch (Exception e) {
          LOGGER.error("Failed to activate consumer of class {}", consumer.getClass(), e);
          ensureCompleteShutdown();
          throw e;
        }
      }
    }
  }

  Channel ensureOpenChannel() {
    try {
      if (channel == null || !channel.isOpen()) {
        channel = this.consumerChannelFactory.createChannel();
        channel.addShutdownListener(shutdownListener);
        channel.basicQos(this.prefetchCount);
      }
      return channel;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  void ensureCompleteShutdown() {
    if (channel != null) {
      channel.removeShutdownListener(this.shutdownListener);
    }
    resourceCloser.closeResource(channel, "closing channel for consumer " + consumer.getClass());
    channel = null;
    active = false;
  }

  boolean isAutoAck() {
    return autoAck;
  }

  String getQueueName() {
    return queueName;
  }
}
