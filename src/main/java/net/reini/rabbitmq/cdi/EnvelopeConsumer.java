package net.reini.rabbitmq.cdi;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

/**
 * Consumer of an Rabbit MQ envelope content.
 *
 * @author Patrick Reinhart
 */
@FunctionalInterface
public interface EnvelopeConsumer {
  boolean consume(String consumerTag, Envelope envelope, BasicProperties properties,
      byte[] body) throws IOException;
}
