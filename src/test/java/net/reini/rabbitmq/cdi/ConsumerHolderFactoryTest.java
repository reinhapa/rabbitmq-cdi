package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsumerHolderFactoryTest {
  @Mock
  private EventConsumer<?> eventConsumerMock;
  @Mock
  private ConnectionRepository connectionRepositoryMock;
  @Mock
  private ConnectionConfig configMock;
  @Mock
  private List<Declaration> declarations;
  @Mock
  private DeclarerRepository declarerRepositoryMock;


  @Test
  void testCreate() {
    ConsumerHolderFactory consumerHolderFactory = new ConsumerHolderFactory();
    ConsumerHolder consumerHolder =
        consumerHolderFactory.createConsumerHolder(eventConsumerMock, "queue", true, 0,
            connectionRepositoryMock, configMock, declarations, declarerRepositoryMock);
    assertNotNull(consumerHolder);
  }
}
