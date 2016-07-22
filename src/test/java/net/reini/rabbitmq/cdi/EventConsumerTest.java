package net.reini.rabbitmq.cdi;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

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
    consumer = new EventConsumer(decoder, true, eventControl, eventPool);
  }

  @Test
  public void testGetSetChannel() throws Exception {
    Channel channel = Mockito.mock(Channel.class);
    assertNull(consumer.getChannel());
    consumer.setChannel(channel);
    assertEquals(channel, consumer.getChannel());
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
  public void testHandleConsumeOk() throws Exception {
    consumer.handleConsumeOk(null);
  }

  @Test
  public void testHandleCancelOk() throws Exception {
    consumer.handleCancelOk(null);
  }

  @Test
  public void testHandleCancel() throws Exception {
    consumer.handleCancel(null);
  }

  @Test
  public void testHandleShutdownSignal() throws Exception {
    ShutdownSignalException sig = new ShutdownSignalException(false, false, null, null);

    consumer.handleShutdownSignal(null, sig);
  }

  @Test
  public void testHandleRecoverOk() throws Exception {
    consumer.handleRecoverOk(null);
  }

  @Test
  public void testHandleDelivery() throws Exception {
    byte[] body = "the message".getBytes();
    Envelope envelope = new Envelope(123L, false, null, null);
    BasicProperties properties = new BasicProperties();

    consumer.handleDelivery("consumerTag", envelope, properties, body);
  }
}
