package net.reini.rabbitmq.cdi;

public interface MessagePublisher {

  /**
   * Publishes the given event using the given publisher configuration template.
   * 
   * @param event the event being published to RabbitMQ
   * @param publisherConfiguration the default publisher configuration
   * @throws PublishException if the event could not be delivered to RabbitMQ
   */
  void publish(Object event, PublisherConfiguration<?> publisherConfiguration)
      throws PublishException;

  /**
   * Closes the publisher by closing its underlying channel.
   */
  void close();
}
