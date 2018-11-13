package net.reini.rabbitmq.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventConsumerTest {
  @Mock
  private Event<Object> eventControl;
  @Mock
  private Instance<Object> eventPool;
  @Mock
  private Decoder<TestEvent> decoder;

  private EventConsumer consumer;

  @Before
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
    TestEvent event = new TestEvent();
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
