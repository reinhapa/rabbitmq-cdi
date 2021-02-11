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

import static org.mockito.Mockito.verify;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Connection;

@ExtendWith(MockitoExtension.class)
public class ContainerConnectionListenerTest {
  @Mock
  private ConsumerContainer consumerContainerMock;
  @Mock
  private Connection conectionMock;
  @Mock
  private ReentrantLock lockMock;
  @Mock
  private Condition conditionMock;

  @Test
  void testOnConnectionClosed() {
    ContainerConnectionListener sut =
        new ContainerConnectionListener(consumerContainerMock, lockMock, conditionMock);
    sut.onConnectionClosed(conectionMock);
    verify(consumerContainerMock, Mockito.times(1)).setConnectionAvailable(false);
    verify(consumerContainerMock, Mockito.times(1)).deactivateAllConsumer();

  }

  @Test
  void testOnConnectionLost() {
    ContainerConnectionListener sut =
        new ContainerConnectionListener(consumerContainerMock, lockMock, conditionMock);
    sut.onConnectionLost(conectionMock);
    verify(consumerContainerMock, Mockito.times(1)).setConnectionAvailable(false);
    verify(consumerContainerMock, Mockito.times(1)).deactivateAllConsumer();
  }

  @Test
  void testOnConnectionEstablished() {
    ContainerConnectionListener sut =
        new ContainerConnectionListener(consumerContainerMock, lockMock, conditionMock);
    sut.onConnectionEstablished(conectionMock);
    verify(consumerContainerMock, Mockito.times(1)).setConnectionAvailable(true);
    verify(conditionMock, Mockito.times(1)).signalAll();

  }

}
