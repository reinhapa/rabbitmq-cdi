/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015, 2019 Patrick Reinhart
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding;
import net.reini.rabbitmq.cdi.EventBinder.QueueBinding;

@ExtendWith(MockitoExtension.class)
class EventBinderTest {
  @Mock
  private Event<Object> remoteEventControl;
  @Mock
  private Instance<Object> remoteEventPool;
  @Mock
  private EventPublisher eventPublisher;
  @Mock
  private ConnectionRepository connectionRepository;
  @Mock
  private ConsumerContainerFactory consumerContainerFactory;
  @Mock
  private ConsumerContainer consumerContainerMock;
  @Mock
  private ReentrantLock lockMock;
  @Mock
  private Condition conditionMock;

  @InjectMocks
  private TestEventBinder eventBinder;

  @BeforeEach
  void prepare() {
    Mockito.when(consumerContainerFactory.create(Mockito.any(), Mockito.any(),Mockito.any()))
        .thenReturn(consumerContainerMock);
    eventBinder.initializeConsumerContainer();
  }

  @Test
  void testStop() {

    eventBinder.stop();
    verify(consumerContainerMock).stop();
  }


  @Test
  void testBind() {
    assertNotNull(eventBinder.bind(TestEvent.class));
  }

  @Test
  void testConfiguration() {
    assertNotNull(eventBinder.configuration());
  }

  @Test
  void testInitialize() throws IOException {
    eventBinder.initialize();
  }

  @Test
  void testBindQueue() throws IOException {
    QueueBinding<TestEvent> queueBinding = new QueueBinding<>(TestEvent.class, "queue");

    eventBinder.bindQueue(queueBinding);
    eventBinder.initialize();
  }

  @Test
  void testBindExchange() throws IOException {
    ExchangeBinding<TestEvent> exchangeBinding = new ExchangeBinding<>(TestEvent.class, "exchange");

    eventBinder.bindExchange(exchangeBinding);
    eventBinder.initialize();
  }

  @Test
  void testUriDecode() {
    assertEquals("stock + stein", EventBinder.uriDecode("stock%20+%20stein"));
  }

  @Test
  void testDeclarerFactoryNotNUll() {
    assertNotNull(eventBinder.declarerFactory());
  }

  @Test
  void testAddListener() throws IOException {
    ContainerConnectionListener sut =
            new ContainerConnectionListener(consumerContainerMock, lockMock, conditionMock);
    eventBinder.registerConnectionListener( sut );
    verify(connectionRepository, Mockito.times(1)).registerConnectionListener( Mockito.any(), Mockito.eq(sut) );
  }

  @Test
  void testRemoveListener() throws IOException {
    ContainerConnectionListener sut =
            new ContainerConnectionListener(consumerContainerMock, lockMock, conditionMock);
    eventBinder.removeConnectionListener( sut );
    verify(connectionRepository, Mockito.times(1)).removeConnectionListener( Mockito.any(), Mockito.eq(sut) );
  }

  static class TestEventBinder extends EventBinder {
    @Override
    protected void bindEvents() {
    }
  }
}
