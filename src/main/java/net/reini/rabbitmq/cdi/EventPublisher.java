/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2023 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.reini.rabbitmq.cdi;

import static jakarta.enterprise.event.TransactionPhase.AFTER_COMPLETION;
import static jakarta.enterprise.event.TransactionPhase.AFTER_FAILURE;
import static jakarta.enterprise.event.TransactionPhase.AFTER_SUCCESS;
import static jakarta.enterprise.event.TransactionPhase.BEFORE_COMPLETION;
import static jakarta.enterprise.event.TransactionPhase.IN_PROGRESS;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.event.ObserverException;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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

  private final ConnectionRepository connectionRepository;
  private final Map<EventKey<?>, PublisherConfiguration<?>> publisherConfigurations;
  private final ThreadLocal<Map<EventKey<Object>, MessagePublisher<Object>>> publishers;

  @Inject
  public EventPublisher(ConnectionRepository connectionRepository) {
    this.connectionRepository = connectionRepository;
    this.publisherConfigurations = new HashMap<>();
    this.publishers = ThreadLocal.withInitial(HashMap::new);
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
  public <T> void addEvent(EventKey<T> eventKey, PublisherConfiguration<T> configuration) {
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
   * @throws ObserverException if the event could not be delivered to RabbitMQ
   */
  public void onEventAfterSuccess(@Observes(during = AFTER_SUCCESS) Object event) {
    publishEvent(event, AFTER_SUCCESS);
  }

  void publishEvent(Object event, TransactionPhase transactionPhase) {
    @SuppressWarnings("unchecked")
    EventKey<Object> eventKey =  (EventKey<Object>) EventKey.of(event.getClass(), transactionPhase);
    @SuppressWarnings("unchecked")
    PublisherConfiguration<Object> configuration = (PublisherConfiguration<Object>) publisherConfigurations.get(eventKey);
    if (configuration == null) {
      LOGGER.trace("No publisher configured for event {}", event);
    } else {
      doPublish(event, providePublisher(eventKey, transactionPhase), configuration);
    }
  }

  @PreDestroy
  public void cleanUp() {
    publishers.get().values().forEach(MessagePublisher::close);
  }

  <T> void doPublish(T event, MessagePublisher<T> publisher,
      PublisherConfiguration<T> configuration) {
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
   * Provides a publisher with the specified reliability. Within the same thread, the same producer
   * instance is provided for the given event type.
   *
   * @param eventKey The event key
   * @param transactionPhase The actual transaction phase of the event
   * @return The provided publisher
   */
  MessagePublisher<Object> providePublisher(EventKey<Object> eventKey,
      TransactionPhase transactionPhase) {
    Map<EventKey<Object>, MessagePublisher<Object>> localPublishers = publishers.get();
    return localPublishers.computeIfAbsent(eventKey,
        key -> new GenericPublisher<>(connectionRepository));
  }
}
