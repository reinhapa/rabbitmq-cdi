package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

class DeclarerRepository<T extends Declaration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeclarerRepository.class);

  private final Supplier<Declarer<T>> declarerSupplier;

  DeclarerRepository(Supplier<Declarer<T>> declarerSupplier) {
    this.declarerSupplier = declarerSupplier;
  }

  void declare(Channel channel, List<T> declarations) throws IOException {
    if (!declarations.isEmpty()) {
      Declarer<T> declarer = declarerSupplier.get();
      for (T declaration : declarations) {
        LOGGER.info("declaring: {}", declaration);
        declarer.declare(channel, declaration);
      }
    }
  }
}