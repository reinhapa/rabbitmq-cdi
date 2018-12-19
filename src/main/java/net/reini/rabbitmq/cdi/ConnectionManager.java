package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Manager manages one connection to one broker. The Manager will reconnect in case the connection is lost, and keeps constantly checking the connection status.
 * </p>
 *
 * @author Patrick Reinhart
 */
public class ConnectionManager {

  private final ConnectionConfiguration config;
  private final Set<ConnectionListener> listeners = ConcurrentHashMap.newKeySet();
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
  private final ReentrantLock connectionManagerLock;
  private final Condition noConnectionCondition;

  private volatile Connection connection;
  private volatile ConnectionState state = ConnectionState.NEVER_CONNECTED;
  private ResourceCloser resourceCloser = new ResourceCloser();
  private final ConnectionManagerWatcherThread connectThread;
  private ConnectionShutdownListener shutdownListener;
  private final ConnectionFactory connectionFactory;

  ConnectionManager(ConnectionConfiguration config) {
    this.config = config;
    this.connectionFactory = new ConnectionFactory();
    this.connectionManagerLock = new ReentrantLock();
    this.noConnectionCondition = connectionManagerLock.newCondition();
    this.shutdownListener = new ConnectionShutdownListener(this, this.connectionManagerLock);
    this.connectThread = new ConnectionManagerWatcherThread(connectionManagerLock, noConnectionCondition, this, config.getConnectRetryWaitTime());

  }

  ConnectionManager(ConnectionConfiguration config, ConnectionManagerWatcherThread connectThread, ConnectionShutdownListener shutdownListener, ConnectionFactory connectionFactory,
      ReentrantLock connectionManagerLock, Condition noConnectionCondition) {
    this.connectThread = connectThread;
    this.shutdownListener = shutdownListener;
    this.connectionFactory = connectionFactory;
    this.config = config;
    this.connectionManagerLock = connectionManagerLock;
    this.noConnectionCondition = noConnectionCondition;
  }

  public void connect() {
    if (state == ConnectionState.CLOSED) {
      throw new IllegalStateException("Attempt to initiate a connect from a closed connection manager");
    }
    startConnectThread();
  }

  public void addListener(ConnectionListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(ConnectionListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Changes the factory state and notifies all connection listeners.
   *
   * @param newState The new connection factory state
   */
  void changeState(ConnectionState newState) {
    state = newState;
    if (state == ConnectionState.CONNECTING) {
      try {
        connectionManagerLock.lock();
        noConnectionCondition.signalAll();

      } finally {
        connectionManagerLock.unlock();
      }
    }
    notifyListenersOnStateChange();
  }

  ConnectionState getState() {
    return state;
  }


  Connection getConnection()
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
   */
  private Connection createNewConnection()
      throws IOException, TimeoutException {
    LOGGER.debug("Trying to establish connection using {}", config);
    connection = config.createConnection(connectionFactory);
    connection.addShutdownListener(this.shutdownListener);
    LOGGER.debug("Established connection successfully");
    changeState(ConnectionState.CONNECTED);
    return connection;
  }

  boolean tryToEstablishConnection() {
    String connectWarning = "could not establish connection to host " + connectionFactory.getHost() + " on port " + connectionFactory.getPort() + ", retry to establish connection...";
    if (state == ConnectionState.CONNECTED || state == ConnectionState.CLOSED) {
      throw new IllegalStateException("connection manager illegal state to establish a connection: " + state);
    }

    try {
      connectionManagerLock.lock();
      connection = createNewConnection();
      return true;
    } catch (IOException | TimeoutException e) {
      LOGGER.warn(connectWarning);
      LOGGER.debug("could not establish connection", e);
    } finally {
      connectionManagerLock.unlock();
    }

    return false;
  }

  void close() {
    try {
      connectionManagerLock.lock();
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
    } finally {
      connectionManagerLock.unlock();
    }
  }


  /**
   * Notifies all connection listener about a state change.
   */
  private void notifyListenersOnStateChange() {
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
      } catch (RuntimeException e) {
        LOGGER.warn("connection listener throw an exception while informing about state change", e);
      }

    }
  }

  private synchronized void startConnectThread() {
    connectThread.start();
  }

  private synchronized void stopConnectThread() {
    if (connectThread.isRunning()) {
      connectThread.stopThread();
    }
  }

}

