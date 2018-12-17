package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.verify;

import com.rabbitmq.client.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ContainerConnectionListenerTest
{
    @Mock
    private ConsumerContainer consumerContainerMock;
    @Mock
    private Connection conectionMock;
    @Mock
    private ContainerConnectionListener.NotifyWrapper notifyWrapperMock;

    @Test
    void testOnConnectionClosed()
    {
        ContainerConnectionListener sut = new ContainerConnectionListener(consumerContainerMock);
        sut.onConnectionClosed(conectionMock);
        verify(consumerContainerMock,Mockito.times(1)).setConnectionAvailable(false);
        verify(consumerContainerMock,Mockito.times(1)).deactivateAllConsumer();

    }

    @Test
    void testOnConnectionLost()
    {
        ContainerConnectionListener sut = new ContainerConnectionListener(consumerContainerMock);
        sut.onConnectionLost(conectionMock);
        verify(consumerContainerMock,Mockito.times(1)).setConnectionAvailable(false);
        verify(consumerContainerMock,Mockito.times(1)).deactivateAllConsumer();
    }

    @Test
    void testOnConnectionEstablished()
    {
        ContainerConnectionListener sut = new ContainerConnectionListener(consumerContainerMock,notifyWrapperMock);
        sut.onConnectionEstablished(conectionMock);
        verify(consumerContainerMock,Mockito.times(1)).setConnectionAvailable(true);
        verify(notifyWrapperMock,Mockito.times(1)).notifyThread();

    }

}