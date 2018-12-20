package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import com.rabbitmq.client.Connection;

/**
 * <p>
 * Repository to hold one single connection to each broker
 * </p>
 *
 * <p>
 * It is recommended by the RabbitMQ documentation (v2.7) to use one single connection within a client and to use one channel for every client thread.
 * </p>
 *
 * @author Patrick Reinhart
 */
@ApplicationScoped
public class ConnectionRepository {
  private final Map<ConnectionConfiguration, ConnectionManager> connectionManagers;
  private Function<ConnectionConfiguration, ConnectionManager> connectionManagerFactoryFunction;

  public ConnectionRepository() {
    this(ConnectionManager::new);
  }

  ConnectionRepository(Function<ConnectionConfiguration, ConnectionManager> connectionManagerFactoryFunction) {
    connectionManagers = new ConcurrentHashMap<>();
    this.connectionManagerFactoryFunction = connectionManagerFactoryFunction;
  }

  /**
   * <p>
   * Gets a connection for the broker config.
   * </p>
   *
   * <p>
   * In case a connection is lost, the factory will try to reestablish a new connection.
   * </p>
   *
   * @param config the connection configuration
   * @return The connection
   * @throws IOException if the connection is not yet available
   */
  public Connection getConnection(ConnectionConfiguration config)
      throws IOException {
    return connectionManagers.computeIfAbsent(config, connectionManagerFactoryFunction)
        .getConnection();
  }

  /**
   * <p>
   * Triggers the repository to create a ConnectionManager for the broker configuration if not present and to start connection attempts to the broker
   * </p>
   *
   * @param config the broker connection configuration
   */
  public void connect(ConnectionConfiguration config) {
    connectionManagers.computeIfAbsent(config, connectionManagerFactoryFunction)
        .connect();
  }


  /**
   * <p>
   * Closes the connection factory and interrupts all threads associated to it.
   * </p>
   *
   * <p>
   * Note: Make sure to close the connection factory when not used any more as otherwise the connection may remain established and ghost threads may reside.
   * </p>
   */
  @PreDestroy
  public void close() {
    connectionManagers.values().forEach(ConnectionManager::close);
  }

  /**
   * Registers a connection listener at the factory which is notified about changes of connection states.
   *
   * @param config the connection configuration
   * @param listener The connection listener
   */
  public void registerConnectionListener(ConnectionConfiguration config, ConnectionListener listener) {
    connectionManagers.computeIfAbsent(config, connectionManagerFactoryFunction).addListener(listener);
  }

  /**
   * Removes a connection listener from the factory.
   *
   * @param config the connection configuration
   * @param listener The connection listener
   */
  public void removeConnectionListener(ConnectionConfiguration config, ConnectionListener listener) {
    connectionManagers.computeIfAbsent(config, connectionManagerFactoryFunction).removeListener(listener);
  }
}
