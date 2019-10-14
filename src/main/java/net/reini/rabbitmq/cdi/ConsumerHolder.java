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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoverableChannel;
import com.rabbitmq.client.RecoveryListener;
import com.rabbitmq.client.ShutdownSignalException;

class ConsumerHolder implements RecoveryListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerHolder.class);

  private final boolean autoAck;
  private final String queueName;
  private final Object activeLock;
  private final EventConsumer<?> consumer;
  private final ConsumerChannelFactory consumerChannelFactory;
  private final ResourceCloser resourceCloser;
  private final DeclarerRepository declarerRepository;
  private final List<Declaration> declarations;
  private final int prefetchCount;

  private RecoverableChannel channel;

  private volatile boolean active;
  private volatile boolean recoverRunning;

  ConsumerHolder(EventConsumer<?> consumer, String queueName, boolean autoAck, int prefetchCount,
      ConsumerChannelFactory consumerChannelFactory, List<Declaration> declarations,
      DeclarerRepository declarerRepository) {
    this.consumer = consumer;
    this.queueName = queueName;
    this.autoAck = autoAck;
    this.prefetchCount = prefetchCount;
    this.consumerChannelFactory = consumerChannelFactory;
    this.declarations = declarations;
    this.declarerRepository = declarerRepository;
    this.activeLock = new Object();
    this.resourceCloser = new ResourceCloser();
  }

  void deactivate() {
    synchronized (activeLock) {
      if (active) {
        LOGGER.debug("Deactivating consumer of class {}", consumer.getClass());
        LOGGER.debug("Closing channel for consumer of class {}", consumer.getClass());
        ensureCompleteShutdown();
        active = false;
      }
      LOGGER.info("Deactivated consumer of class {}", consumer.getClass());
    }
  }

  void activate() throws IOException {
    synchronized (activeLock) {
      if (!active) {
        LOGGER.debug("Activating consumer of class {}", consumer.getClass());
        // Start the consumer
        try {
          channel = this.consumerChannelFactory.createChannel();
          channel.addRecoveryListener(this);
          channel.basicQos(this.prefetchCount);
          channel.basicConsume(queueName, autoAck,
              autoAck ? this::deliverNoAck : this::deliverWithAck, this::handleShutdownSignal);
          declarerRepository.declare(channel, declarations);
          LOGGER.info("Activated consumer of class {}", consumer.getClass());
          active = true;
        } catch (Exception e) {
          LOGGER.error("Failed to activate consumer of class {}", consumer.getClass(), e);
          ensureCompleteShutdown();
          throw e;
        }
      }
    }
  }

  Channel ensureOpenChannel() {
    synchronized (activeLock) {
      while (recoverRunning) {
        LOGGER.debug("Waiting for recovery...");
        try {
          activeLock.wait(1000);
        } catch (InterruptedException e) {
          LOGGER.error("Interrupted while waiting", e);
          Thread.currentThread().interrupt();
        }
      }
    }
    return channel;
  }

  void deliverNoAck(String consumerTag, Delivery message) throws IOException {
    Envelope envelope = message.getEnvelope();
    LOGGER.debug("Consuming message {} for consumer tag {}", envelope, consumerTag);
    consumer.consume(consumerTag, envelope, message.getProperties(),
        message.getBody());
  }

  void deliverWithAck(String consumerTag, Delivery message) throws IOException {
    Envelope envelope = message.getEnvelope();
    long deliveryTag = envelope.getDeliveryTag();
    try {
      LOGGER.debug("Consuming message {} for consumer tag {}", envelope, consumerTag);
      if (consumer.consume(consumerTag, envelope, message.getProperties(), message.getBody())) {
        ensureOpenChannel().basicAck(deliveryTag, false);
        LOGGER.debug("Acknowledged {}", message);
      } else {
        ensureOpenChannel().basicNack(deliveryTag, false, false);
        LOGGER.debug("Not acknowledged {}", envelope);
      }
    } catch (IOException e) {
      LOGGER.warn("Consume failed for {}", message, e);
      channel.basicNack(deliveryTag, false, true);
      LOGGER.debug("Not acknowledged {} (re-queue)", envelope);
    }
  }

  void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
    LOGGER.info("Received shutdown signal {} for consumer tag {}", sig, consumerTag);
  }

  void ensureCompleteShutdown() {
    synchronized (activeLock) {
      if (channel != null) {
        resourceCloser.closeResource(channel, "Closing channel failed");
        channel = null;
      }
    }
  }

  boolean isAutoAck() {
    return autoAck;
  }

  String getQueueName() {
    return queueName;
  }

  @Override
  public void handleRecovery(Recoverable recoverable) {
    LOGGER.debug("Handle recovery");
    if (channel == recoverable) {
      synchronized (activeLock) {
        recoverRunning = false;
        activeLock.notify();
      }
    }
  }

  @Override
  public void handleRecoveryStarted(Recoverable recoverable) {
    LOGGER.debug("Handle recovery started");
    if (channel == recoverable) {
      recoverRunning = true;
    }
  }
}
