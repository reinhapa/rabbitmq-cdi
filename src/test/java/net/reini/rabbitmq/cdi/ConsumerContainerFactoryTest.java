package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsumerContainerFactoryTest {

  @Mock
  private ConnectionConfig configMock;
  @Mock
  private ConnectionRepository repositoryMock;
  @Mock
  private DeclarerRepository<QueueDeclaration> declarerRepositoryMock;

  @Test
  void testCreate() {
    ConsumerContainerFactory consumerContainerFactory = new ConsumerContainerFactory();
    ConsumerContainer consumerContainer =
        consumerContainerFactory.create(configMock, repositoryMock, declarerRepositoryMock);
    assertNotNull(consumerContainer);
  }
}
