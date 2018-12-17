package net.reini.rabbitmq.cdi;

/**
 * A general exception that occurs when trying to encode a custom object to a binary message.
 *
 * @author André Ignacio
 */
public class EncodeException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Construct a {@link EncodeException}.
   *
   * @param cause Cause
   */
  public EncodeException(Throwable cause) {
    super(cause);
  }
}
