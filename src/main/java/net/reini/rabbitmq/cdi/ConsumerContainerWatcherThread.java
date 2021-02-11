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

final class ConsumerContainerWatcherThread extends StoppableThread {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConsumerContainerWatcherThread.class);
  private final ConsumerContainer consumerContainer;
  private final ReentrantLock lock;
  private final Condition noConnectionCondition;
  private final ThreadStopper threadStopper;
  private long attempt;
  private long retryTime;

  ConsumerContainerWatcherThread(ConsumerContainer consumerContainer, long retryTime,
      ReentrantLock lock, Condition noConnectionCondition) {
    this.threadStopper = new ThreadStopper();
    this.consumerContainer = consumerContainer;
    this.retryTime = retryTime;
    this.lock = lock;
    this.noConnectionCondition = noConnectionCondition;
    this.setDaemon(true);
    this.setName("consumer watcher thread");
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted() && !stopped) {
      boolean allConsumersActive = false;
      try {
        lock.lock();
        if (consumerContainer.isConnectionAvailable()) {
          attempt++;
          allConsumersActive = consumerContainer.ensureConsumersAreActive();
        }
        if (allConsumersActive || !consumerContainer.isConnectionAvailable()) {
          attempt = 0;
          this.noConnectionCondition.await();
        }
      } catch (InterruptedException e) {
        LOGGER.info("interrupted while waiting for notification");
        Thread.currentThread().interrupt();
        return;
      } finally {
        lock.unlock();
      }
      if (!allConsumersActive && attempt > 0) {
        waitForRetry();
      }

    }
  }

  public void stopThread() {
    threadStopper.stopThread(this);
  }

  private void waitForRetry() {
    LOGGER.warn("could not activate all consumer. Retry to activate failed consumers");
    try {
      Thread.sleep(retryTime);
    } catch (InterruptedException e) {
      LOGGER.info("interrupted while sleeping", e);
      Thread.currentThread().interrupt();
    }
  }
}
