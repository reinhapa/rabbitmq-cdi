package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

@ExtendWith(MockitoExtension.class)
public class EventConsumerTest {
  @Mock
  private Event<Object> eventControl;
  @Mock
  private Instance<Object> eventPool;
  @Mock
  private Decoder<TestEvent> decoder;

  private EventConsumer consumer;

  @BeforeEach
  public void setUp() throws Exception {
    consumer = new EventConsumer(decoder, eventControl, eventPool);
  }

  @Test
  public void testBuildEvent() throws DecodeException {
    TestEvent event = new TestEvent();
    byte[] body = "the message".getBytes();

    when(decoder.decode(body)).thenReturn(event);

    TestEvent eventObject = (TestEvent) consumer.buildEvent(body);
    assertEquals(event, eventObject);
  }

  @Test
  public void testBuildEvent_withDecodeFailure() throws DecodeException {
    TestEvent event = new TestEvent();
    byte[] body = "the message".getBytes();

    when(decoder.decode(body)).thenThrow(new DecodeException(null));
    when(eventPool.get()).thenReturn(event);

    TestEvent eventObject = (TestEvent) consumer.buildEvent(body);
    assertEquals(event, eventObject);
  }

  @Test
  public void testHandleDelivery_not_decodeable() throws Exception {
    byte[] body = "the message".getBytes();
    Envelope envelope = new Envelope(123L, false, null, null);
    BasicProperties properties = new BasicProperties();

    assertFalse(consumer.consume("consumerTag", envelope, properties, body));
  }

  @Test
  public void testHandleDelivery() throws Exception {
    TestEvent event = new TestEvent();
    byte[] body = "the message".getBytes();
    Envelope envelope = new Envelope(123L, false, null, null);
    BasicProperties properties = new BasicProperties();

    when(decoder.willDecode(null)).thenReturn(true);
    when(decoder.decode(body)).thenReturn(event);

    assertTrue(consumer.consume("consumerTag", envelope, properties, body));
  }
}
