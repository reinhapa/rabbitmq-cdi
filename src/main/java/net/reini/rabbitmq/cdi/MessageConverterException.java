package net.reini.rabbitmq.cdi;

/**
 * Exception for failures in message conversion.
 * 
 * @author André Ignacio 
 */
public class MessageConverterException extends RuntimeException {
  private static final String MESSAGE = "Message conversion failed.";
  private static final long serialVersionUID = 1L;

  /**
   * Construct a {@link MessageConverterException}.
   * 
   * @param cause Cause
   */
  public MessageConverterException(Throwable cause){
    super(MESSAGE, cause);
  }
}
