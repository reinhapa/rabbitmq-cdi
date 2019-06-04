package net.reini.rabbitmq.cdi.kryo;

import net.reini.rabbitmq.cdi.DecodeException;
import net.reini.rabbitmq.cdi.Decoder;

public class KryoDecoder<T> implements Decoder<T> {
  private final Class<T> eventType;
  
  public KryoDecoder(Class<T> eventType) {
    this.eventType = eventType;
  }
  @Override
  public T decode(byte[] bytes) throws DecodeException {
    return null;
  }

  @Override
  public boolean willDecode(String contentType) {
    return false;
  }
}
