package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublisherConfigurationTest {
  @Mock
  private ConnectionConfiguration  config;
  @Mock
  private Encoder<Object> encoder;
  @Mock
  private BiConsumer<Object, PublishException> errorHandler;
  @Mock
  private Channel channel;

  private Builder propertiesBuilder;
  private Object event;

  @BeforeEach
  void prepare() {
    propertiesBuilder = MessageProperties.BASIC.builder();
    event = new Object();
  }

  @Test
  void testPublisherConfiguration() throws EncodeException, IOException {
    byte[] expectedData = "somedata".getBytes();

    when(encoder.contentType()).thenReturn("application/sometype");
    when(encoder.encode(event)).thenReturn(expectedData);

    PublisherConfiguration publisherConfig = new PublisherConfiguration(config, "exchange",
        "routingKey", propertiesBuilder, encoder, errorHandler);

    publisherConfig.publish(channel, event);

    verify(channel).basicPublish("exchange", "routingKey",
        propertiesBuilder.contentType("application/sometype").build(), expectedData);
  }

  @Test
  void testAcceptError() {
    PublishException publishError = new PublishException("some error", null);
    PublisherConfiguration publisherConfig = new PublisherConfiguration(config, "exchange",
        "routingKey", propertiesBuilder, encoder, errorHandler);

    publisherConfig.accept(event, publishError);
    
    verify(errorHandler).accept(event, publishError);
  }
}
