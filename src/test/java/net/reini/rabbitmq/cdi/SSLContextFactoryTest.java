package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SSLContextFactoryTest {

  @Test
  void createSSLContext() throws NoSuchAlgorithmException {
    SSLContextFactory sslContextFactory = new SSLContextFactory();
    SSLContext sslContext = sslContextFactory.createSSLContext();
    Assertions.assertNotNull(sslContext);
  }
}