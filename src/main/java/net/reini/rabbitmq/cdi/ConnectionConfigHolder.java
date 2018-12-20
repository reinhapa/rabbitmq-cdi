package net.reini.rabbitmq.cdi;

import java.util.Set;

import com.rabbitmq.client.Address;

/**
 * Holds the configuration for a AMQP connection.
 *
 * @author Patrick Reinhart
 */
interface ConnectionConfigHolder {

  /**
   * @param username the user name to set
   */
  void setUsername(String username);

  /**
   * @param password the password to set
   */
  void setPassword(String password);

  /**
   * @param virtualHost the virtualHost to set
   */
  void setVirtualHost(String virtualHost);

  /**
   * @param secure {@code true} to use secured connection, {@code false} otherwise
   */
  void setSecure(boolean secure);

  /**
   * @param host broker host to be added
   */
  void addHost(Address host);

  /**
   * @param hosts the complete set of broker hosts
   */
  void setHosts(Set<Address> hosts);

  /**
   * Set the requested heartbeat timeout. Heartbeat frames will be sent at about 1/2 the timeout interval. If server heartbeat timeout is configured to a non-zero value, this method can only be used
   * to lower the value; otherwise any value provided by the client will be used.
   *
   * @param requestedHeartbeat the initially requested heartbeat timeout, in seconds; zero for none
   * @see <a href="http://rabbitmq.com/heartbeats.html">RabbitMQ Heartbeats Guide</a>
   */
  void setRequestedConnectionHeartbeatTimeout(int requestedHeartbeat);

  /**
   * Set the TCP connection timeout.
   *
   * @param timeout connection TCP establishment timeout in milliseconds; zero for infinite
   */
  void setConnectTimeout(int timeout);

  /**
   * Set the time to sleep between connection attempts, this only applies if the connection was not recoverable and a complete reconnect is needed and also during the first connect attempt.
   *
   * @param waitTime time in milli seconds to wait between retries
   */
  void setConnectRetryWaitTime(long waitTime);

  /**
   * Set the time to sleep between retries to  activate consumers
   *
   * @param waitTime time in milli seconds to wait between retries
   */
  void setFailedConsumerActivationRetryTime(long waitTime);
}
