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
   * @param object the object to be encode
   * @return the encoded object as a byte array
   * @throws EncodeException If the conversion fails
   */
  byte[] encode(T object) throws EncodeException;

  /**
   * Content type of converter.
   * 
   * <h3>Example:</h3>
   * 
   * {@code application/json}
   * 
   * @return The content type string, can be {@code null}
   */
  String contentType();
}
