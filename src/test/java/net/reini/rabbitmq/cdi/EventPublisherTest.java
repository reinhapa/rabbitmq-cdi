package net.reini.rabbitmq.cdi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import javax.enterprise.event.TransactionPhase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Tests the {@link EventPublisher} implementation.
 *
 * @author Patrick Reinhart
 */
@ExtendWith(MockitoExtension.class)
public class EventPublisherTest {
  @Mock
  private ConnectionProducer connectionProducer;
  @Mock
  private ConnectionConfig config;
  @Mock
  private Connection connection;
  @Mock
  private Channel channel;
  @Mock
  private BiConsumer<TestEvent, PublishException> errorHandler;

  private EventPublisher publisher;
  private Builder basicProperties;
  private JsonEncoder<TestEvent> encoder;

  @BeforeEach
  public void setUp() throws Exception {
    publisher = new EventPublisher(connectionProducer);
    basicProperties = new BasicProperties.Builder();
    encoder = new JsonEncoder<>();
  }

  /**
   * Test method for {@link EventPublisher#publishEvent(Object, TransactionPhase)}.
   */
  @Test
  public void testPublishEvent_no_configuration() {
    publisher.publishEvent(new TestEvent(), TransactionPhase.AFTER_COMPLETION);
  }

  /**
   * Test method for {@link EventPublisher#addEvent(EventKey, PublisherConfiguration)},
   * {@link EventPublisher#publishEvent(Object, TransactionPhase)} and
   * {@link EventPublisher#cleanUp()}.
   * 
   * @throws TimeoutException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  @Test
  public void testPublishEvent() throws IOException, TimeoutException, NoSuchAlgorithmException {
    EventKey<TestEvent> key = EventKey.of(TestEvent.class, TransactionPhase.AFTER_SUCCESS);
    when(connectionProducer.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.addEvent(key, new PublisherConfiguration<>(config, "exchange", "routingKey",
        basicProperties, encoder, errorHandler));
    publisher.publishEvent(new TestEvent(), TransactionPhase.AFTER_SUCCESS);
    publisher.cleanUp();

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), any(), any());
    verify(channel).close();
  }

  /**
   * Test method for {@link EventPublisher#addEvent(EventKey, PublisherConfiguration)},
   * {@link EventPublisher#publishEvent(Object, TransactionPhase)} and
   * {@link EventPublisher#cleanUp()}.
   * 
   * @throws TimeoutException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  @Test
  public void testPublishEvent_failing()
      throws IOException, TimeoutException, NoSuchAlgorithmException {
    EventKey<TestEvent> key = EventKey.of(TestEvent.class, TransactionPhase.AFTER_FAILURE);

    when(connectionProducer.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);
    doThrow(IOException.class).when(channel).basicPublish(eq("exchange"), eq("routingKey"), any(),
        any());

    publisher.addEvent(key, new PublisherConfiguration<>(config, "exchange", "routingKey",
        basicProperties, encoder, errorHandler));
    publisher.publishEvent(new TestEvent(), TransactionPhase.AFTER_FAILURE);
    publisher.cleanUp();

    verify(channel, times(3)).close();
  }
}
