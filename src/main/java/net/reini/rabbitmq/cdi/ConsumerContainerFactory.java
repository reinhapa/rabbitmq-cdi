package net.reini.rabbitmq.cdi;

class ConsumerContainerFactory {
  public ConsumerContainer create(ConnectionConfiguration configuration, ConnectionRepository connectionRepository) {
    return new ConsumerContainer(configuration, connectionRepository);
  }
}
