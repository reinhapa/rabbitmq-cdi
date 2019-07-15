package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rabbitmq.client.Channel;

class DeclarerRepository {
    private final Map<Class<?>, Declarer<? extends Declaration>> declarerMap;

  DeclarerRepository() {
    this.declarerMap = new HashMap<>();
    this.declarerMap.put(ExchangeDeclaration.class, new ExchangeDeclarer());
    this.declarerMap.put(QueueDeclaration.class, new QueueDeclarer());
    this.declarerMap.put(BindingDeclaration.class, new BindingDeclarer());
  }

  @SuppressWarnings("unchecked")
  void declare(Channel channel, List<? extends Declaration> declarations) throws IOException {
    for (Declaration declaration : declarations) {
      Class<? extends Declaration> aClass = declaration.getClass();
      @SuppressWarnings({"rawtypes"})
      Declarer declarer = declarerMap.get(aClass);
      declarer.declare(channel, declaration);
    }
  }

}