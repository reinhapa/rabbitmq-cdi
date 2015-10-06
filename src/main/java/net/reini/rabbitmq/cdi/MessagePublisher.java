package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface MessagePublisher extends AutoCloseable {

  /**
   * Publishes the given event using the given publisher configuration template.
   * 
   * @param event the event being published to RabbitMQ
   * @param publisherConfiguration the default publisher configuration
   * @throws IOException if the channel can not be opened correctly or the actual send fails.
   * @throws TimeoutException if a timeout while sending occurs
   */
  void publish(Object event, PublisherConfiguration publisherConfiguration)
      throws IOException, TimeoutException;

  /**
   * Closes the publisher by closing its underlying channel.
   * 
   * @throws IOException if the channel cannot be closed correctly. Usually occurs when the channel
   *         is already closing or is already closed.
   * @throws TimeoutException if a timeout occurs while closing the publisher.
   */
  @Override
  void close() throws IOException, TimeoutException;
}
