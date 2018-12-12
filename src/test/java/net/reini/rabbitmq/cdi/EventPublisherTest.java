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
  private BiConsumer<?, PublishException> errorHandler;

  private EventPublisher publisher;
  private Builder basicProperties;
  private JsonEncoder<Object> encoder;

  @BeforeEach
  public void setUp() throws Exception {
    publisher = new EventPublisher(connectionProducer);
    basicProperties = new BasicProperties.Builder();
    encoder = new JsonEncoder<>();
  }

  /**
   * Test method for {@link EventPublisher#publishEvent(Object)}.
   */
  @Test
  public void testPublishEvent_no_configuration() {
    publisher.publishEvent(new TestEvent());
  }

  /**
   * Test method for {@link EventPublisher#addEvent(Class, PublisherConfiguration)},
   * {@link EventPublisher#publishEvent(Object)} and {@link EventPublisher#cleanUp()}.
   * 
   * @throws TimeoutException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  @Test
  public void testPublishEvent() throws IOException, TimeoutException, NoSuchAlgorithmException {
    when(connectionProducer.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.addEvent(TestEvent.class, new PublisherConfiguration(config, "exchange", "routingKey",
        basicProperties, encoder, errorHandler));
    publisher.publishEvent(new TestEvent());
    publisher.cleanUp();

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), any(), any());
    verify(channel).close();
  }

  /**
   * Test method for {@link EventPublisher#addEvent(Class, PublisherConfiguration)},
   * {@link EventPublisher#publishEvent(Object)} and {@link EventPublisher#cleanUp()}.
   * 
   * @throws TimeoutException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  @Test
  public void testPublishEvent_failing()
      throws IOException, TimeoutException, NoSuchAlgorithmException {
    when(connectionProducer.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);
    doThrow(IOException.class).when(channel).basicPublish(eq("exchange"), eq("routingKey"), any(),
        any());

    publisher.addEvent(TestEvent.class, new PublisherConfiguration(config, "exchange", "routingKey",
        basicProperties, encoder, errorHandler));
    publisher.publishEvent(new TestEvent());
    publisher.cleanUp();

    verify(channel, times(3)).close();
  }
}
