package net.reini.rabbitmq.cdi;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManagerWatcherThread extends Thread {

  private final ReentrantLock connectionManagerLock;
  private final Condition noConnectionCondition;
  private ConnectionManager connectionManager;
  private long connectRetryWaitTime;
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
  private final ThreadStopper threadStopper;

  public ConnectionManagerWatcherThread(ReentrantLock connectionManagerLock, Condition noConnectionCondition, ConnectionManager connectionManager, long connectRetryWaitTime) {
    this.threadStopper=new ThreadStopper();
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

  private void ensureConnectionState() {

    while (Thread.currentThread().interrupted() == false) {
      boolean connectionEstablished = false;
      try {
        connectionManagerLock.lock();
        if (connectionManager.getState() == ConnectionState.NEVER_CONNECTED || connectionManager.getState() == ConnectionState.CONNECTING) {
          connectionEstablished = connectionManager.tryToEstablishConnection();
          if (connectionEstablished) {
            try {
              noConnectionCondition.await();
            } catch (InterruptedException e) {
              LOGGER.debug("connect thread was interrupted while waiting", e);
              Thread.currentThread().interrupt();
            }
          }

        }
      } finally {
        connectionManagerLock.unlock();
      }
      if (connectionEstablished == false && Thread.currentThread().isInterrupted() == false) {
        try {
          Thread.sleep(connectRetryWaitTime);
        } catch (InterruptedException e) {
          LOGGER.debug("connect thread was interrupted while sleeping", e);
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  public void stopThread() {
    threadStopper.stopThread(this);
  }

  public boolean isRunning() {
    return isAlive();
  }
}
