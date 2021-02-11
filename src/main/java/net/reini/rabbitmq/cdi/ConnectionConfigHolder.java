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
   * Set the requested heartbeat timeout. Heartbeat frames will be sent at about 1/2 the timeout
   * interval. If server heartbeat timeout is configured to a non-zero value, this method can only
   * be used to lower the value; otherwise any value provided by the client will be used.
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
   * Set the time to sleep between connection attempts, this only applies if the connection was not
   * recoverable and a complete reconnect is needed and also during the first connect attempt.
   *
   * @param waitTime time in milli seconds to wait between retries
   */
  void setConnectRetryWaitTime(long waitTime);

  /**
   * Set the time to sleep between retries to activate consumers
   *
   * @param waitTime time in milli seconds to wait between retries
   */
  void setFailedConsumerActivationRetryTime(long waitTime);
}
