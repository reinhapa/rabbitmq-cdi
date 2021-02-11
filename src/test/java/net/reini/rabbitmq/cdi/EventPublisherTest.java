/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2020 Patrick Reinhart
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.enterprise.event.TransactionPhase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Tests the {@link EventPublisher} implementation.
 *
 * @author Patrick Reinhart
 */
@ExtendWith(MockitoExtension.class)
public class EventPublisherTest {
  @Mock
  private ConnectionRepository connectionRepository;
  @Mock
  private ConnectionConfig config;
  @Mock
  private Connection connection;
  @Mock
  private Channel channel;
  @Mock
  private BiConsumer<TestEvent, PublishException> errorHandler;

  private List<ExchangeDeclaration> declarations = new ArrayList<>();
  private EventPublisher publisher;
  private Builder basicProperties;
  private JsonEncoder<TestEvent> encoder;
  private Function<TestEvent, String> routingKeyFunction;

  @BeforeEach
  public void setUp() throws Exception {
    publisher = new EventPublisher(connectionRepository);
    basicProperties = new BasicProperties.Builder();
    encoder = new JsonEncoder<>();
    routingKeyFunction = e -> "routingKey";
  }

  /**
   * Test method for {@link EventPublisher#publishEvent(Object, TransactionPhase)}.
   */
  @Test
  public void testPublishEvent_no_configuration() {
    publisher.publishEvent(new TestEvent(), TransactionPhase.AFTER_COMPLETION);
  }

  /**
   * Test method for {@link EventPublisher#addEvent(EventKey, PublisherConfiguration)},
   * {@link EventPublisher#publishEvent(Object, TransactionPhase)} and
   * {@link EventPublisher#cleanUp()}.
   * 
   * @throws TimeoutException
   * @throws IOException
   */
  @Test
  public void testPublishEvent() throws IOException, TimeoutException {
    EventKey<TestEvent> key = EventKey.of(TestEvent.class, TransactionPhase.AFTER_SUCCESS);
    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.addEvent(key, new PublisherConfiguration(config, "exchange", routingKeyFunction,
        basicProperties, null, encoder, errorHandler, declarations));
    publisher.publishEvent(new TestEvent(), TransactionPhase.AFTER_SUCCESS);
    publisher.cleanUp();

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), any(), any());
    verify(channel).close();
  }

  /**
   * Test method for {@link EventPublisher#addEvent(EventKey, PublisherConfiguration)},
   * {@link EventPublisher#publishEvent(Object, TransactionPhase)} and
   * {@link EventPublisher#cleanUp()}.
   * 
   * @throws TimeoutException
   * @throws IOException
   */
  @Test
  public void testPublishEvent_failing() throws IOException, TimeoutException {
    EventKey<TestEvent> key = EventKey.of(TestEvent.class, TransactionPhase.AFTER_FAILURE);

    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);
    doThrow(IOException.class).when(channel).basicPublish(eq("exchange"), eq("routingKey"), any(),
        any());

    publisher.addEvent(key, new PublisherConfiguration(config, "exchange", routingKeyFunction,
        basicProperties, null, encoder, errorHandler, declarations));
    publisher.publishEvent(new TestEvent(), TransactionPhase.AFTER_FAILURE);
    publisher.cleanUp();

    verify(channel, times(3)).close();
  }

  @Test
  public void testOnEventInProgress() throws IOException, TimeoutException {
    EventKey<TestEvent> key = EventKey.of(TestEvent.class, TransactionPhase.IN_PROGRESS);

    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.addEvent(key, new PublisherConfiguration(config, "exchange", routingKeyFunction,
        basicProperties, null, encoder, errorHandler, declarations));
    publisher.onEventInProgress(new TestEvent());
    publisher.cleanUp();

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), any(), any());
    verify(channel).close();
  }

  @Test
  public void testOnEventInBeforeCompletion() throws IOException, TimeoutException {
    EventKey<TestEvent> key = EventKey.of(TestEvent.class, TransactionPhase.BEFORE_COMPLETION);

    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.addEvent(key, new PublisherConfiguration(config, "exchange", routingKeyFunction,
        basicProperties, null, encoder, errorHandler, declarations));
    publisher.onEventBeforeCompletion(new TestEvent());
    publisher.cleanUp();

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), any(), any());
    verify(channel).close();
  }

  @Test
  public void testOnEventAfterCompletion() throws IOException, TimeoutException {
    EventKey<TestEvent> key = EventKey.of(TestEvent.class, TransactionPhase.AFTER_COMPLETION);

    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.addEvent(key, new PublisherConfiguration(config, "exchange", routingKeyFunction,
        basicProperties, null, encoder, errorHandler, declarations));
    publisher.onEventAfterCompletion(new TestEvent());
    publisher.cleanUp();

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), any(), any());
    verify(channel).close();
  }

  @Test
  public void testOnEventAfterFailure() throws IOException, TimeoutException {
    EventKey<TestEvent> key = EventKey.of(TestEvent.class, TransactionPhase.AFTER_FAILURE);

    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.addEvent(key, new PublisherConfiguration(config, "exchange", routingKeyFunction,
        basicProperties, null, encoder, errorHandler, declarations));
    publisher.onEventAfterFailure(new TestEvent());
    publisher.cleanUp();

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), any(), any());
    verify(channel).close();
  }

  @Test
  public void testOnEventAfterSuccess() throws IOException, TimeoutException {
    EventKey<TestEvent> key = EventKey.of(TestEvent.class, TransactionPhase.AFTER_SUCCESS);

    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.addEvent(key, new PublisherConfiguration(config, "exchange", routingKeyFunction,
        basicProperties, null, encoder, errorHandler, declarations));
    publisher.onEventAfterSuccess(new TestEvent());
    publisher.cleanUp();

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), any(), any());
    verify(channel).close();
  }
}
