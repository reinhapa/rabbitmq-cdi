package net.reini.rabbitmq.cdi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

@RunWith(MockitoJUnitRunner.class)
public class GenericPublisherTest {
  @Mock
  private ConnectionProducer connectionProducer;
  @Mock
  private Connection connection;
  @Mock
  private Channel channel;

  private GenericPublisher publisher;
  private TestEvent event;

  @Before
  public void setUp() throws Exception {
    publisher = new GenericPublisher(connectionProducer);
    event = new TestEvent();
    event.id = "theId";
    event.booleanValue = true;
  }

  @Test
  public void test() throws Exception {
    Builder builder = new Builder();
    PublisherConfiguration publisherConfiguration =
        new PublisherConfiguration("exchange", "routingKey", builder, new JsonEncoder<>(), connectionProducer);
    ArgumentCaptor<BasicProperties> propsCaptor = ArgumentCaptor.forClass(BasicProperties.class);

    when(connectionProducer.newConnection()).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channel);

    publisher.publish(event, publisherConfiguration);

    verify(channel).basicPublish(eq("exchange"), eq("routingKey"), propsCaptor.capture(),
        eq("{\"id\":\"theId\",\"booleanValue\":true}".getBytes()));
    assertEquals("application/json", propsCaptor.getValue().getContentType());
  }

  @Test
  public void testCustomMessageConverter() throws Exception {
    Builder builder = new Builder();
    PublisherConfiguration publisherConfiguration =
        new PublisherConfiguration("exchange", "routingKey", builder, new CustomEncoder(), connectionProducer);
    ArgumentCaptor<BasicProperties> propsCaptor = ArgumentCaptor.forClass(BasicProperties.class);

    when(connectionProducer.newConnection()).thenReturn(connection);
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
    public byte[] encode(TestEvent event) throws EncodeException {
      final String str =
          MessageFormat.format("Id: {0}, BooleanValue: {1}", event.getId(), event.isBooleanValue());
      return str.getBytes();
    }
  }
}
