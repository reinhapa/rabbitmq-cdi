package net.reini.rabbitmq.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.event.ObserverException;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Publishes events to exchanges of a broker.
 *
 * @author Patrick Reinhart
 */
@Singleton
public class EventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisher.class);

  private final ConnectionRepository connectionRepository;
  private final Map<Class<?>, Set<PublisherConfiguration>> publisherConfigurations;
  private final ThreadLocal<Map<Class<?>, MessagePublisher>> publishers;

  @Inject
  public EventPublisher(ConnectionRepository connectionRepository) {
    this.connectionRepository = connectionRepository;
    this.publisherConfigurations = new HashMap<>();
    this.publishers = ThreadLocal.withInitial(HashMap::new);
  }

  /**
   * Adds events of the given type to the CDI events to which the event publisher listens in order to publish them. The publisher configuration is used to decide where to and how to publish messages.
   *
   * @param eventType The event type
   * @param configuration The configuration used when publishing and event
   */
  public void addEvent(Class<?> eventType, PublisherConfiguration configuration) {
    publisherConfigurations.computeIfAbsent(eventType, key -> new HashSet<>()).add(configuration);
  }

  /**
   * Observes CDI events for remote events and publishes those events if their event type was added before.
   *
   * @param event The event to publish
   * @throws ObserverException if the event could not be delivered to RabbitMQ
   */
  public void publishEvent(@Observes Object event) {
    Class<?> eventType = event.getClass();
    Set<PublisherConfiguration> configurations = publisherConfigurations.get(eventType);
    if (configurations == null) {
      LOGGER.trace("No publisher configured for event {}", event);
    } else {
      configurations.forEach(config -> doPublish(event, providePublisher(eventType), config));

    }
  }

  @PreDestroy
  public void cleanUp() {
    publishers.get().values().forEach(MessagePublisher::close);
  }

  void doPublish(Object event, MessagePublisher publisher, PublisherConfiguration configuration) {
    try {
      LOGGER.debug("Start publishing event {} ({})...", event, configuration);
      publisher.publish(event, configuration);
      LOGGER.debug("Published event successfully");
    } catch (PublishException e) {
      LOGGER.debug("Published event failed");
      configuration.accept(event, e);
    }
  }

  /**
   * Provides a publisher with the specified reliability. Within the same thread, the same producer instance is provided for the given event type.
   *
   * @param eventType The event type
   * @return The provided publisher
   */
  MessagePublisher providePublisher(Class<?> eventType) {
    return publishers.get().computeIfAbsent(eventType,
        key -> new GenericPublisher(connectionRepository));
  }

}
