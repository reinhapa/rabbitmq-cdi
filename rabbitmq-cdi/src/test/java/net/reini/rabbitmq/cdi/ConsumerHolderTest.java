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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

@ExtendWith(MockitoExtension.class)
class ConsumerHolderTest {
  private static final int PREFETCH_COUNT = 5;
  @Mock
  private EventConsumer<TestEvent> eventConsumerMock;
  @Mock
  private ConsumerChannelFactory consumerChannelFactoryMock;
  @Mock
  private Channel channelMock;
  @Mock
  private ConsumerFactory consumerFactoryMock;
  @Mock
  private Consumer consmerMock;
  @Mock
  private List<Declaration> declarationsListMock;
  @Mock
  private DeclarerRepository declarerRepositoryMock;

  private ConsumerHolder sut;


  @Test
  void activateAndDeactivate() throws IOException, TimeoutException {
    this.sut = new ConsumerHolder(eventConsumerMock, "queue", false, PREFETCH_COUNT, consumerChannelFactoryMock,
        consumerFactoryMock, declarationsListMock,declarerRepositoryMock);
    Assertions.assertEquals("queue", sut.getQueueName());
    Assertions.assertFalse(sut.isAutoAck());
    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    when(consumerFactoryMock.createAcknowledged(eventConsumerMock, channelMock))
        .thenReturn(consmerMock);
    sut.activate();
    verify(channelMock).addShutdownListener(any());
    verify(declarerRepositoryMock).declare(channelMock, declarationsListMock);
    verify(channelMock, never()).close();
    verify(channelMock, never()).removeShutdownListener(any());
    verify(channelMock).basicConsume("queue", false, consmerMock);
    sut.deactivate();
    verify(channelMock).close();
    verify(channelMock).removeShutdownListener(any());
  }

  @Test
  void activateAndDeactivateWithAutoAck() throws IOException, TimeoutException {
    this.sut = new ConsumerHolder(eventConsumerMock, "queue", true, PREFETCH_COUNT, consumerChannelFactoryMock,
        consumerFactoryMock,declarationsListMock,declarerRepositoryMock);
    Assertions.assertEquals("queue", sut.getQueueName());
    Assertions.assertTrue(sut.isAutoAck());
    when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
    when(consumerFactoryMock.create(eventConsumerMock)).thenReturn(consmerMock);
    sut.activate();
    verify(channelMock).addShutdownListener(any());
    verify(channelMock).basicConsume("queue", true, consmerMock);
    verify(declarerRepositoryMock).declare(channelMock,declarationsListMock);
    verify(channelMock, never()).close();
    verify(channelMock, never()).removeShutdownListener(any());
    verify(channelMock).basicQos(PREFETCH_COUNT);

    sut.deactivate();
    verify(channelMock).close();
    verify(channelMock).removeShutdownListener(any());
  }

  @Test
  void errorDuringActivate() {
    Assertions.assertThrows(IOException.class, () -> {
      this.sut = new ConsumerHolder(eventConsumerMock, "queue", true, 0, consumerChannelFactoryMock,
          consumerFactoryMock,declarationsListMock,declarerRepositoryMock);
      when(consumerChannelFactoryMock.createChannel()).thenReturn(channelMock);
      when(consumerFactoryMock.create(eventConsumerMock)).thenReturn(consmerMock);
      doThrow(new IOException()).when(channelMock).basicConsume("queue", true, consmerMock);
      sut.activate();
      verify(channelMock).addShutdownListener(any());
      verify(declarerRepositoryMock).declare(channelMock,declarationsListMock);
      verify(channelMock).close();
      verify(channelMock).removeShutdownListener(any());
    });
  }

}
