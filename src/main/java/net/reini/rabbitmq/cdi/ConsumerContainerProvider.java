package net.reini.rabbitmq.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides the default consumer container.
 *
 * @author Patrick Reinhart
 */
@ApplicationScoped
public class ConsumerContainerProvider {
  @Inject
  CdiConnectionFactory connectionFactory;

  @Produces
  @Singleton
  public ConsumerContainer provideConsumerContainer() {
    return new ConsumerContainer(connectionFactory);
  }
}
