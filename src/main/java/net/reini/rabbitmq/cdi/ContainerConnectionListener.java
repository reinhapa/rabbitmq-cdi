/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2020 Patrick Reinhart
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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Connection;

final class ContainerConnectionListener implements ConnectionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainer.class);

  private ConsumerContainer consumerContainer;
  private ReentrantLock lock;
  private Condition connectionAvailableCondition;

  public ContainerConnectionListener(ConsumerContainer consumerContainer, ReentrantLock lock,
      Condition connectionAvailableCondition) {
    this.consumerContainer = consumerContainer;
    this.lock = lock;
    this.connectionAvailableCondition = connectionAvailableCondition;
  }

  @Override
  public void onConnectionEstablished(Connection con) {
    try {
      lock.lock();
      this.consumerContainer.setConnectionAvailable(true);
      LOGGER.info("Connection established to {}. Activating consumers...", con);
      connectionAvailableCondition.signalAll();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void onConnectionLost(Connection con) {
    try {
      lock.lock();
      this.consumerContainer.setConnectionAvailable(false);
      LOGGER.warn("Connection lost. Deactivating consumers");
      this.consumerContainer.deactivateAllConsumer();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void onConnectionClosed(Connection con) {
    try {
      lock.lock();
      this.consumerContainer.setConnectionAvailable(false);
      LOGGER.warn("Connection closed for ever. Deactivating consumers");
      this.consumerContainer.deactivateAllConsumer();
    } finally {
      lock.unlock();
    }
  }
}
