package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsumerHolderFactoryTest {

  @Mock
  private EventConsumer eventConsumerMock;
  @Mock
  private ConnectionRepository connectionRepositoryMock;
  @Mock
  private ConnectionConfiguration configMock;
  @Mock
  private ExchangeDeclarationConfig exchangeDeclarationMock;
  @Mock
  private QueueDeclarationConfig queueDeclarationMock;

  @Test
  void testCreate() {
    ConsumerHolderFactory consumerHolderFactory = new ConsumerHolderFactory();
    ConsumerHolder consumerHolder = consumerHolderFactory.createConsumerHolder(eventConsumerMock, "queue", true, connectionRepositoryMock, configMock, exchangeDeclarationMock, queueDeclarationMock);
    assertNotNull(consumerHolder);
  }
}