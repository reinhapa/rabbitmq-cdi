package net.reini.rabbitmq.cdi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Specialized encoder that encodes a event type into Json using Jackson {@link ObjectMapper}.
 *
 * @author André Ignacio
 */
public final class JsonEncoder<T> implements Encoder<T> {
  private static final String CONTENT_TYPE = "application/json";
  private final ObjectMapper mapper;

  public JsonEncoder() {
    mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  @Override
  public byte[] encode(T object) throws EncodeException {
    try {
      return mapper.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new EncodeException(e);
    }
  }

  @Override
  public String contentType() {
    return CONTENT_TYPE;
  }
}
