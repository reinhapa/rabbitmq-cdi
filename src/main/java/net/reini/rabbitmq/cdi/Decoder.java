package net.reini.rabbitmq.cdi;

/**
 * The Decoder is responsible to convert a raw bytes message to a message object of the given type.
 *
 * @author Patrick Reinhart
 */
public interface Decoder<T> {

  /**
   * Decode the given bytes into an message object of type M.
   *
   * @param bytes the bytes to be decoded.
   * @return the decoded message object.
   * @throws DecodeException if the message decoding fails
   */
  T decode(byte[] bytes) throws DecodeException;

  /**
   * Answer whether the given content type can be decoded into an object of type T.
   *
   * @param contentType the content type to be decoded.
   * @return whether or not the bytes can be decoded by this decoder.
   */
  boolean willDecode(String contentType);
}
