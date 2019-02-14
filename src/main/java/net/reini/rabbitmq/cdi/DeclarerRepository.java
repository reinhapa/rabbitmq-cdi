package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rabbitmq.client.Channel;

class DeclarerRepository {
  private final Map<Class, Declarer> declarerMap;

  DeclarerRepository() {
    this.declarerMap = new HashMap<>();
    this.declarerMap.put(ExchangeDeclaration.class, new ExchangeDeclarer());
    this.declarerMap.put(QueueDeclaration.class, new QueueDeclarer());
    this.declarerMap.put(QueueToExchangeBindingDeclaration.class, new QueueToExchangeBindingDeclarer());
  }

  void declare(Channel channel, List<Declaration> declarations) throws IOException {
    for (Declaration declaration : declarations) {
      Declarer declarer = declarerMap.get(declaration.getClass());
      declarer.declare(channel, declaration);
    }
  }
}