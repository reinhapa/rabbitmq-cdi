package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ContainerConnectionListener implements ConnectionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainer.class);
  private final NotifyWrapper notifyWrapper;

  private ConsumerContainer consumerContainer;

  public ContainerConnectionListener(ConsumerContainer consumerContainer) {
    this(consumerContainer, new NotifyWrapper(consumerContainer));
  }

  ContainerConnectionListener(ConsumerContainer consumerContainer, NotifyWrapper notifyWrapper) {
    this.notifyWrapper = notifyWrapper;
    this.consumerContainer = consumerContainer;
  }


  @Override
  public void onConnectionEstablished(Connection con) {
    synchronized (this.consumerContainer) {
      this.consumerContainer.setConnectionAvailable(true);
      LOGGER.info("Connection established to {}. Activating consumers...", con);
      this.notifyWrapper.notifyThread();
    }
  }

  @Override
  public void onConnectionLost(Connection con) {
    synchronized (this.consumerContainer) {
      this.consumerContainer.setConnectionAvailable(false);
      LOGGER.warn("Connection lost. Deactivating consumers");
      this.consumerContainer.deactivateAllConsumer();
    }
  }

  @Override
  public void onConnectionClosed(Connection con) {
    synchronized (this.consumerContainer) {
      this.consumerContainer.setConnectionAvailable(false);
      LOGGER.warn("Connection closed for ever. Deactivating consumers");
      this.consumerContainer.deactivateAllConsumer();
    }
  }

  static class NotifyWrapper {

    private ConsumerContainer consumerContainer;

    public NotifyWrapper(ConsumerContainer consumerContainer) {
      this.consumerContainer = consumerContainer;
    }

    public void notifyThread() {
      this.consumerContainer.notify();
    }
  }

}
