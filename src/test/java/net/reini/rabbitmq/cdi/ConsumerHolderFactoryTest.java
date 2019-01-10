package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
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
  private ConnectionConfig configMock;

  private ExchangeDeclarationConfig exchangeDeclaration;
  private QueueDeclarationConfig queueDeclaration;

  @BeforeEach
  void prepare() {
    exchangeDeclaration = new ExchangeDeclarationConfig();
    queueDeclaration = new QueueDeclarationConfig();
  }

  @Test
  void testCreate() {
    ConsumerHolderFactory consumerHolderFactory = new ConsumerHolderFactory();
    ConsumerHolder consumerHolder =
        consumerHolderFactory.createConsumerHolder(eventConsumerMock, "queue", true,
            connectionRepositoryMock, configMock, exchangeDeclaration, queueDeclaration);
    assertNotNull(consumerHolder);
  }
}
