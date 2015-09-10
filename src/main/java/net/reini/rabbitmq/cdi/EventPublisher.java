package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Publishes events to exchanges of a broker.
 *
 * @author christian.bick
 */
@Singleton
public class EventPublisher {
	private static Logger LOGGER = LoggerFactory
			.getLogger(EventPublisher.class);

	ConnectionFactory connectionFactory;

	Map<Class<?>, PublisherConfiguration> publisherConfigurations = new HashMap<>();

	ThreadLocal<Map<Class<?>, MessagePublisher>> publishers = new ThreadLocal<>();

	@Inject
	public EventPublisher(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Adds events of the given type to the CDI events to which the event
	 * publisher listens in order to publish them. The publisher configuration
	 * is used to decide where to and how to publish messages.
	 *
	 * @param eventType
	 *            The event type
	 * @param configuration
	 *            The configuration used when publishing and event
	 * @param <T>
	 *            The event type
	 */
	public <T> void addEvent(Class<T> eventType,
			PublisherConfiguration configuration) {
		publisherConfigurations.put(eventType, configuration);
	}

	/**
	 * Observes CDI events for remote events and publishes those events if their
	 * event type was added before.
	 *
	 * @param event
	 *            The event to publish
	 * @throws IOException
	 *             if the event failed to be published
	 * @throws TimeoutException
	 *             if the event failed to be published
	 */
	public void publishEvent(@Observes Object event)
			throws IOException, TimeoutException {
		Class<?> eventType = event.getClass();
		LOGGER.debug("Receiving event of type {}", eventType.getSimpleName());
		if (!publisherConfigurations.containsKey(eventType)) {
			LOGGER.debug("No publisher configured for event of type {}",
					eventType.getSimpleName());
			return;
		}
		PublisherConfiguration publisherConfiguration = publisherConfigurations
				.get(eventType);
		try (MessagePublisher publisher = providePublisher(eventType)) {
			LOGGER.info("Publishing event of type {}",
					eventType.getSimpleName());
			publisher.publish(event, publisherConfiguration);
			LOGGER.info("Successfully published event of type {}",
					eventType.getSimpleName());
		} catch (IOException | TimeoutException e) {
			LOGGER.error("Failed to publish event {}",
					eventType.getSimpleName(), e);
			throw e;
		}
	}

	/**
	 * Provides a publisher with the specified reliability. Within the same
	 * thread, the same producer instance is provided for the given event type.
	 *
	 * @param reliability
	 *            The desired publisher reliability
	 * @param eventType
	 *            The event type
	 * @return The provided publisher
	 */
	MessagePublisher providePublisher(Class<?> eventType) {
		Map<Class<?>, MessagePublisher> localPublishers = publishers.get();
		if (localPublishers == null) {
			localPublishers = new HashMap<>();
			publishers.set(localPublishers);
		}
		MessagePublisher publisher = localPublishers.get(eventType);
		if (publisher == null) {
			publisher = new GenericPublisher(connectionFactory);
			localPublishers.put(eventType, publisher);
		}
		return publisher;
	}

	/**
	 * A publisher configuration stores all important settings and options used
	 * for publishing and event.
	 *
	 * @author christian.bick
	 */
	public static class PublisherConfiguration {
		public PublisherConfiguration(String exchange, String routingKey,
				Boolean persistent, AMQP.BasicProperties basicProperties) {
			this.exchange = exchange;
			this.routingKey = routingKey;
			this.persistent = persistent;
			this.basicProperties = basicProperties;
		}

		String exchange;
		String routingKey;
		Boolean persistent;
		AMQP.BasicProperties basicProperties;
	}
}
