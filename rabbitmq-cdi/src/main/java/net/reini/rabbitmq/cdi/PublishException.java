package net.reini.rabbitmq.cdi;

/**
 * A general exception that occurs when trying to publish a RabbitMQ message.
 * 
 * @author Patrick Reinhart
 */
public class PublishException extends Exception {
  private static final long serialVersionUID = 1L;

  public PublishException(Throwable cause) {
    super(cause);
  }

  public PublishException(String message, Throwable cause) {
    super(message, cause);
  }
}
