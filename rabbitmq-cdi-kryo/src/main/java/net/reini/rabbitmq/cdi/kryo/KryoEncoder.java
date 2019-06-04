package net.reini.rabbitmq.cdi.kryo;

import net.reini.rabbitmq.cdi.EncodeException;
import net.reini.rabbitmq.cdi.Encoder;

public class KryoEncoder<T> implements Encoder<T> {

  @Override
  public byte[] encode(T object) throws EncodeException {
    return null;
  }

  @Override
  public String contentType() {
    return null;
  }
}
