package net.reini.rabbitmq.cdi;

import static javax.enterprise.event.TransactionPhase.AFTER_COMPLETION;
import static javax.enterprise.event.TransactionPhase.AFTER_FAILURE;
import static javax.enterprise.event.TransactionPhase.AFTER_SUCCESS;
import static javax.enterprise.event.TransactionPhase.BEFORE_COMPLETION;
import static javax.enterprise.event.TransactionPhase.IN_PROGRESS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
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
  private final Map<EventKey<?>, PublisherConfiguration> publisherConfigurations;
  private final ThreadLocal<Map<EventKey<?>, MessagePublisher>> publishers;

  @Inject
  public EventPublisher(ConnectionProducer connectionProducer) {
    this.connectionProducer = connectionProducer;
    this.publisherConfigurations = new HashMap<>();
    this.publishers = new ThreadLocal<>();
  }

  /**
   * Adds events of the given type to the CDI events to which the event publisher listens in order
   * to publish them. The publisher configuration is used to decide where to and how to publish
   * messages.
   *
   * @param eventKey The event key
   * @param configuration The configuration used when publishing and event
   * @param <T> The event type
   */
  public void addEvent(EventKey<?> eventKey, PublisherConfiguration configuration) {
    publisherConfigurations.put(eventKey, configuration);
  }

  /**
   * Observes a CDI event in progress and publishes it to the respective RabbitMQ exchange.
   * 
   * @param event The event to publish
   */
  public void onEventInProgress(@Observes(during = IN_PROGRESS) Object event) {
    publishEvent(event, IN_PROGRESS);
  }

  /**
   * Observes a CDI event before completion and publishes it to the respective RabbitMQ exchange.
   * 
   * @param event The event to publish
   */
  public void onEventBeforeCompletion(@Observes(during = BEFORE_COMPLETION) Object event) {
    publishEvent(event, BEFORE_COMPLETION);
  }

  /**
   * Observes a CDI event after completion and publishes it to the respective RabbitMQ exchange.
   * 
   * @param event The event to publish
   */
  public void onEventAfterCompletion(@Observes(during = AFTER_COMPLETION) Object event) {
    publishEvent(event, AFTER_COMPLETION);
  }

  /**
   * Observes a CDI event after failure and publishes it to the respective RabbitMQ exchange.
   * 
   * @param event The event to publish
   */
  public void onEventAfterFailure(@Observes(during = AFTER_FAILURE) Object event) {
    publishEvent(event, AFTER_FAILURE);
  }

  /**
   * Observes a CDI event after success and publishes it to the respective RabbitMQ exchange.
   * 
   * @param event The event to publish
   */
  public void onEventAfterSuccess(@Observes(during = AFTER_SUCCESS) Object event) {
    publishEvent(event, AFTER_SUCCESS);
  }

  void publishEvent(Object event, TransactionPhase transactionPhase) {
    EventKey<?> eventKey =  EventKey.of(event.getClass(), transactionPhase);
    PublisherConfiguration publisherConfiguration = publisherConfigurations.get(eventKey);
    if (publisherConfiguration == null) {
      LOGGER.trace("No publisher configured for event {}", event);
    } else {
      try (MessagePublisher publisher = providePublisher(eventKey, transactionPhase)) {
        LOGGER.debug("Start publishing event {}...", event);
        publisher.publish(event, publisherConfiguration);
        LOGGER.debug("Published event successfully");
      } catch (IOException | TimeoutException e) {
        throw new RuntimeException("Failed to publish event to RabbitMQ", e);
      }
    }
  }

  /**
   * Provides a publisher with the specified reliability. Within the same thread, the same producer
   * instance is provided for the given event type.
   *
   * @param reliability The desired publisher reliability
   * @param eventKey The event key
   * @param transactionPhase The actual transaction phase of the event
   * @return The provided publisher
   */
  MessagePublisher providePublisher(EventKey<?> eventKey, TransactionPhase transactionPhase) {
    Map<EventKey<?>, MessagePublisher> localPublishers = publishers.get();
    if (localPublishers == null) {
      localPublishers = new HashMap<>();
      publishers.set(localPublishers);
    }
    MessagePublisher publisher = localPublishers.get(eventKey);
    if (publisher == null) {
      publisher = new GenericPublisher(connectionProducer);
      localPublishers.put(eventKey, publisher);
    }
    return publisher;
  }
}
