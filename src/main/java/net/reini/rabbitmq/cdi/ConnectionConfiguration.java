package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
class ConnectionConfiguration implements ConnectionConfig, ConnectionConfigHolder {
  private final List<Address> brokerHosts;

  private boolean secure;
  private String username;
  private String password;
  private String virtualHost;

  ConnectionConfiguration() {
    brokerHosts = new ArrayList<>();
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
  public void setHosts(Set<Address> hosts) {
    brokerHosts.clear();
    brokerHosts.addAll(hosts);
  }

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
      throw new IllegalArgumentException("No broker host defined");
    }
    return connectionFactory.newConnection(new ArrayList<>(brokerHosts));
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
    } else if (!(obj instanceof ConnectionConfiguration)) {
      return false;
    }
    ConnectionConfiguration other = (ConnectionConfiguration) obj;
    return secure == other.secure && brokerHosts.equals(other.brokerHosts)
        && username.equals(other.username)
        && password.equals(other.password) && Objects.equals(virtualHost, other.virtualHost);
  }
}
