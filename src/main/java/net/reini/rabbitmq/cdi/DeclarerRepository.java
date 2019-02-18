package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

class DeclarerRepository {
  private final Map<Class, Declarer> declarerMap;
  private static final Logger LOGGER = LoggerFactory.getLogger(DeclarerRepository.class);

  DeclarerRepository() {
    this.declarerMap = new HashMap<>();
    this.declarerMap.put(ExchangeDeclaration.class, new ExchangeDeclarer());
    this.declarerMap.put(QueueDeclaration.class, new QueueDeclarer());
    this.declarerMap.put(QueueToExchangeBindingDeclaration.class, new QueueToExchangeBindingDeclarer());
  }

  void declare(Channel channel, List<Declaration> declarations) throws IOException {
    for (Declaration declaration : declarations) {
      Declarer declarer = declarerMap.get(declaration.getClass());
      LOGGER.info("declaring: " + declaration);
      declarer.declare(channel, declaration);
    }
  }
}