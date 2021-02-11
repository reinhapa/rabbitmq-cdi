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
import static org.mockito.Mockito.when;

import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.ShutdownSignalException;

@SuppressWarnings("boxing")
@ExtendWith(MockitoExtension.class)
class ConnectionShutdownListenerTest {
  @Mock
  private ConnectionManager connectionManager;
  @Mock
  private ShutdownSignalException shutdownSignalExceptionMock;
  @Mock
  private ReentrantLock connectionManagerLock;
  @InjectMocks
  private ConnectionShutdownListener sut;

  @Test
  void testRecoverableShutdown() {
    when(shutdownSignalExceptionMock.isInitiatedByApplication()).thenReturn(true);
    when(connectionManager.getState()).thenReturn(ConnectionState.CONNECTED);
    sut.shutdownCompleted(shutdownSignalExceptionMock);
    verify(connectionManager, Mockito.times(1)).changeState(ConnectionState.CONNECTING);
  }

  @Test
  void testShutdownAfterClose() {
    when(shutdownSignalExceptionMock.isInitiatedByApplication()).thenReturn(true);
    when(connectionManager.getState()).thenReturn(ConnectionState.CLOSED);
    sut.shutdownCompleted(shutdownSignalExceptionMock);
    verify(connectionManager, Mockito.never()).changeState(ConnectionState.CONNECTING);
  }

  @Test
  void testShutdownAfterAlreadyStartingToRetry() {
    when(shutdownSignalExceptionMock.isInitiatedByApplication()).thenReturn(true);
    when(connectionManager.getState()).thenReturn(ConnectionState.CONNECTING);
    sut.shutdownCompleted(shutdownSignalExceptionMock);
    verify(connectionManager, Mockito.never()).changeState(ConnectionState.CONNECTING);
  }

}
