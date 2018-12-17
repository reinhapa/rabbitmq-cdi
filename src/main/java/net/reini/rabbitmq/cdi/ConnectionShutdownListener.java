package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConnectionShutdownListener implements ShutdownListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
  private UnrecoverableErrorDetector unrecoverableErrorDetector;
  private ConnectionManager connectionManager;

  public ConnectionShutdownListener(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
    this.unrecoverableErrorDetector = new UnrecoverableErrorDetector();
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException cause) {
    LOGGER.debug("connection shutdown detected", cause);
    // Only hard error means loss of connection
    if (unrecoverableErrorDetector.isUnrecoverableError(cause)) {
      synchronized (connectionManager) {
        // No action to be taken if factory is already closed
        // or already connecting
        if (connectionManager.getState() == ConnectionState.CLOSED || connectionManager.getState() == ConnectionState.CONNECTING) {
          return;
        }
        connectionManager.changeState(ConnectionState.CONNECTING);
      }
      LOGGER.error("Connection lost by unrecoverable error reconnecting");
    }
  }

}
