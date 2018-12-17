package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Contains the Rabbit MQ connection configuration
 *
 * @author Patrick Reinhart
 */
public class ConnectionConfiguration implements ConnectionConfigHolder
{
  private static final int DEFAULT_CONNECTION_HEARTBEAT_TIMEOUT_IN_SEC = 3;
  private static final int DEFAULT_CONNECT_TIMEOUT_IN_MS = 10000;
  private static final int DEFAULT_WAIT_TIME_RETRY_CONNECT_IN_MS = 10_000;
  private static final long DEFAULT_WAIT_TIME_RETRY_ACTIVATE_CONSUMER_IN_MS = 10000;

  private final List<Address> brokerHosts;

  private boolean secure;
  private String username;
  private String password;
  private String virtualHost;
  private int requestedConnectionHeartbeatTimeout;
  private int connectTimeout;
  private long connectRetryWaitTime;
  private long failedConsumerActivationRetryTime;

  ConnectionConfiguration() {
    brokerHosts = new ArrayList<>();
    username = "guest";
    password = "guest";
    this.connectTimeout = DEFAULT_CONNECT_TIMEOUT_IN_MS;
    this.requestedConnectionHeartbeatTimeout = DEFAULT_CONNECTION_HEARTBEAT_TIMEOUT_IN_SEC;
    this.connectRetryWaitTime = DEFAULT_WAIT_TIME_RETRY_CONNECT_IN_MS;
    this.failedConsumerActivationRetryTime = DEFAULT_WAIT_TIME_RETRY_ACTIVATE_CONSUMER_IN_MS;
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
  public void setRequestedConnectionHeartbeatTimeout(int requestedHeartbeat)
  {
    this.requestedConnectionHeartbeatTimeout = requestedHeartbeat;
  }

  @Override
  public void setConnectTimeout(int timeout)
  {
    this.connectTimeout = timeout;
  }

  @Override
  public void setConnectRetryWaitTime(long waitTime)
  {
    this.connectRetryWaitTime = waitTime;
  }

  public Connection createConnection(ConnectionFactory connectionFactory)
      throws IOException, TimeoutException, NoSuchAlgorithmException {
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);
    connectionFactory.setRequestedHeartbeat(requestedConnectionHeartbeatTimeout);
    connectionFactory.setConnectionTimeout(connectTimeout);
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

  public void setFailedConsumerActivationRetryTime(long failedConsumerActivationRetryTime)
  {
    this.failedConsumerActivationRetryTime = failedConsumerActivationRetryTime;
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

  boolean isSecure()
  {
    return secure;
  }

  String getUsername()
  {
    return username;
  }

  String getPassword()
  {
    return password;
  }

  String getVirtualHost()
  {
    return virtualHost;
  }

  int getRequestedConnectionHeartbeatTimeout()
  {
    return requestedConnectionHeartbeatTimeout;
  }

  int getConnectTimeout()
  {
    return connectTimeout;
  }

  long getConnectRetryWaitTime()
  {
    return connectRetryWaitTime;
  }

  long getFailedConsumerActivationRetryTime()
  {
    return failedConsumerActivationRetryTime;
  }
}
