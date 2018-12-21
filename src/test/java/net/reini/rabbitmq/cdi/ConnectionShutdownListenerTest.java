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
