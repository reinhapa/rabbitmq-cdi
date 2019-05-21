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
