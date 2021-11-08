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
final class ConnectionConfiguration implements ConnectionConfig, ConnectionConfigHolder {
  private static final int DEFAULT_CONNECTION_HEARTBEAT_TIMEOUT_IN_SEC = 3;
  private static final int DEFAULT_CONNECT_TIMEOUT_IN_MS = 10_000;
  private static final int DEFAULT_WAIT_TIME_RETRY_CONNECT_IN_MS = 10_000;
  private static final long DEFAULT_WAIT_TIME_RETRY_ACTIVATE_CONSUMER_IN_MS = 10_000;

  private final List<Address> brokerHosts;

  private int requestedConnectionHeartbeatTimeout;
  private int connectTimeout;
  private long connectRetryWaitTime;
  private long failedConsumerActivationRetryTime;
  private boolean secure;
  private String username;
  private String password;
  private String virtualHost;
  private SSLContextFactory sslContextFactory;

  ConnectionConfiguration(SSLContextFactory sslContextFactory) {
    this.sslContextFactory = sslContextFactory;
    brokerHosts = new ArrayList<>();
    username = "guest";
    password = "guest";
    connectTimeout = DEFAULT_CONNECT_TIMEOUT_IN_MS;
    requestedConnectionHeartbeatTimeout = DEFAULT_CONNECTION_HEARTBEAT_TIMEOUT_IN_SEC;
    connectRetryWaitTime = DEFAULT_WAIT_TIME_RETRY_CONNECT_IN_MS;
    failedConsumerActivationRetryTime = DEFAULT_WAIT_TIME_RETRY_ACTIVATE_CONSUMER_IN_MS;
  }

  ConnectionConfiguration() {
    this(SSLContext::getDefault);
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
  public void setRequestedConnectionHeartbeatTimeout(int requestedHeartbeat) {
    this.requestedConnectionHeartbeatTimeout = requestedHeartbeat;
  }

  @Override
  public void setConnectTimeout(int timeout) {
    this.connectTimeout = timeout;
  }

  @Override
  public void setConnectRetryWaitTime(long waitTime) {
    this.connectRetryWaitTime = waitTime;
  }

  @Override
  public Connection createConnection(ConnectionFactory connectionFactory)
      throws IOException, TimeoutException {
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);
    connectionFactory.setRequestedHeartbeat(requestedConnectionHeartbeatTimeout);
    connectionFactory.setConnectionTimeout(connectTimeout);
    if (secure) {
      final SSLContext sslContext;
      try {
        sslContext = sslContextFactory.createSSLContext();
      } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException("error during connect, fatal system configuration", e);
      }
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
  public void setFailedConsumerActivationRetryTime(long failedConsumerActivationRetryTime) {
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
        && username.equals(other.username) && password.equals(other.password)
        && Objects.equals(virtualHost, other.virtualHost);
  }

  @Override
  public long getConnectRetryWaitTime() {
    return connectRetryWaitTime;
  }

  @Override
  public long getFailedConsumerActivationRetryTime() {
    return failedConsumerActivationRetryTime;
  }
}
