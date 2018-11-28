package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Function responsible to create new connections using a given connection factory.
 *
 * @author Patrick Reinhart
 */
public interface ConnectionConfig {
  /**
   * @param connectionFactory the connection factory used to create the actual connection
   * @return a new connection using the given connection factory
   * 
   * @throws TimeoutException if a timeout occurs
   * @throws IOException if the connection error occurs
   * @throws NoSuchAlgorithmException if the security context creation for secured connection fails
   */
  Connection createConnection(ConnectionFactory connectionFactory)
      throws IOException, TimeoutException, NoSuchAlgorithmException;
}
