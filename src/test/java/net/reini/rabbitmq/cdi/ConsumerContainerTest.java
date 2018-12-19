package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsumerContainerTest {

  private static final String EXPECTED_QUEUE_NAME = "queue";
  private static final boolean EXPECTED_AUTOACK = true;

  @Mock
  private ConnectionRepository connectionRepositoryMock;
  @Mock
  private CopyOnWriteArrayList<ConsumerHolder> consumerHolderListMock;
  @Mock
  private ExchangeDeclarationConfig exchangeDeclarationConfigMock;
  @Mock
  private QueueDeclarationConfig queueDeclarationConfigMock;
  @Mock
  private ExchangeDeclaration exchangeDeclarationMock;
  @Mock
  private QueueDeclaration queueDeclarationMock;
  @Mock
  private ConnectionConfiguration connectionConfigMock;
  @Mock
  private EventConsumer consumerMock;
  @Mock
  private ConsumerHolderFactory consumerHolderFactoryMock;
  @Mock
  private ConsumerHolder consumerHolderMock;
  @Mock
  private ConsumerHolder consumerHolderMock2;
  @Mock
  private ReentrantLock lockMock;

  @Test
  void testDelegateExchangeDeclaration() {
    ConsumerContainer sut = new ConsumerContainer(null, null, null, exchangeDeclarationConfigMock, null, null,lockMock);
    sut.addExchangeDeclaration(exchangeDeclarationMock);
    verify(exchangeDeclarationConfigMock).addExchangeDeclaration(exchangeDeclarationMock);
  }

  @Test
  void testDelegateQueueDeclaration() {
    ConsumerContainer sut = new ConsumerContainer(null, null, null, null, queueDeclarationConfigMock, null,lockMock);
    sut.addQueueDeclaration(queueDeclarationMock);
    verify(queueDeclarationConfigMock).addQueueDeclaration(queueDeclarationMock);
  }

  @Test
  void testAddConsumerHolder() {
    List<ConsumerHolder> consumerHolders = new ArrayList<>();
    ConsumerContainer sut = new ConsumerContainer(connectionConfigMock, connectionRepositoryMock, consumerHolders, exchangeDeclarationConfigMock, queueDeclarationConfigMock,
        consumerHolderFactoryMock,lockMock);
    when(consumerHolderFactoryMock
        .createConsumerHolder(consumerMock, EXPECTED_QUEUE_NAME, EXPECTED_AUTOACK, connectionRepositoryMock, connectionConfigMock, exchangeDeclarationConfigMock, queueDeclarationConfigMock))
        .thenReturn(consumerHolderMock);
    sut.addConsumer(consumerMock, EXPECTED_QUEUE_NAME, EXPECTED_AUTOACK);

    assertEquals(1, consumerHolders.size());
    ConsumerHolder consumerHolder = consumerHolders.get(0);
    assertSame(consumerHolderMock, consumerHolder);
  }

  @Test
  void testEnsureAllConsumerAreActive() throws IOException {
    List<ConsumerHolder> consumerHolders = new ArrayList<>();
    consumerHolders.add(consumerHolderMock);
    ConsumerContainer sut = new ConsumerContainer(null, null, consumerHolders, null, null, null,lockMock);
    boolean result = sut.ensureConsumersAreActive();
    verify(consumerHolderMock).activate();
    assertTrue(result);

  }

  @Test
  void testEnsureAllConsumerAreActiveWith2ConsumerAndOneFailing() throws IOException {
    List<ConsumerHolder> consumerHolders = new ArrayList<>();
    consumerHolders.add(consumerHolderMock2);
    consumerHolders.add(consumerHolderMock);
    doThrow(new IOException("")).when(consumerHolderMock2).activate();
    ConsumerContainer sut = new ConsumerContainer(null, null, consumerHolders, null, null, null,lockMock);
    boolean result = sut.ensureConsumersAreActive();
    verify(consumerHolderMock).activate();
    assertFalse(result);

  }

  @Test
  void testDeactivateAllConsumers() {
    List<ConsumerHolder> consumerHolders = new ArrayList<>();
    consumerHolders.add(consumerHolderMock);
    ConsumerContainer sut = new ConsumerContainer(null, null, consumerHolders, null, null, null,lockMock);
    sut.deactivateAllConsumer();
    verify(consumerHolderMock).deactivate();
  }


  @Test
  void testStartAndStopConsumerContainer(){
    ConsumerContainer sut = new ConsumerContainer(connectionConfigMock, connectionRepositoryMock, null, null, null, null,lockMock);
    sut.start();
    sut.stop();

  }
  
}
