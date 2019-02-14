package net.reini.rabbitmq.cdi;

import javax.enterprise.context.Dependent;

@Dependent
class ConsumerContainerFactory {
  public ConsumerContainer create(ConnectionConfig configuration,
      ConnectionRepository connectionRepository,DeclarerRepository declarerRepository) {
    return new ConsumerContainer(configuration, connectionRepository,declarerRepository);
  }
}
