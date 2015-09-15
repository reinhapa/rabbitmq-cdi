package net.reini.rabbitmq.cdi;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.rabbitmq.client.ConnectionFactory;

/**
 * Provides the default consumer container.
 *
 * @author Patrick Reinhart
 */
public class ConsumerContainerProvider {
	@Inject
	ConnectionFactory connectionFactory;

	@Produces
	@Singleton
	public ConsumerContainer provideConsumerContainer() {
		return new ConsumerContainer(connectionFactory);
	}
}
