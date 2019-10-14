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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerShutdownSignalCallback;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.RecoverableChannel;
import com.rabbitmq.client.ShutdownSignalException;

@SuppressWarnings("boxing")
@ExtendWith(MockitoExtension.class)
class ConsumerHolderTest {
  private static final int PREFETCH_COUNT = 5;
  @Mock
  private EventConsumer<TestEvent> eventConsumerMock;
  @Mock
  private ConsumerChannelFactory consumerChannelFactoryMock;
  @Mock
  private Supplier<Channel> channelSupplierMock;
  @Mock
  private RecoverableChannel channelMock;
  @Mock
  private List<Declaration> declarationsListMock;
  @Mock
  private DeclarerRepository declarerRepositoryMock;

  private ExecutorService executor;
  private ConsumerHolder sut;

  @AfterEach
  void cleanUp() {
    if (executor != null && executor.isTerminated()) {
      executor.shutdownNow();
    }
  }

  @Test
  void activateAndDeactivate() throws IOException, TimeoutException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", false, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    Assertions.assertEquals("queue", sut.getQueueName());
    Assertions.assertFalse(sut.isAutoAck());
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
  void activateAndDeactivateWithAutoAck() throws IOException, TimeoutException {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);
    Assertions.assertEquals("queue", sut.getQueueName());
    Assertions.assertTrue(sut.isAutoAck());
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
    Assertions.assertThrows(IOException.class, () -> {
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

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    when(eventConsumerMock.consume("consumerTag", envelope, properties, body)).thenReturn(false);
    doThrow(new IOException("")).when(channelMock).basicNack(123L, false, false);

    sut.activate();
    sut.deliverWithAck("consumerTag", message);
  }

  @Test
  void ensureCompleteShutdown() {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);

    sut.ensureCompleteShutdown();
  }

  @Test
  void ensureOpenChannel()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    executor = Executors.newSingleThreadExecutor();
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);

    sut.activate();

    executor.submit(sut::ensureOpenChannel).get(2, TimeUnit.SECONDS);

    sut.handleRecoveryStarted(channelMock);
    assertThrows(TimeoutException.class,
        () -> executor.submit(sut::ensureOpenChannel).get(1, TimeUnit.SECONDS));

    sut.handleRecovery(channelMock);
    executor.submit(sut::ensureOpenChannel).get(2, TimeUnit.SECONDS);

    sut.ensureOpenChannel();
  }

  @Test
  void ensureOpenChannelWaitInterrupted() throws IOException {
    executor = Executors.newSingleThreadExecutor();
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);

    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);

    sut.activate();

    sut.handleRecoveryStarted(channelMock);

    executor.submit(sut::ensureOpenChannel);
    executor.shutdownNow();
  }

  @Test
  void handleShutdownSignal() {
    sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT,
        consumerChannelFactoryMock, declarationsListMock, declarerRepositoryMock);

    sut.handleShutdownSignal("consumerTag", new ShutdownSignalException(false, false, null, null));
  }
}
