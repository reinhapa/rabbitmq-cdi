package net.reini.rabbitmq.cdi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Specialized decoder that decodes Json into the target event type.
 * 
 * @author Patrick Reinhart
 */
public final class JsonDecoder<T> implements Decoder<T> {
  private static final ObjectMapper MAPPER =
      new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

  private final Class<T> eventType;

  public JsonDecoder(Class<T> eventType) {
    this.eventType = eventType;
  }

  @Override
  public T decode(byte[] bytes) throws DecodeException {
    try {
      return MAPPER.readValue(bytes, eventType);
    } catch (Exception e) {
      throw new DecodeException(e);
    }
  }

  @Override
  public boolean willDecode(String contentType) {
    return "application/json".equals(contentType);
  }
}
