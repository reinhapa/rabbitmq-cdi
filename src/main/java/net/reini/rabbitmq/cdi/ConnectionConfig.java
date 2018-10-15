package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Contains the Rabbit MQ connection configuration
 *
 * @author Patrick Reinhart
 */
class ConnectionConfig {
  private final Set<Address> brokerHosts;

  private String username;
  private String password;
  private String virtualHost;

  ConnectionConfig() {
    brokerHosts = ConcurrentHashMap.newKeySet();
    username = "guest";
    password = "guest";
  }

  /**
   * @param username the username to set
   */
  void setUsername(String username) {
    this.username = username == null ? "guest" : username;
  }

  /**
   * @param password the password to set
   */
  void setPassword(String password) {
    this.password = password == null ? "guest" : password;
  }

  /**
   * @param virtualHost the virtualHost to set
   */
  void setVirtualHost(String virtualHost) {
    this.virtualHost = virtualHost;
  }

  void addHost(Address hostAddress) {
    brokerHosts.add(hostAddress);
  }

  /**
   * @return the brokerHosts
   */
  Set<Address> getBrokerHosts() {
    return brokerHosts;
  }

  /**
   * @return a new connection using the given connection factory
   * 
   * @throws TimeoutException if a timeout occurs
   * @throws IOException if the connection error occurs
   */
  Connection createConnection(ConnectionFactory connectionFactory)
      throws IOException, TimeoutException {
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);
    if (virtualHost != null) {
      connectionFactory.setVirtualHost(virtualHost);
    }
    if (brokerHosts.isEmpty()) {
      brokerHosts.add(new Address(connectionFactory.getHost(), connectionFactory.getPort()));
    }
    return connectionFactory.newConnection(brokerHosts.toArray(new Address[0]));
  }

  @Override
  public String toString() {
    return String.format("broker hosts: %s, connect user: %s", brokerHosts, username);
  }

  @Override
  public int hashCode() {
    return brokerHosts.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ConnectionConfig)) {
      return false;
    }
    ConnectionConfig other = (ConnectionConfig) obj;
    return brokerHosts.equals(other.brokerHosts) && username.equals(other.username)
        && password.equals(other.password) && Objects.equals(virtualHost, other.virtualHost);
  }
}
