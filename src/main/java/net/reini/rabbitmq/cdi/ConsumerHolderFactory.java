package net.reini.rabbitmq.cdi;

class ConsumerHolderFactory {

  public ConsumerHolder createConsumerHolder(EventConsumer consumer, String queue, boolean autoAck, ConnectionRepository connectionRepository, ConnectionConfiguration config,
      ExchangeDeclarationConfig exchangeDeclarationConfig, QueueDeclarationConfig queueDeclarationConfig) {
    ConsumerExchangeAndQueueDeclarer consumerExchangeAndQueueDeclarer = new ConsumerExchangeAndQueueDeclarer(exchangeDeclarationConfig, queueDeclarationConfig);
    ConsumerChannelFactory consumerChannelFactory = new ConsumerChannelFactory(connectionRepository, config);
    return new ConsumerHolder(consumer, queue, autoAck, consumerChannelFactory, consumerExchangeAndQueueDeclarer);
  }
}
