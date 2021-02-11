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

class ConnectionManagerWatcherThread extends StoppableThread {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

  private final ReentrantLock connectionManagerLock;
  private final Condition noConnectionCondition;
  private final ThreadStopper threadStopper;

  private long connectRetryWaitTime;
  private ConnectionManager connectionManager;

  ConnectionManagerWatcherThread(ReentrantLock connectionManagerLock,
      Condition noConnectionCondition, ConnectionManager connectionManager,
      long connectRetryWaitTime) {
    this.threadStopper = new ThreadStopper();
    this.connectionManagerLock = connectionManagerLock;
    this.noConnectionCondition = noConnectionCondition;
    this.connectionManager = connectionManager;
    this.connectRetryWaitTime = connectRetryWaitTime;
    this.setDaemon(true);
    this.setName("rabbitmq-cdi connect thread");
  }

  @Override
  public void run() {
    ensureConnectionState();
  }

  void stopThread() {
    threadStopper.stopThread(this);
  }

  boolean isRunning() {
    return isAlive();
  }

  private void ensureConnectionState() {

    while (!Thread.currentThread().isInterrupted() && !stopped) {
      boolean connectionEstablished = false;
      try {
        connectionManagerLock.lock();
        if (reconnectNeeded()) {
          connectionEstablished = connectionManager.tryToEstablishConnection();
          if (connectionEstablished) {
            waitTillConnectionIsLost();
          }
        }
      } catch (InterruptedException e) {
        LOGGER.debug("connect thread was interrupted while waiting", e);
        Thread.currentThread().interrupt();
        return;
      } finally {
        connectionManagerLock.unlock();
      }
      if (!connectionEstablished && (!Thread.currentThread().isInterrupted() && !stopped)) {
        waitForRetry();
      }
    }
  }

  private boolean reconnectNeeded() {
    return connectionManager.getState() == ConnectionState.NEVER_CONNECTED
        || connectionManager.getState() == ConnectionState.CONNECTING;
  }

  private void waitForRetry() {
    try {
      Thread.sleep(connectRetryWaitTime);
    } catch (InterruptedException e) {
      LOGGER.debug("connect thread was interrupted while sleeping", e);
      Thread.currentThread().interrupt();
    }
  }

  private void waitTillConnectionIsLost() throws InterruptedException {
    noConnectionCondition.await();
  }
}
