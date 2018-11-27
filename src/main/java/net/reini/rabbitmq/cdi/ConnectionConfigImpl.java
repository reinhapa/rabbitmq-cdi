package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Contains the Rabbit MQ connection configuration
 *
 * @author Patrick Reinhart
 */
class ConnectionConfigImpl implements ConnectionConfig, ConnectionConfigHolder {
  private final Set<Address> brokerHosts;

  private boolean secure;
  private String username;
  private String password;
  private String virtualHost;

  ConnectionConfigImpl() {
    brokerHosts = ConcurrentHashMap.newKeySet();
    username = "guest";
    password = "guest";
  }

  @Override
  public void setUsername(String username) {
    this.username = username == null ? "guest" : username;
  }

  @Override
  public void setPassword(String password) {
    this.password = password == null ? "guest" : password;
  }

  @Override
  public void setVirtualHost(String virtualHost) {
    this.virtualHost = virtualHost;
  }

  @Override
  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  @Override
  public void addHost(Address hostAddress) {
    brokerHosts.add(hostAddress);
  }

  @Override
  public void setHosts(java.util.Set<Address> hosts) {}

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
   * @throws NoSuchAlgorithmException if the security context creation for secured connection fails
   */
  @Override
  public Connection createConnection(ConnectionFactory connectionFactory)
      throws IOException, TimeoutException, NoSuchAlgorithmException {
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);
    if (secure) {
      SSLContext sslContext = SSLContext.getDefault();
      connectionFactory.setSslContextFactory(name -> sslContext);
    }
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
    } else if (!(obj instanceof ConnectionConfigImpl)) {
      return false;
    }
    ConnectionConfigImpl other = (ConnectionConfigImpl) obj;
    return brokerHosts.equals(other.brokerHosts) && username.equals(other.username)
        && password.equals(other.password) && Objects.equals(virtualHost, other.virtualHost);
  }
}
