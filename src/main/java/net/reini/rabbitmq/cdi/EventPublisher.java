package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes events to exchanges of a broker.
 *
 * @author Patrick Reinhart
 */
@Singleton
public class EventPublisher {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisher.class);

  private final ConnectionProducer connectionProducer;
  private final Map<Class<?>, Set<PublisherConfiguration>> publisherConfigurations;
  private final ThreadLocal<Map<Class<?>, MessagePublisher>> publishers;

  @Inject
  public EventPublisher(ConnectionProducer connectionProducer) {
    this.connectionProducer = connectionProducer;
    this.publisherConfigurations = new HashMap<>();
    this.publishers = ThreadLocal.withInitial(HashMap::new);
  }

  /**
   * Adds events of the given type to the CDI events to which the event publisher listens in order
   * to publish them. The publisher configuration is used to decide where to and how to publish
   * messages.
   *
   * @param eventType The event type
   * @param configuration The configuration used when publishing and event
   */
  public void addEvent(Class<?> eventType, PublisherConfiguration configuration) {
    publisherConfigurations.computeIfAbsent(eventType, key -> new HashSet<>()).add(configuration);
  }

  /**
   * Observes CDI events for remote events and publishes those events if their event type was added
   * before.
   *
   * @param event The event to publish
   */
  public void publishEvent(@Observes Object event) {
    Class<?> eventType = event.getClass();
    Set<PublisherConfiguration> configurations = publisherConfigurations.get(eventType);
    if (configurations == null) {
      LOGGER.trace("No publisher configured for event {}", event);
    } else {
      try (MessagePublisher publisher = providePublisher(eventType)) {
        configurations.forEach(configuration -> doPublish(event, publisher, configuration));
      } catch (IOException | TimeoutException e) {
        throw new RuntimeException("Failed to publish event to RabbitMQ", e);
      }
    }
  }

  void doPublish(Object event, MessagePublisher publisher, PublisherConfiguration configuration) {
    try {
      LOGGER.debug("Start publishing event {} ({})...", event, configuration);
      publisher.publish(event, configuration);
      LOGGER.debug("Published event successfully");
    } catch (IOException | TimeoutException e) {
      LOGGER.error("Failed to publish event {} ({})", event, configuration, e);
    }
  }

  /**
   * Provides a publisher with the specified reliability. Within the same thread, the same producer
   * instance is provided for the given event type.
   *
   * @param reliability The desired publisher reliability
   * @param eventType The event type
   * @return The provided publisher
   */
  MessagePublisher providePublisher(Class<?> eventType) {
    Map<Class<?>, MessagePublisher> localPublishers = publishers.get();
    return localPublishers.computeIfAbsent(eventType,
        key -> new GenericPublisher(connectionProducer));
  }
}
