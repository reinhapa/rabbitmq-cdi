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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ConsumerShutdownSignalCallback;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.RecoverableChannel;
import com.rabbitmq.client.ShutdownSignalException;

import net.reini.rabbitmq.cdi.ConsumerHolder.AckAction;

@ExtendWith(MockitoExtension.class)
class ConsumerHolderTest {
  private static final int PREFETCH_COUNT = 5;
  @Mock
  private EventConsumer<TestEvent> eventConsumerMock;
  @Mock
  private ConsumerChannelFactory consumerChannelFactoryMock;
  @Mock
  private RecoverableChannel channelMock;
  @Mock
  private List<Declaration> declarationsListMock;
  @Mock
  private DeclarerRepository declarerRepositoryMock;

  private ConsumerHolder sut;

  @Test
  void activateAndDeactivate() throws IOException, TimeoutException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", false, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    assertEquals("queue", sut.getQueueName());
    assertFalse(sut.isAutoAck());
    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    sut.activate();
    verify(channelMock).addRecoveryListener(sut);
    verify(declarerRepositoryMock).declare(channelMock, declarationsListMock);
    verify(channelMock, never()).close();
    verify(channelMock).basicConsume(eq("queue"), eq(false), isA(DeliverCallback.class),
        isA(ConsumerShutdownSignalCallback.class));
    sut.deactivate();
    verify(channelMock).close();
  }

  @Test
  void testActivationOrder() throws IOException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", false, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    assertEquals("queue", sut.getQueueName());
    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    sut.activate();
    assertDoesNotThrow(sut::activate);
    InOrder inOrder = inOrder(channelMock, declarerRepositoryMock);
    inOrder.verify(channelMock).addRecoveryListener(sut);
    inOrder.verify(channelMock).basicQos(PREFETCH_COUNT);
    inOrder.verify(declarerRepositoryMock).declare(channelMock, declarationsListMock);
    inOrder.verify(channelMock).basicConsume(eq("queue"), eq(false), isA(DeliverCallback.class),
        isA(ConsumerShutdownSignalCallback.class));
  }

  @Test
  void activateAndDeactivateWithAutoAck() throws IOException, TimeoutException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    assertEquals("queue", sut.getQueueName());
    assertTrue(sut.isAutoAck());
    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    sut.activate();
    verify(channelMock).addRecoveryListener(sut);
    verify(channelMock).basicConsume(eq("queue"), eq(true), isA(DeliverCallback.class),
        isA(ConsumerShutdownSignalCallback.class));
    verify(declarerRepositoryMock).declare(channelMock, declarationsListMock);
    verify(channelMock, never()).close();
    verify(channelMock).basicQos(PREFETCH_COUNT);

    sut.deactivate();
    verify(channelMock).close();
  }

  @Test
  void errorDuringActivate() {
    assertThrows(IOException.class, () -> {
      sut = new ConsumerHolder(eventConsumerMock, "queue", true, 0, consumerChannelFactoryMock,
          declarationsListMock, declarerRepositoryMock);
      when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
      doThrow(new IOException()).when(channelMock).basicConsume(eq("queue"), eq(true),
          isA(DeliverCallback.class), isA(ConsumerShutdownSignalCallback.class));
      sut.activate();
      verify(channelMock).addRecoveryListener(sut);
      verify(declarerRepositoryMock).declare(channelMock, declarationsListMock);
      verify(channelMock).close();
    });
  }

  @Test
  void deliverNoAck() throws IOException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    BasicProperties properties = MessageProperties.BASIC;
    byte[] body = "some body".getBytes();
    Envelope envelope = new Envelope(123L, false, "exchange", "routingKey");
    Delivery message = new Delivery(envelope, properties, body);

    sut.deliverNoAck("consumerTag", message);

    verify(eventConsumerMock).consume("consumerTag", envelope, properties, body);
  }

  @Test
  void deliverWithAckSuccess() throws IOException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    BasicProperties properties = MessageProperties.BASIC;
    byte[] body = "some body".getBytes();
    Envelope envelope = new Envelope(123L, false, "exchange", "routingKey");
    Delivery message = new Delivery(envelope, properties, body);

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    when(eventConsumerMock.consume("consumerTag", envelope, properties, body)).thenReturn(true);

    sut.activate();
    sut.deliverWithAck("consumerTag", message);

    verify(channelMock).basicAck(123L, false);
  }

  @Test
  void deliverWithAckSendFailed() throws IOException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    BasicProperties properties = MessageProperties.BASIC;
    byte[] body = "some body".getBytes();
    Envelope envelope = new Envelope(123L, false, "exchange", "routingKey");
    Delivery message = new Delivery(envelope, properties, body);

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    when(eventConsumerMock.consume("consumerTag", envelope, properties, body)).thenReturn(false);

    sut.activate();
    sut.deliverWithAck("consumerTag", message);

    verify(channelMock).basicNack(123L, false, false);
  }

  @Test
  void deliverWithAckFailedAck() throws IOException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    BasicProperties properties = MessageProperties.BASIC;
    byte[] body = "some body".getBytes();
    Envelope envelope = new Envelope(123L, false, "exchange", "routingKey");
    Delivery message = new Delivery(envelope, properties, body);
    IOException ioe = new IOException("some error");

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    when(eventConsumerMock.consume("consumerTag", envelope, properties, body)).thenReturn(false);
    doThrow(ioe).when(channelMock).basicNack(123L, false, false);

    sut.activate();
    assertThrows(IOException.class, () -> sut.deliverWithAck("consumerTag", message));
  }

  @Test
  void ensureCompleteShutdown() throws IOException, TimeoutException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);

    sut.activate();
    sut.ensureCompleteShutdown();

    verify(channelMock).close();
  }

  @Test
  void invokePendingAckAction() throws IOException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    AckAction action = mock(AckAction.class);

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);

    sut.activate();

    assertTrue(sut.invokePendingAckAction(action));
  }

  @Test
  void invokePendingAckActionRecoveryRunning() throws IOException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    AckAction action = mock(AckAction.class);

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);

    sut.activate();
    sut.handleRecoveryStarted(channelMock);

    assertFalse(sut.invokePendingAckAction(action));
  }

  @Test
  void invokePendingAckActionFailing() throws IOException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    AckAction action = mock(AckAction.class);

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    doThrow(new IOException("action failed")).when(action).apply(channelMock);

    sut.activate();
    sut.handleRecoveryStarted(null);

    assertFalse(sut.invokePendingAckAction(action));
  }

  @Test
  void handleShutdownSignal() {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);

    sut.handleShutdownSignal("consumerTag", new ShutdownSignalException(false, false, null, null));
  }

  @Test
  void invokeAckActionAndRecovery() throws IOException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    AckAction action1 = mock(AckAction.class);
    AckAction action2 = mock(AckAction.class);

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);

    sut.activate();
    sut.handleRecoveryStarted(channelMock);

    sut.invokeAckAction(action1);
    sut.invokeAckAction(action2);

    sut.handleRecovery(null);
    sut.handleRecovery(channelMock);
    sut.handleRecovery(channelMock);

    verify(action1).apply(channelMock);
    verify(action2).apply(channelMock);
  }
}
