package net.reini.rabbitmq.cdi;

import java.io.IOException;

import com.rabbitmq.client.Channel;

public interface Declarer<T extends Declaration> {
  void declare(Channel channel, T declaration) throws IOException;
}