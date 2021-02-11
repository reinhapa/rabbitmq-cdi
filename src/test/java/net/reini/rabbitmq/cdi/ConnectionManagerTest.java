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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@SuppressWarnings("boxing")
@ExtendWith(MockitoExtension.class)
class ConnectionManagerTest {
  @Mock
  private ConnectionConfig configMock;
  @Mock
  private ConnectionManagerWatcherThread watcherThreadMock;
  @Mock
  private ConnectionShutdownListener shutdownListenerMock;
  @Mock
  private ConnectionFactory connectionFactoryMock;
  @Mock
  private ConnectionListener listener;
  @Mock
  private Connection connectionMock;
  @Mock
  private ReentrantLock lockMock;
  @Mock
  private Condition conditionMock;

  private ConnectionManager sut;

  @BeforeEach
  public void setUp() {
    sut = new ConnectionManager(configMock, watcherThreadMock, shutdownListenerMock,
        connectionFactoryMock, lockMock, conditionMock);
    sut.addListener(listener);
  }

  @Test
  public void testConnect() {
    sut.connect();
    verify(watcherThreadMock).start();
  }

  @Test
  public void testGetConnection() {
    Throwable exception = assertThrows(IOException.class, () -> {
      sut.getConnection();
    });
    assertEquals(exception.getMessage(), "Unable to retrieve connection");
  }


  @Test
  public void getConnection_closed() throws Exception {
    sut.changeState(ConnectionState.CLOSED);
    verify(listener).onConnectionClosed(null);
    Throwable exception = assertThrows(IOException.class, () -> {
      sut.getConnection();
    });
    assertEquals(exception.getMessage(),
        "Attempt to retrieve a connection from a closed connection factory");
  }

  @Test
  public void getConnect_closed() throws Exception {
    sut.changeState(ConnectionState.CLOSED);
    verify(listener).onConnectionClosed(null);
    Throwable exception = assertThrows(IllegalStateException.class, () -> {
      sut.connect();
    });
    assertEquals(exception.getMessage(),
        "Attempt to initiate a connect from a closed connection manager");
  }


  @Test
  public void testNotifyConnecting() {
    sut.changeState(ConnectionState.CONNECTING);
    verify(conditionMock).signalAll();
    verify(listener).onConnectionLost(null);
  }

  @Test
  public void testNotifyConnectedWithExceptionInListener() {
    doThrow(new RuntimeException()).when(listener).onConnectionEstablished(null);
    sut.changeState(ConnectionState.CONNECTED);
    verify(listener).onConnectionEstablished(null);
  }

  @Test
  public void testNotifyConnected() {
    sut.changeState(ConnectionState.CONNECTED);
    verify(listener).onConnectionEstablished(null);
  }

  @Test
  public void testRemoveListener() {
    sut.removeListener(listener);
    sut.changeState(ConnectionState.CONNECTING);
    sut.changeState(ConnectionState.CONNECTED);
    sut.changeState(ConnectionState.CLOSED);
    sut.changeState(ConnectionState.NEVER_CONNECTED);
    verify(listener, never()).onConnectionLost(Mockito.any());
    verify(listener, never()).onConnectionEstablished(Mockito.any());
    verify(listener, never()).onConnectionClosed(Mockito.any());
  }

  @Test
  public void testContainsListener() {
    assertTrue(sut.containsListener(listener));
  }

  @Test
  public void testCreateConnection()
      throws TimeoutException, IOException {
    when(configMock.createConnection(connectionFactoryMock)).thenReturn(connectionMock);
    boolean result = sut.tryToEstablishConnection();
    assertTrue(result);
    assertEquals(connectionMock, sut.getConnection());
    verify(connectionMock).addShutdownListener(shutdownListenerMock);
    verify(listener).onConnectionEstablished(connectionMock);
    verify(listener, never()).onConnectionLost(Mockito.any());
    verify(listener, never()).onConnectionClosed(Mockito.any());

  }

  @Test
  public void testCreateConnectionMultipleTimes()
      throws TimeoutException, IOException {
    when(configMock.createConnection(connectionFactoryMock)).thenReturn(connectionMock);
    boolean result = sut.tryToEstablishConnection();
    assertTrue(result);
    verify(connectionMock).addShutdownListener(shutdownListenerMock);

    assertThrows(IllegalStateException.class, () -> {
      sut.tryToEstablishConnection();
    });

    verify(listener).onConnectionEstablished(connectionMock);
    verify(listener, never()).onConnectionLost(Mockito.any());
    verify(listener, never()).onConnectionClosed(Mockito.any());

  }


  @Test
  public void testCreateConnectionWithIoException()
      throws TimeoutException, IOException {
    doThrow(new IOException()).when(configMock).createConnection(connectionFactoryMock);
    boolean result = sut.tryToEstablishConnection();
    assertFalse(result);
    verify(listener, never()).onConnectionEstablished(connectionMock);
    verify(listener, never()).onConnectionLost(Mockito.any());
    verify(listener, never()).onConnectionClosed(Mockito.any());

  }

  @Test
  public void testCreateConnectionWithTimeoutException()
      throws TimeoutException, IOException {
    doThrow(new TimeoutException()).when(configMock).createConnection(connectionFactoryMock);
    boolean result = sut.tryToEstablishConnection();
    assertFalse(result);
    verify(listener, never()).onConnectionEstablished(connectionMock);
    verify(listener, never()).onConnectionLost(Mockito.any());
    verify(listener, never()).onConnectionClosed(Mockito.any());
  }

  @Test
  public void testCreateConnectionAndCloseAgain()
      throws TimeoutException, IOException {
    when(configMock.createConnection(connectionFactoryMock)).thenReturn(connectionMock);
    when(watcherThreadMock.isRunning()).thenReturn(true);
    boolean result = sut.tryToEstablishConnection();
    assertTrue(result);
    verify(connectionMock).addShutdownListener(shutdownListenerMock);
    verify(listener).onConnectionEstablished(connectionMock);
    verify(listener, never()).onConnectionLost(Mockito.any());
    verify(listener, never()).onConnectionClosed(Mockito.any());

    sut.close();
    verify(connectionMock).removeShutdownListener(shutdownListenerMock);
    verify(connectionMock).close();
    assertEquals(ConnectionState.CLOSED, sut.getState());
    verify(listener).onConnectionClosed(null);
    verify(watcherThreadMock).stopThread();
  }

  @Test
  public void testMultipleClose() {
    sut.close();
    sut.close();
    verify(listener).onConnectionClosed(null);
  }

  @Test
  void testNotRunningThreadAndCloseCalled() {
    when(watcherThreadMock.isRunning()).thenReturn(false);
    sut.close();
    verify(watcherThreadMock).isRunning();
    verify(watcherThreadMock, never()).stopThread();
  }

  @Test
  void testConstructor() {
    ConnectionManager connectionManager = new ConnectionManager(configMock);
    assertNotNull(connectionManager);
  }
}

