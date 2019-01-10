package net.reini.rabbitmq.cdi;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

@FunctionalInterface
public interface SSLContextFactory {
  /**
   * @return an new {@link SSLContext} instance-
   * @throws NoSuchAlgorithmException if the security context creation for secured connection fails
   */
  public SSLContext createSSLContext() throws NoSuchAlgorithmException;
}
