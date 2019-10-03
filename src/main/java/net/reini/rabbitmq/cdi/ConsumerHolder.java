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
import com.rabbitmq.client.ShutdownListener;

class ConsumerHolder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerHolder.class);

  private final boolean autoAck;
  private final String queueName;
  private final Object activeLock;
  private final EventConsumer<?> consumer;
  private final ShutdownListener shutdownListener;
  private final ConsumerChannelFactory consumerChannelFactory;
  private final ResourceCloser resourceCloser;
  private final ConsumerFactory consumerFactory;
  private final DeclarerRepository declarerRepository;
  private final List<Declaration> declarations;
  private final int prefetchCount;
  private Channel channel;

  private volatile boolean active;

  ConsumerHolder(EventConsumer<?> consumer, String queueName, boolean autoAck, int prefetchCount,
      ConsumerChannelFactory consumerChannelFactory, ConsumerFactory consumerFactory,
      List<Declaration> declarations,
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
    this.shutdownListener = new ConsumerHolderChannelShutdownListener(this);
    this.consumerFactory = consumerFactory;
  }

  void deactivate() {
    synchronized (activeLock) {
      if (active) {
        LOGGER.debug("Deactivating consumer of class {}", consumer.getClass());
        LOGGER.debug("Closing channel for consumer of class {}", consumer.getClass());
        ensureCompleteShutdown();
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
          channel.addShutdownListener(shutdownListener);
          declarerRepository.declare(channel, declarations);
          channel.basicQos(this.prefetchCount);
          channel.basicConsume(queueName, autoAck, autoAck ? consumerFactory.create(consumer)
              : consumerFactory.createAcknowledged(consumer, channel));
          LOGGER.info("Activated consumer of class {}", consumer.getClass());
          active = true;
        } catch (IOException e) {
          LOGGER.error("Failed to activate consumer of class {}", consumer.getClass(), e);
          ensureCompleteShutdown();
          throw e;
        }
      }
    }
  }

  void ensureCompleteShutdown() {
    if (channel != null) {
      channel.removeShutdownListener(this.shutdownListener);
    }
    resourceCloser.closeResource(channel, "closing channel for consumer " + consumer.getClass());
    channel = null;
    active = false;
  }

  boolean isAutoAck() {
    return autoAck;
  }

  String getQueueName() {
    return queueName;
  }
}
