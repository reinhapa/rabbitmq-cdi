package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

public class GenericPublisher implements MessagePublisher {
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericPublisher.class);

  public static final int DEFAULT_RETRY_ATTEMPTS = 3;
  public static final int DEFAULT_RETRY_INTERVAL = 1000;

  private final ConnectionProducer connectionProducer;
  private final Map<ConnectionConfig, Channel> channelMap;

  public GenericPublisher(ConnectionProducer connectionProducer) {
    this.connectionProducer = connectionProducer;
    channelMap = new HashMap<>();
  }

  /**
   * Initializes a channel if there is not already an open channel.
   * 
   * @param config the connection configuration
   * @return The initialized or already open channel.
   * @throws IOException if the channel cannot be initialized
   * @throws TimeoutException if the channel can not be opened within the timeout period
   */
  protected Channel provideChannel(ConnectionConfig config) throws IOException, TimeoutException {
    Channel channel = channelMap.get(config);
    if (channel == null || !channel.isOpen()) {
      channel = connectionProducer.getConnection(config).createChannel();
      channelMap.put(config, channel);
    }
    return channel;
  }

  /**
   * Handles an exception depending on the already used attempts to send a message. Also performs a
   * soft reset of the currently used channel.
   *
   * @param channel Current channel that has a problem. Can be {@code null}
   * @param attempt Current attempt count
   * @param cause The thrown exception
   * @throws PublishException if the maximum amount of attempts is exceeded
   */
  protected void handleIoException(Channel channel, int attempt, Throwable cause)
      throws PublishException {
    if (channel != null) {
      closeChannel(channel);
    }
    channel = null;
    if (attempt == DEFAULT_RETRY_ATTEMPTS) {
      throw new PublishException("Unable to send message after " + attempt + " attempts", cause);
    }
    sleepBeforeRetry();
  }

  protected void sleepBeforeRetry() {
    try {
      Thread.sleep(DEFAULT_RETRY_INTERVAL);
    } catch (InterruptedException e) {
      LOGGER.warn("Sending message interrupted while waiting for retry attempt", e);
    }
  }

  @Override
  public void publish(Object event, PublisherConfiguration publisherConfiguration)
      throws PublishException {
    for (int attempt = 1; attempt <= DEFAULT_RETRY_ATTEMPTS; attempt++) {
      if (attempt > 1) {
        LOGGER.debug("Attempt {} to send message", Integer.valueOf(attempt));
      }
      Channel channel = null;
      try {
        channel = provideChannel(publisherConfiguration.getConfig());
        publisherConfiguration.publish(channel, event);
        return;
      } catch (EncodeException e) {
        throw new PublishException("Unable to serialize event", e);
      } catch (IOException | TimeoutException e) {
        handleIoException(channel, attempt, e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    channelMap.values().forEach(this::closeChannel);
  }

  protected void closeChannel(Channel channel) {
    if (channel == null) {
      LOGGER.warn("Attempt to close a publisher channel that has not been initialized");
      return;
    } else if (!channel.isOpen()) {
      LOGGER.warn(
          "Attempt to close a publisher channel that has already been closed or is already closing");
      return;
    }
    LOGGER.debug("Closing publisher channel");
    try {
      channel.close();
    } catch (IOException | TimeoutException e) {
      LOGGER.warn("Failed to close channel", e);
    }
    LOGGER.debug("Successfully closed publisher channel");
  }
}
