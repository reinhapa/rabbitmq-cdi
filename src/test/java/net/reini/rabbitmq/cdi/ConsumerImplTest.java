package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Tests the {@link ConsumerImpl} implementation.
 *
 * @author Patrick Reinhart
 */
@ExtendWith(MockitoExtension.class)
public class ConsumerImplTest {
  @Mock
  private EnvelopeConsumer envelopeConsumer;
  @Mock
  private Channel channel;

  private Consumer consumer;
  private Consumer consumerAcknowledged;

  @BeforeEach
  public void setUp() {
    consumer = ConsumerImpl.create(envelopeConsumer);
    consumerAcknowledged = ConsumerImpl.createAcknowledged(envelopeConsumer, channel);
  }

  /**
   * Test method for {@link ConsumerImpl#handleConsumeOk(String)}.
   */
  @Test
  public void testHandleConsumeOk() {
    consumer.handleCancelOk("consumerTag");
    consumerAcknowledged.handleCancelOk("consumerTag");
  }

  /**
   * Test method for {@link ConsumerImpl#handleCancelOk(String)}.
   */
  @Test
  public void testHandleCancelOk() {
    consumer.handleCancelOk("consumerTag");
    consumerAcknowledged.handleCancelOk("consumerTag");
  }

  /**
   * Test method for {@link ConsumerImpl#handleCancel(String)}.
   * 
   * @throws IOException
   */
  @Test
  public void testHandleCancel() throws IOException {
    consumer.handleCancel("consumerTag");
    consumerAcknowledged.handleCancel("consumerTag");
  }

  /**
   * Test method for {@link ConsumerImpl#handleShutdownSignal(String, ShutdownSignalException)}.
   */
  @Test
  public void testHandleShutdownSignal() {
    ShutdownSignalException sig = new ShutdownSignalException(false, false, null, null);

    consumer.handleShutdownSignal("consumerTag", sig);
    consumerAcknowledged.handleShutdownSignal("consumerTag", sig);
  }

  /**
   * Test method for {@link ConsumerImpl#handleRecoverOk(String)}.
   */
  @Test
  public void testHandleRecoverOk() {
    consumer.handleRecoverOk("consumerTag");
    consumerAcknowledged.handleRecoverOk("consumerTag");
  }

  /**
   * Test method for {@link ConsumerImpl#handleDelivery(String, Envelope, BasicProperties, byte[])}.
   * 
   * @throws IOException
   */
  @Test
  public void testHandleDelivery() throws IOException {
    Envelope envelope = new Envelope(1234L, false, "exchange", "routingKey");
    BasicProperties basicProperties = new BasicProperties();
    byte[] bodyData = "the body data".getBytes();

    consumer.handleDelivery("consumerTag", envelope, basicProperties, bodyData);

    verify(envelopeConsumer).consume("consumerTag", envelope, basicProperties, bodyData);
  }

  /**
   * Test method for {@link ConsumerImpl#handleDelivery(String, Envelope, BasicProperties, byte[])}.
   * 
   * @throws IOException
   */
  @Test
  public void testHandleDelivery_acknowledged() throws IOException {
    Envelope envelope = new Envelope(1234L, false, "exchange", "routingKey");
    BasicProperties basicProperties = new BasicProperties();
    byte[] bodyData = "the body data".getBytes();

    when(envelopeConsumer.consume("consumerTag", envelope, basicProperties, bodyData))
        .thenReturn(true);

    consumerAcknowledged.handleDelivery("consumerTag", envelope, basicProperties, bodyData);

    verify(channel).basicAck(1234L, false);
  }

  /**
   * Test method for {@link ConsumerImpl#handleDelivery(String, Envelope, BasicProperties, byte[])}.
   * 
   * @throws IOException
   */
  @Test
  public void testHandleDelivery_not_acknowledged() throws IOException {
    Envelope envelope = new Envelope(1234L, false, "exchange", "routingKey");
    BasicProperties basicProperties = new BasicProperties();
    byte[] bodyData = "the body data".getBytes();

    when(envelopeConsumer.consume("consumerTag", envelope, basicProperties, bodyData))
        .thenReturn(false);

    consumerAcknowledged.handleDelivery("consumerTag", envelope, basicProperties, bodyData);

    verify(channel).basicNack(1234L, false, false);
  }

  /**
   * Test method for {@link ConsumerImpl#handleDelivery(String, Envelope, BasicProperties, byte[])}.
   * 
   * @throws IOException
   */
  @Test
  public void testHandleDelivery_not_acknowledged_with_requeue() throws IOException {
    Envelope envelope = new Envelope(1234L, false, "exchange", "routingKey");
    BasicProperties basicProperties = new BasicProperties();
    byte[] bodyData = "the body data".getBytes();

    when(envelopeConsumer.consume("consumerTag", envelope, basicProperties, bodyData))
        .thenThrow(new IOException());

    consumerAcknowledged.handleDelivery("consumerTag", envelope, basicProperties, bodyData);

    verify(channel).basicNack(1234L, false, true);
  }
}
