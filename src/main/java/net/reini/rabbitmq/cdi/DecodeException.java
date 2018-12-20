package net.reini.rabbitmq.cdi;

/**
 * A general exception that occurs when trying to decode a custom object from a binary message.
 *
 * @author Patrick Reinhart
 */
public class DecodeException extends Exception {
  private static final long serialVersionUID = 1L;

  public DecodeException(Throwable cause) {
    super(cause);
  }
}
