package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.MissedHeartbeatException;
import com.rabbitmq.client.ShutdownSignalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsumerHolderChannelShutdownListenerTest
{

    @Mock
    private ConsumerHolder consumerHolderMock;

    @Mock
    ShutdownSignalException shutdownExceptionMock;

    @Test
    void testShutdownBecauseOfApplicationException()
    {
        ConsumerHolderChannelShutdownListener sut = new ConsumerHolderChannelShutdownListener(consumerHolderMock);
        Mockito.when(shutdownExceptionMock.isInitiatedByApplication()).thenReturn(true);
        sut.shutdownCompleted(shutdownExceptionMock);;
        Mockito.verify(consumerHolderMock).ensureCompleteShutdown();
    }

    @Test
    void testShutdownBecauseOfMissedHeartbeatException()
    {
        ConsumerHolderChannelShutdownListener sut = new ConsumerHolderChannelShutdownListener(consumerHolderMock);
        Mockito.when(shutdownExceptionMock.isInitiatedByApplication()).thenReturn(false);

        Mockito.when(shutdownExceptionMock.getCause()).thenReturn(new MissedHeartbeatException(""));
        sut.shutdownCompleted(shutdownExceptionMock);;
        Mockito.verify(consumerHolderMock,Mockito.never()).ensureCompleteShutdown();
    }

    @Test
    void testRecoverableShutdown()
    {
        ConsumerHolderChannelShutdownListener sut = new ConsumerHolderChannelShutdownListener(consumerHolderMock);
        Mockito.when(shutdownExceptionMock.isInitiatedByApplication()).thenReturn(false);
        sut.shutdownCompleted(shutdownExceptionMock);;
        Mockito.verify(consumerHolderMock,Mockito.never()).ensureCompleteShutdown();
    }

}