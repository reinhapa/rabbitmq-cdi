package net.reini.rabbitmq.cdi;

class ConsumerHolderFactory {
  ConsumerHolder createConsumerHolder(EventConsumer consumer, String queue, boolean autoAck, ConnectionRepository connectionRepository, ConnectionConfiguration config,
      ExchangeDeclarationConfig exchangeDeclarationConfig, QueueDeclarationConfig queueDeclarationConfig) {
    ConsumerExchangeAndQueueDeclarer consumerExchangeAndQueueDeclarer = new ConsumerExchangeAndQueueDeclarer(exchangeDeclarationConfig, queueDeclarationConfig);
    ConsumerChannelFactory consumerChannelFactory = new ConsumerChannelFactory(connectionRepository, config);
    ConsumerFactory consumerFactory =new ConsumerFactory();
    return new ConsumerHolder(consumer, queue, autoAck, consumerChannelFactory, consumerExchangeAndQueueDeclarer, consumerFactory);
  }
}