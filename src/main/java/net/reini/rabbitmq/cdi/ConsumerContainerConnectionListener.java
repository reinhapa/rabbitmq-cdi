package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Connection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ContainerConnectionListener implements ConnectionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainer.class);

  private ConsumerContainer consumerContainer;
  private ReentrantLock lock;
  private Condition connectionAvailableCondition;

  public ContainerConnectionListener(ConsumerContainer consumerContainer, ReentrantLock lock, Condition connectionAvailableCondition) {
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

