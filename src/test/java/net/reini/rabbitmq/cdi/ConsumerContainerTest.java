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
  private ConnectionConfig connectionConfigMock;
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
  @Mock
  private DeclarerRepository declarerRepositoryMock;

  private List<Declaration> declarations=new ArrayList<>();

  @Test
  void testAddConsumerHolder() {
    List<ConsumerHolder> consumerHolders = new ArrayList<>();
    QueueDeclaration declaration = new QueueDeclaration(EXPECTED_QUEUE_NAME);
    declarations.add(declaration);
    ConsumerContainer sut =
        new ConsumerContainer(connectionConfigMock, connectionRepositoryMock,declarerRepositoryMock, consumerHolders,
             consumerHolderFactoryMock, lockMock);
    when(consumerHolderFactoryMock.createConsumerHolder(consumerMock, EXPECTED_QUEUE_NAME,
        EXPECTED_AUTOACK, 0, connectionRepositoryMock, connectionConfigMock, declarations,declarerRepositoryMock)).thenReturn(consumerHolderMock);
    sut.addConsumer(consumerMock, EXPECTED_QUEUE_NAME, EXPECTED_AUTOACK, 0, declarations);

    assertEquals(1, consumerHolders.size());
    ConsumerHolder consumerHolder = consumerHolders.get(0);
    assertSame(consumerHolderMock, consumerHolder);
  }

  @Test
  void testEnsureAllConsumerAreActive() throws IOException {
    List<ConsumerHolder> consumerHolders = new ArrayList<>();
    consumerHolders.add(consumerHolderMock);
    ConsumerContainer sut =
        new ConsumerContainer(null, null,declarerRepositoryMock, consumerHolders, null, lockMock);
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
    ConsumerContainer sut =
        new ConsumerContainer(null, null,declarerRepositoryMock, consumerHolders, null, lockMock);
    boolean result = sut.ensureConsumersAreActive();
    verify(consumerHolderMock).activate();
    assertFalse(result);

  }

  @Test
  void testDeactivateAllConsumers() {
    List<ConsumerHolder> consumerHolders = new ArrayList<>();
    consumerHolders.add(consumerHolderMock);
    ConsumerContainer sut =
        new ConsumerContainer(null, null,declarerRepositoryMock, consumerHolders, null, lockMock);
    sut.deactivateAllConsumer();
    verify(consumerHolderMock).deactivate();
  }


  @Test
  void testStartAndStopConsumerContainer() {
    ConsumerContainer sut = new ConsumerContainer(connectionConfigMock, connectionRepositoryMock,declarerRepositoryMock,
        null, null, lockMock);
    sut.start();
    sut.stop();
  }

  @Test
  void testSetConnectionAvailable() {
    ConsumerContainer sut = new ConsumerContainer(null, null,declarerRepositoryMock, null, null, lockMock);
    assertFalse(sut.isConnectionAvailable());
    sut.setConnectionAvailable(true);
    assertTrue(sut.isConnectionAvailable());
    sut.setConnectionAvailable(false);
    assertFalse(sut.isConnectionAvailable());
  }
}
