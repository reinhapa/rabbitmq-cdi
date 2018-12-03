package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

@ExtendWith(MockitoExtension.class)
public class GenericPublisherTest {
  @Mock
  private ConnectionConfig config;
  @Mock
  private ConnectionProducer connectionProducer;
  @Mock
  private Connection connection;
  @Mock
  private Channel channel;
  @Mock
  BiConsumer<?, PublishException> errorHandler;

  private GenericPublisher publisher;
  private TestEvent event;

  @BeforeEach
  public void setUp() throws Exception {
    publisher = new GenericPublisher(connectionProducer) {
      @Override
      protected void sleepBeforeRetry() {
        // no delay
      }
    };
    event = new TestEvent();
    event.id = "theId";
    event.booleanValue = true;
  }

  @Test
  public void testPublish() throws Exception {
    Builder builder = new Builder();
    PublisherConfiguration publisherConfiguration = new PublisherConfiguration(config, "exchange",
        "routingKey", builder, new JsonEncoder<>(), errorHandler);
    ArgumentCaptor<BasicProperties> propsCaptor = ArgumentCaptor.forClass(BasicProperties.class);

    when(connectionProducer.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.publish(event, publisherConfiguration);

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), propsCaptor.capture(),
        eq("{\"id\":\"theId\",\"booleanValue\":true}".getBytes()));
    assertEquals("application/json", propsCaptor.getValue().getContentType());
  }

  @Test
  public void testPublish_with_error() throws Exception {
    Builder builder = new Builder();
    PublisherConfiguration publisherConfiguration = new PublisherConfiguration(config, "exchange",
        "routingKey", builder, new JsonEncoder<>(), errorHandler);
    ArgumentCaptor<BasicProperties> propsCaptor = ArgumentCaptor.forClass(BasicProperties.class);

    when(connectionProducer.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);
    doThrow(new IOException("someError")).when(channel).basicPublish(eq("exchange"),
        eq("routingKey"), propsCaptor.capture(),
        eq("{\"id\":\"theId\",\"booleanValue\":true}".getBytes()));

    Throwable exception = assertThrows(PublishException.class, () -> {
      publisher.publish(event, publisherConfiguration);
    });
    assertEquals("Unable to send message after 3 attempts", exception.getMessage());
    assertEquals("application/json", propsCaptor.getValue().getContentType());
  }

  @Test
  public void testPublish_with_custom_MessageConverter() throws Exception {
    Builder builder = new Builder();
    PublisherConfiguration publisherConfiguration = new PublisherConfiguration(config, "exchange",
        "routingKey", builder, new CustomEncoder(), errorHandler);
    ArgumentCaptor<BasicProperties> propsCaptor = ArgumentCaptor.forClass(BasicProperties.class);

    when(connectionProducer.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.publish(event, publisherConfiguration);

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), propsCaptor.capture(),
        eq("Id: theId, BooleanValue: true".getBytes()));
    assertEquals("text/plain", propsCaptor.getValue().getContentType());
  }

  public static class CustomEncoder implements Encoder<TestEvent> {
    @Override
    public String contentType() {
      return "text/plain";
    }

    @Override
    @SuppressWarnings("boxing")
    public byte[] encode(TestEvent event) throws EncodeException {
      final String str =
          MessageFormat.format("Id: {0}, BooleanValue: {1}", event.getId(), event.isBooleanValue());
      return str.getBytes();
    }
  }
}
