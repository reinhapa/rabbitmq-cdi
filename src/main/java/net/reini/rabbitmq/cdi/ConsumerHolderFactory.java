package net.reini.rabbitmq.cdi;

import java.util.List;

class ConsumerHolderFactory {
  ConsumerHolder createConsumerHolder(EventConsumer<?> consumer, String queue, boolean autoAck,
      int prefetchCount, ConnectionRepository connectionRepository, ConnectionConfig config,
      List<Declaration> declarations, DeclarerRepository declarerRepository) {
    ConsumerChannelFactory consumerChannelFactory =
        new ConsumerChannelFactory(connectionRepository, config);
    ConsumerFactory consumerFactory = new ConsumerFactory();
    return new ConsumerHolder(consumer, queue, autoAck, prefetchCount, consumerChannelFactory,
        consumerFactory, declarations, declarerRepository);
  }
}
