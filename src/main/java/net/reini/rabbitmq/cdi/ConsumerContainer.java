package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

class ConsumerContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainer.class);

  private final CdiConnectionFactory connectionFactory;
  private final List<ConsumerHolder> consumerHolders;

  ConsumerContainer(CdiConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
    this.consumerHolders = Collections.synchronizedList(new ArrayList<>());
    connectionFactory.registerListener(new ContainerConnectionListener());
  }

  /**
   * Creates a channel to be used for consuming from the broker.
   * 
   * @return The channel
   * @throws IOException if the channel cannot be created due to a connection problem
   * @throws TimeoutException if the channel cannot be created due to a timeout problem
   */
  protected Channel createChannel() throws IOException, TimeoutException {
    LOGGER.debug("Creating channel");
    Connection connection = connectionFactory.newConnection();
    Channel channel = connection.createChannel();
    LOGGER.debug("Created channel");
    return channel;
  }

  public void addConsumer(EventConsumer consumer, String queue, boolean autoAck) {
    consumerHolders.add(new ConsumerHolder(consumer, queue, autoAck));
  }

  public void startAllConsumers() {
    consumerHolders.forEach(holder -> holder.activate());
  }

  final class ContainerConnectionListener implements ConnectionListener {
    @Override
    public void onConnectionEstablished(Connection connection) {
      String hostName = connection.getAddress().getHostName();
      LOGGER.info("Connection established to {}. Activating consumers...", hostName);
      consumerHolders.forEach(consumer -> consumer.activate());
    }

    @Override
    public void onConnectionLost(Connection connection) {
      LOGGER.warn("Connection lost. Deactivating consumers");
      consumerHolders.forEach(consumer -> consumer.deactivate());
    }

    @Override
    public void onConnectionClosed(Connection connection) {
      LOGGER.warn("Connection closed for ever. Deactivating consumers");
      consumerHolders.forEach(consumer -> consumer.deactivate());
    }
  }

  final class ConsumerHolder {
    private final boolean autoAck;
    private final String queueName;
    private final EventConsumer consumer;

    private boolean active;
    private Channel channel;

    ConsumerHolder(EventConsumer consumer, String queueName, boolean autoAck) {
      this.consumer = consumer;
      this.queueName = queueName;
      this.autoAck = autoAck;
    }

    void deactivate() {
      LOGGER.info("Deactivating consumer of class {}", consumer.getClass());
      if (channel != null) {
        try {
          LOGGER.info("Closing channel for consumer of class {}", consumer.getClass());
          channel.close();
          LOGGER.info("Closed channel for consumer of class {}", consumer.getClass());
        } catch (Exception e) {
          LOGGER.info("Aborted closing channel for consumer of class {} (already closing)",
              consumer.getClass());
          // Ignore exception: In this case the channel is for sure
          // not usable any more
        }
        channel = null;
        consumer.setChannel(channel);
      }
      active = false;
      LOGGER.info("Deactivated consumer of class {}", consumer.getClass());
    }

    void activate() {
      LOGGER.info("Activating consumer of class {}", consumer.getClass());
      // Make sure the consumer is not active before starting it
      if (active) {
        deactivate();
      }
      // Start the consumer
      try {
        channel = createChannel();
        consumer.setChannel(channel);
        channel.basicConsume(queueName, autoAck, consumer);
        active = true;
        LOGGER.info("Activated consumer of class {}", consumer.getClass());
      } catch (IOException | TimeoutException e) {
        LOGGER.error("Failed to activate consumer of class {}", consumer.getClass(), e);
      }
    }
  }
}
