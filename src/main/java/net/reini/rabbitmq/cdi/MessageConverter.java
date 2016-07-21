package net.reini.rabbitmq.cdi;

/**
 * Transform a object in a array de bytes to send a message.
 * 
 * @author André Ignacio
 */
public interface MessageConverter {

  /**
   * Transform a object in a array de bytes to send a message.
   *  
   * @return Array of bytes
   * @throws MessageConverterException If the conversion fails
   */
  byte[] toBytes(Object object) throws MessageConverterException;

  /**
   * Content type of converter. Example: application/json
   * Could be null
   * 
   * @return Content Type
   */
  String contentType();
}
