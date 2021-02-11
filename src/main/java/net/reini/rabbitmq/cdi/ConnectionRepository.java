/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2020 Patrick Reinhart
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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import com.rabbitmq.client.Connection;

/**
 * Repository to hold one single connection to each broker
 *
 * <p>
 * It is recommended by the RabbitMQ documentation (v2.7) to use one single connection within a
 * client and to use one channel for every client thread.
 *
 * @author Patrick Reinhart
 */
@ApplicationScoped
public class ConnectionRepository {
  private final Map<ConnectionConfig, ConnectionManager> connectionManagers;
  private Function<ConnectionConfig, ConnectionManager> connectionManagerFactoryFunction;

  public ConnectionRepository() {
    this(ConnectionManager::new);
  }

  ConnectionRepository(
      Function<ConnectionConfig, ConnectionManager> connectionManagerFactoryFunction) {
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
  public Connection getConnection(ConnectionConfig config) throws IOException {
    return connectionManagers.computeIfAbsent(config, connectionManagerFactoryFunction)
        .getConnection();
  }

  /**
   * <p>
   * Triggers the repository to create a ConnectionManager for the broker configuration if not
   * present and to start connection attempts to the broker
   * </p>
   *
   * @param config the broker connection configuration
   */
  public void connect(ConnectionConfig config) {
    connectionManagers.computeIfAbsent(config, connectionManagerFactoryFunction).connect();
  }


  /**
   * <p>
   * Closes the connection factory and interrupts all threads associated to it.
   * </p>
   *
   * <p>
   * Note: Make sure to close the connection factory when not used any more as otherwise the
   * connection may remain established and ghost threads may reside.
   * </p>
   */
  @PreDestroy
  public void close() {
    connectionManagers.values().forEach(ConnectionManager::close);
  }

  /**
   * Registers a connection listener at the factory which is notified about changes of connection
   * states.
   *
   * @param config the connection configuration
   * @param listener The connection listener
   */
  public void registerConnectionListener(ConnectionConfig config, ConnectionListener listener) {
    connectionManagers.computeIfAbsent(config, connectionManagerFactoryFunction)
        .addListener(listener);
  }

  /**
   * Removes a connection listener from the factory.
   *
   * @param config the connection configuration
   * @param listener The connection listener
   */
  public void removeConnectionListener(ConnectionConfig config, ConnectionListener listener) {
    connectionManagers.computeIfAbsent(config, connectionManagerFactoryFunction)
        .removeListener(listener);
  }


  /**
   * Checks if the given listener is already added to the factory
   *
   * @param config the connection configuration
   * @param listener The connection listener
   * @return {@code true} if the given connection listener is already added, otherwise {@code false}
   */
  public boolean containsConnectionListener(ConnectionConfig config, ConnectionListener listener)
  {
    return connectionManagers.computeIfAbsent(config, connectionManagerFactoryFunction)
            .containsListener(listener);
  }
}
