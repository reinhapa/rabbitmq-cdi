package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.MissedHeartbeatException;
import com.rabbitmq.client.ShutdownSignalException;

@SuppressWarnings("boxing")
@ExtendWith(MockitoExtension.class)
class ConsumerHolderChannelShutdownListenerTest {

  @Mock
  private ConsumerHolder consumerHolderMock;

  @Mock
  ShutdownSignalException shutdownExceptionMock;

  @Test
  void testShutdownBecauseOfApplicationException() {
    ConsumerHolderChannelShutdownListener sut =
        new ConsumerHolderChannelShutdownListener(consumerHolderMock);
    when(shutdownExceptionMock.isInitiatedByApplication()).thenReturn(true);
    sut.shutdownCompleted(shutdownExceptionMock);
    verify(consumerHolderMock).ensureCompleteShutdown();
  }

  @Test
  void testShutdownBecauseOfMissedHeartbeatException() {
    ConsumerHolderChannelShutdownListener sut =
        new ConsumerHolderChannelShutdownListener(consumerHolderMock);
    when(shutdownExceptionMock.isInitiatedByApplication()).thenReturn(false);

    when(shutdownExceptionMock.getCause()).thenReturn(new MissedHeartbeatException(""));
    sut.shutdownCompleted(shutdownExceptionMock);
    verify(consumerHolderMock, Mockito.never()).ensureCompleteShutdown();
  }

  @Test
  void testRecoverableShutdown() {
    ConsumerHolderChannelShutdownListener sut =
        new ConsumerHolderChannelShutdownListener(consumerHolderMock);
    when(shutdownExceptionMock.isInitiatedByApplication()).thenReturn(false);
    sut.shutdownCompleted(shutdownExceptionMock);
    verify(consumerHolderMock, Mockito.never()).ensureCompleteShutdown();
  }

}
