package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class GenericPublisher implements MessagePublisher {
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericPublisher.class);

  public static final int DEFAULT_RETRY_ATTEMPTS = 3;
  public static final int DEFAULT_RETRY_INTERVAL = 1000;

  private final ConnectionRepository connectionRepository;

  public GenericPublisher(ConnectionRepository connectionRepository) {
    this.connectionRepository = connectionRepository;
  }

  /**
   * Handles an exception depending on the already used attempts to send a message. Also performs a
   * soft reset of the currently used channel.
   *
   * @param attempt Current attempt count
   * @param cause The thrown exception
   *
   * @throws PublishException if the maximum amount of attempts is exceeded
   */
  protected void handleIoException(int attempt, Throwable cause)
      throws PublishException {
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
      try (Channel channel =
                   connectionRepository.getConnection(publisherConfiguration.getConfig()).createChannel()) {
        publisherConfiguration.publish(channel, event);
        return;
      } catch (EncodeException e) {
        throw new PublishException("Unable to serialize event", e);
      } catch (IOException | TimeoutException e) {
        handleIoException(attempt, e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
  }
}
