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
  private ConnectionRepository connectionRepository;
  @Mock
  private Connection connection;
  @Mock
  private Channel channel;
  @Mock
  private Encoder<TestEvent> encoder;
  @Mock
  private BiConsumer<?, PublishException> errorHandler;

  private GenericPublisher publisher;
  private TestEvent event;

  @BeforeEach
  public void setUp() throws Exception {
    publisher = new GenericPublisher(connectionRepository) {
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

    when(connectionRepository.getConnection(config)).thenReturn(connection);
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

    when(connectionRepository.getConnection(config)).thenReturn(connection);
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
  public void testPublish_withEncodeException() throws Exception {
    Builder builder = new Builder();
    PublisherConfiguration publisherConfiguration = new PublisherConfiguration(config, "exchange",
        "routingKey", builder, encoder, errorHandler);

    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);
    doThrow(new EncodeException(new RuntimeException("someError"))).when(encoder).encode(event);

    Throwable exception = assertThrows(PublishException.class, () -> {
      publisher.publish(event, publisherConfiguration);
    });
    assertEquals("Unable to serialize event", exception.getMessage());
  }

  @Test
  public void testPublish_withTooManyAttempts() throws Exception {
    publisher = new GenericPublisher(connectionRepository) {
      @Override
      protected void handleIoException(int attempt, Throwable cause) throws PublishException {
        // do not throw to allow attempts to overrun DEFAULT_RETRY_ATTEMPTS
      }

      @Override
      protected void sleepBeforeRetry() {
        // no delay
      }
    };

    Builder builder = new Builder();
    PublisherConfiguration publisherConfiguration = new PublisherConfiguration(config, "exchange",
        "routingKey", builder, new JsonEncoder<>(), errorHandler);
    ArgumentCaptor<BasicProperties> propsCaptor = ArgumentCaptor.forClass(BasicProperties.class);

    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);
    doThrow(new IOException("someError")).when(channel).basicPublish(eq("exchange"),
        eq("routingKey"), propsCaptor.capture(),
        eq("{\"id\":\"theId\",\"booleanValue\":true}".getBytes()));

    publisher.publish(event, publisherConfiguration);
    assertEquals("application/json", propsCaptor.getValue().getContentType());
  }

  @Test
  public void testPublish_with_FatalError() throws Exception {
    Builder builder = new Builder();
    PublisherConfiguration publisherConfiguration = new PublisherConfiguration(config, "exchange",
        "routingKey", builder, new JsonEncoder<>(), errorHandler);
    ArgumentCaptor<BasicProperties> propsCaptor = ArgumentCaptor.forClass(BasicProperties.class);

    when(connectionRepository.getConnection(config)).thenReturn(connection);
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

    when(connectionRepository.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.publish(event, publisherConfiguration);

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), propsCaptor.capture(),
        eq("Id: theId, BooleanValue: true".getBytes()));
    assertEquals("text/plain", propsCaptor.getValue().getContentType());
  }

  @Test
  public void testClose() {
    publisher.close();
  }

  @Test
  public void testHandleIoException_channel_null() throws PublishException {
    publisher.handleIoException(1, null);
  }

  @Test
  public void testSleepBeforeRetry_real_wait() {
    publisher = new GenericPublisher(connectionRepository);
    publisher.sleepBeforeRetry();
  }

  @Test
  public void testSleepBeforeRetry_InterruptedException() throws InterruptedException {
    publisher = new GenericPublisher(connectionRepository);
    call_SleepBeforeRetry_InAnotherThread_AndInterrupt();
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

  private void call_SleepBeforeRetry_InAnotherThread_AndInterrupt() throws InterruptedException {
    Thread sleeper = new Thread("sleeper") {
      @Override
      public void run() {
        publisher.sleepBeforeRetry();
      }
    };
    sleeper.start();
    Thread.sleep(GenericPublisher.DEFAULT_RETRY_INTERVAL / 4);
    sleeper.interrupt();
  }
}
