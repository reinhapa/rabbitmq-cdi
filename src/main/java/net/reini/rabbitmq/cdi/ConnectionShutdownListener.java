/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2025 Patrick Reinhart
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

import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

class ConnectionShutdownListener implements ShutdownListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

  private final ReentrantLock connectionManagerLock;

  private UnrecoverableErrorDetector unrecoverableErrorDetector;
  private ConnectionManager connectionManager;

  public ConnectionShutdownListener(ConnectionManager connectionManager,
      ReentrantLock connectionManagerLock) {
    this.connectionManager = connectionManager;
    this.connectionManagerLock = connectionManagerLock;
    this.unrecoverableErrorDetector = new UnrecoverableErrorDetector();
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException cause) {
    LOGGER.debug("connection shutdown detected", cause);
    // Only hard error means loss of connection
    if (unrecoverableErrorDetector.isUnrecoverableError(cause)) {
      try {
        connectionManagerLock.lock();
        // No action to be taken if factory is already closed
        // or already connecting
        final ConnectionState state = connectionManager.getState();
        if (state == ConnectionState.CLOSED || state == ConnectionState.CONNECTING) {
          return;
        }
        connectionManager.changeState(ConnectionState.CONNECTING);
        LOGGER.error("Connection lost by unrecoverable error reconnecting");
      } finally {
        connectionManagerLock.unlock();
      }
    }
  }
}
