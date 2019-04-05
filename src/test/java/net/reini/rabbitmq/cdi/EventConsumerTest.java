package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;

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
  private Event<Object> eventSink;
  @Mock
  private Event<TestEvent> testEventSink;
  @Mock
  private Decoder<TestEvent> decoder;

  private EventConsumer<TestEvent> consumer;

  @BeforeEach
  public void setUp() throws Exception {
    consumer = new EventConsumer<>(TestEvent.class, decoder, eventSink);
  }

  @Test
  public void testBuildEvent() throws DecodeException {
    TestEvent event = new TestEvent();
    byte[] body = "the message".getBytes();

    when(decoder.decode(body)).thenReturn(event);

    TestEvent eventObject = consumer.buildEvent(body);
    assertEquals(event, eventObject);
  }

  @Test
  public void testBuildEventDecodingFails() throws DecodeException {
    byte[] body = "the message".getBytes();

    when(decoder.decode(body)).thenThrow(new RuntimeException("some error"));

    TestEvent eventObject = consumer.buildEvent(body);
    assertNull(eventObject);
  }

  @Test
  public void testFireEventNullEvent() {
    assertFalse(consumer.fireEvent(null));
  }

  @Test
  public void testHandleDelivery_not_decodeable() throws Exception {
    byte[] body = "the message".getBytes();
    Envelope envelope = new Envelope(123L, false, null, null);
    BasicProperties properties = new BasicProperties();

    assertFalse(consumer.consume("consumerTag", envelope, properties, body));
  }

  @SuppressWarnings("boxing")
  @Test
  public void testHandleDelivery() throws Exception {
    TestEvent event = new TestEvent();
    byte[] body = "the message".getBytes();
    Envelope envelope = new Envelope(123L, false, null, null);
    BasicProperties properties = new BasicProperties();

    when(decoder.willDecode(null)).thenReturn(true);
    when(decoder.decode(body)).thenReturn(event);
    when(eventSink.select(TestEvent.class)).thenReturn(testEventSink);

    assertTrue(consumer.consume("consumerTag", envelope, properties, body));

    verify(testEventSink).fire(event);
  }

  @SuppressWarnings("boxing")
  @Test
  public void testHandleDelivery_withError() throws Exception {
    TestEvent event = new TestEvent();
    byte[] body = "the message".getBytes();
    Envelope envelope = new Envelope(123L, false, null, null);
    BasicProperties properties = new BasicProperties();

    when(decoder.willDecode(null)).thenReturn(true);
    when(decoder.decode(body)).thenReturn(event);
    when(eventSink.select(TestEvent.class)).thenReturn(testEventSink);
    doThrow(new ObserverException()).when(testEventSink).fire(event);

    assertFalse(consumer.consume("consumerTag", envelope, properties, body));
  }
}
