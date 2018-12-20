package net.reini.rabbitmq.cdi;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConnectionManagerWatcherThread extends Thread {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
  private final ReentrantLock connectionManagerLock;
  private final Condition noConnectionCondition;
  private final ThreadStopper threadStopper;
  private ConnectionManager connectionManager;
  private long connectRetryWaitTime;

  ConnectionManagerWatcherThread(ReentrantLock connectionManagerLock, Condition noConnectionCondition, ConnectionManager connectionManager, long connectRetryWaitTime) {
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

    while (!interrupted()) {
      boolean connectionEstablished = false;
      try {
        connectionManagerLock.lock();
        if (reconnectNeeded()) {
          connectionEstablished = connectionManager.tryToEstablishConnection();
          if (connectionEstablished) {
            waitTillConnectionIsLost();
          }
        }
      } finally {
        connectionManagerLock.unlock();
      }
      if (!connectionEstablished && !Thread.currentThread().isInterrupted()) {
        waitForRetry();
      }
    }
  }

  private boolean reconnectNeeded() {
    return connectionManager.getState() == ConnectionState.NEVER_CONNECTED || connectionManager.getState() == ConnectionState.CONNECTING;
  }

  private void waitForRetry() {
    try {
      Thread.sleep(connectRetryWaitTime);
    } catch (InterruptedException e) {
      LOGGER.debug("connect thread was interrupted while sleeping", e);
      Thread.currentThread().interrupt();
    }
  }

  private void waitTillConnectionIsLost() {
    try {
      noConnectionCondition.await();
    } catch (InterruptedException e) {
      LOGGER.debug("connect thread was interrupted while waiting", e);
      Thread.currentThread().interrupt();
    }
  }
}