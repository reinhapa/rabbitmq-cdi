/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015, 2019 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.reini.rabbitmq.cdi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Consumer implementation with and without acknowledged behavior.
 *
 * @author Patrick Reinhart
 */
final class ConsumerImpl implements Consumer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerImpl.class);

  private final EnvelopeConsumer envelopeConsumer;

  static Consumer createAcknowledged(EnvelopeConsumer consumer, Channel channel) {
    return create((consumerTag, envelope, properties, body) -> acknowledgedConsume(consumer,
        channel, consumerTag, envelope, properties, body));
  }

  static Consumer create(EnvelopeConsumer consumer) {
    return new ConsumerImpl(consumer);
  }

  static boolean acknowledgedConsume(EnvelopeConsumer consumer, Channel channel, String consumerTag,
      Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
    long deliveryTag = envelope.getDeliveryTag();
    try {
      if (consumer.consume(consumerTag, envelope, properties, body)) {
        channel.basicAck(deliveryTag, false);
        LOGGER.debug("Acknowledged {}", envelope);
        return true;
      }
      channel.basicNack(deliveryTag, false, false);
      LOGGER.debug("Not acknowledged {}", envelope);
    } catch (IOException e) {
      LOGGER.warn("Consume failed for {}", envelope, e);
      channel.basicNack(deliveryTag, false, true);
      LOGGER.debug("Not acknowledged {} (re-queue)", envelope);
    }
    return false;
  }

  private ConsumerImpl(EnvelopeConsumer envelopeConsumer) {
    this.envelopeConsumer = envelopeConsumer;
  }

  @Override
  public void handleConsumeOk(String consumerTag) {
  }

  @Override
  public void handleCancelOk(String consumerTag) {
  }

  @Override
  public void handleCancel(String consumerTag) throws IOException {
  }

  @Override
  public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
  }

  @Override
  public void handleRecoverOk(String consumerTag) {
  }


  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
      byte[] body) throws IOException {
    envelopeConsumer.consume(consumerTag, envelope, properties, body);
  }
}
