package net.reini.rabbitmq.cdi;

import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

public class SSLContextFactory {
  public SSLContext createSSLContext() throws NoSuchAlgorithmException {
    return SSLContext.getDefault();
  }
}