package net.reini.rabbitmq.cdi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Default converter who use Jackson {@link ObjectMapper} to converte the message.
 * 
 * @author André Ignacio
 */
public class JacksonMessageConverter extends AbstractMessageConverter {
  private static final String CONTENT_TYPE = "application/json";
  private final ObjectMapper mapper;
    
  public JacksonMessageConverter(){
    mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);    
  }

  @Override
  public byte[] toBytesInner(Object object) throws JsonProcessingException {
    return mapper.writeValueAsBytes(object);
  }

  @Override
  public String contentType() {
    return CONTENT_TYPE;
  }
}