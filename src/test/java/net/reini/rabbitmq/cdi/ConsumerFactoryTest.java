package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsumerFactoryTest {
  @Mock
  private EventConsumer eventConsumerMock;
  @Mock
  private Channel channelMock;

  private ConsumerFactory sut = new ConsumerFactory();

  @Test
  void testCreate() {
    Consumer consumer = sut.create(eventConsumerMock);
    assertNotNull(consumer);
  }

  @Test
  void testCreateAcknowledged() {
    Consumer consumer = sut.createAcknowledged(eventConsumerMock,channelMock);
    assertNotNull(consumer);
  }

}