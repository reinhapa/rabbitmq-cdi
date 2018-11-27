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
   * @param hosts the complete set of brocker hosts
   */
  void setHosts(Set<Address> hosts);
}
