package net.reini.rabbitmq.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

class ConsumerHolderChannelShutdownListener implements ShutdownListener {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConsumerHolderChannelShutdownListener.class);
  private final UnrecoverableErrorDetector unrecoverableErrorDetector;
  private ConsumerHolder consumerHolder;

  ConsumerHolderChannelShutdownListener(ConsumerHolder consumerHolder) {
    this.consumerHolder = consumerHolder;
    this.unrecoverableErrorDetector = new UnrecoverableErrorDetector();
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException cause) {
    synchronized (consumerHolder) {
      LOGGER.warn("channel shutdown detected", cause);
      if (unrecoverableErrorDetector.isUnrecoverableError(cause)) {
        consumerHolder.ensureCompleteShutdown();
      }
    }
  }
}
