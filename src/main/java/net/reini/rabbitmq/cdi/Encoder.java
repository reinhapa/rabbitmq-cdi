package net.reini.rabbitmq.cdi;

/**
 * The Encoder is responsible to convert a message object of the given type to a raw bytes message.
 * 
 * @author André Ignacio
 * @param <T> Message type
 */
public interface Encoder<T> {

  /**
   * Encode a message object of type T into given bytes.
   * 
   * @return bytes
   * @throws EncodeException If the conversion fails
   */
  byte[] encode(T object) throws EncodeException;

  /**
   * Content type of converter. Example:"application/json". Could be null
   * 
   * @return Content Type
   */
  String contentType();
}
