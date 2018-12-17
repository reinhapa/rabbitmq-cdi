package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * <p>
 * Manager manages one connection to one broker. The Manager will reconnect in case the connection is lost, and keeps constantly checking the connection status.
 * </p>
 *
 * @author Patrick Reinhart
 */
public class ConnectionManager {

  private final ConnectionConfiguration config;
  private final Set<ConnectionListener> listeners;
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

  private volatile Connection connection;
  private volatile ConnectionState state;
  private Thread connectThread = null;
  private ShutdownListener shutdownListener = null;
  private ResourceCloser resourceCloser;
  
  ConnectionManager(ConnectionConfiguration config) {
    this.resourceCloser = new ResourceCloser();
    this.config = config;
    state = ConnectionState.NEVER_CONNECTED;
    listeners = ConcurrentHashMap.newKeySet();
    this.shutdownListener = new ConnectionShutdownListener(this);
  }

  /**
   * Changes the factory state and notifies all connection listeners.
   *
   * @param newState The new connection factory state
   */
  void changeState(ConnectionState newState) {
    state = newState;
    notifyListenersOnStateChange();
  }

  /**
   * Notifies all connection listener about a state change.
   */
  void notifyListenersOnStateChange() {
    LOGGER.debug("Notifying connection listeners about state change to {}", state);

    for (ConnectionListener listener : listeners) {
      try {

        switch (state) {
          case CONNECTED:
            listener.onConnectionEstablished(connection);
            break;
          case CONNECTING:
            listener.onConnectionLost(connection);
            break;
          case CLOSED:
            listener.onConnectionClosed(connection);
            break;
          default:
            break;
        }
      } catch (ShutdownSignalException e) {
        LOGGER.warn("connection listener throw an exception while informing about state change", e);
      } catch (RuntimeException e) {
        LOGGER.warn("connection listener throw an exception while informing about state change", e);
      }

    }
  }

  Connection getConnection(Supplier<ConnectionFactory> factorySupplier)
      throws IOException {
    // Retrieve the connection if it is established
    if (state == ConnectionState.CLOSED) {
      throw new IOException("Attempt to retrieve a connection from a closed connection factory");
    }
    if (state == ConnectionState.CONNECTED) {
      return connection;
    }
    // Throw an exception if no established connection could not be
    // retrieved
    LOGGER.error("Unable to retrieve connection");
    throw new IOException("Unable to retrieve connection");
  }

  /**
   * Establishes a new connection with the given {@code addresses}.
   *
   * @throws IOException if establishing a new connection fails
   * @throws TimeoutException if establishing a new connection times out
   * @throws NoSuchAlgorithmException if the security context creation for secured connection fails
   */
  private Connection establishConnection(Supplier<ConnectionFactory> factorySupplier)
      throws IOException, TimeoutException, NoSuchAlgorithmException {
    LOGGER.debug("Trying to establish connection using {}", config);
    ConnectionFactory connectionFactory = factorySupplier.get();
    return config.createConnection(connectionFactory);
  }

  private synchronized void startConnectThread(Supplier<ConnectionFactory> factorySupplier) {
    if (this.connectThread == null) {
      this.connectThread = new Thread(new Runnable() {
        @Override
        public void run() {
          ensureConnectionState(factorySupplier);
        }
      });
      connectThread.setName("rabbitmq-cdi connect thread");
      connectThread.setDaemon(true);
      connectThread.start();
    }
  }

  private synchronized void stopConnectThread() {
    if (connectThread != null && connectThread.isAlive()) {
      connectThread.interrupt();
      try {
        connectThread.join();
      } catch (InterruptedException e) {
        LOGGER.warn("failed to wait for thread end of connect thread", e);
      }
      connectThread = null;
    }
  }

  private void ensureConnectionState(Supplier<ConnectionFactory> factorySupplier) {
    String connectWarning = "could not establish connection to host " + factorySupplier.get().getHost() + " on port " + factorySupplier.get().getPort() + ", retry to establish connection...";

    while (Thread.currentThread().interrupted() == false) {
      synchronized (this) {
        if (state == ConnectionState.NEVER_CONNECTED || state == ConnectionState.CONNECTING) {
          try {
            connection = establishConnection(factorySupplier);
            connection.addShutdownListener(this.shutdownListener);
            LOGGER.debug("Established connection successfully");
            changeState(ConnectionState.CONNECTED);

          } catch (IOException | TimeoutException e) {
            LOGGER.warn(connectWarning);
            LOGGER.debug("could not establish connection", e);
          } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("error during connect", e);
          }
        }
      }
      try {

        Thread.sleep(config.getConnectRetryWaitTime());
      } catch (InterruptedException e) {
        LOGGER.debug("connect thread was interrupted", e);
        Thread.currentThread().interrupt();
      }


    }
  }

  synchronized void close() {
    if (state == ConnectionState.CLOSED) {
      LOGGER.warn("Attempt to close connection factory which is already closed");
      return;
    }
    LOGGER.info("Closing connection factory");
    stopConnectThread();
    if (connection != null) {
      connection.removeShutdownListener(this.shutdownListener);
      resourceCloser.closeResource(connection, "Unable to close current connection");
      connection = null;
    }
    changeState(ConnectionState.CLOSED);
    LOGGER.info("Closed connection factory");
  }

  public void connect(Supplier<ConnectionFactory> factorySupplier) {
    startConnectThread(factorySupplier);
  }

  ConnectionState getState() {
    return state;
  }

  public void addListener(ConnectionListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(ConnectionListener listener) {
    this.listeners.remove(listener);
  }
}

