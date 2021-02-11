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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Address;

import net.reini.rabbitmq.cdi.EventBinder.BinderConfiguration;

/**
 * Tests the binder configuration part.
 *
 * @author Patrick Reinhart
 */
@ExtendWith(MockitoExtension.class)
public class BinderConfigurationTest {
  @Mock
  private ConnectionConfigHolder config;

  private BinderConfiguration binderConfig;

  @BeforeEach
  public void setUp() {
    binderConfig = new BinderConfiguration(config);
  }

  /**
   * Test method for {@link BinderConfiguration#setHost(String)}.
   */
  @SuppressWarnings({"deprecation", "javadoc"})
  @Test
  public void testSetHost() {
    assertSame(binderConfig, binderConfig.setHost("hostName"));

    verify(config).addHost(new Address("hostName"));
  }

  /**
   * Test method for {@link BinderConfiguration#addHost(String)}.
   */
  @Test
  public void testAddHostString() {
    assertSame(binderConfig, binderConfig.addHost("hostName:123"));

    verify(config).addHost(new Address("hostName", 123));
  }

  /**
   * Test method for {@link BinderConfiguration#addHost(com.rabbitmq.client.Address)}.
   */
  @Test
  public void testAddHostAddress() {
    Address host = new Address("hostName", 1234);

    assertSame(binderConfig, binderConfig.addHost(host));

    verify(config).addHost(host);
  }

  /**
   * Test method for {@link BinderConfiguration#setUsername(String)}.
   */
  @Test
  public void testSetUsername() {
    assertSame(binderConfig, binderConfig.setUsername("username"));

    verify(config).setUsername("username");
  }

  /**
   * Test method for {@link BinderConfiguration#setPassword(String)}.
   */
  @Test
  public void testSetPassword() {
    assertSame(binderConfig, binderConfig.setPassword("password"));

    verify(config).setPassword("password");
  }

  /**
   * Test method for {@link BinderConfiguration#setVirtualHost(String)}.
   */
  @Test
  public void testSetVirtualHost() {
    assertSame(binderConfig, binderConfig.setVirtualHost("virtualHost"));

    verify(config).setVirtualHost("virtualHost");
  }

  /**
   * Test method for {@link BinderConfiguration#setSecure(boolean)}.
   */
  @Test
  public void testSetSecure() {
    assertSame(binderConfig, binderConfig.setSecure(true));
    assertSame(binderConfig, binderConfig.setSecure(false));

    verify(config).setSecure(true);
    verify(config).setSecure(false);
  }


  /**
   * Test method for {@link BinderConfiguration#setConnectionUri(java.net.URI)}.
   */
  @Test
  public void testSetConnectionUri_unkown_scheme() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      binderConfig.setConnectionUri(URI.create("xXXx://flamingo.rmq.cloudamqp.com"));
    });
    assertEquals("Wrong scheme in AMQP URI: xXXx", exception.getMessage());
  }

  /**
   * Test method for {@link BinderConfiguration#setConnectionUri(java.net.URI)}.
   */
  @Test
  public void testSetConnectionUri_unkown_host() {
    assertSame(binderConfig, binderConfig.setConnectionUri(URI.create("amqp://?")));

    verify(config).setHosts(Collections.singleton(new Address("127.0.0.1", 5672)));
  }

  /**
   * Test method for {@link BinderConfiguration#setConnectionUri(java.net.URI)}.
   */
  @Test
  public void testSetConnectionUri_unkown_credentials_part() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      binderConfig.setConnectionUri(URI.create("amqp://xx:yyy:zzz@flamingo.rmq.cloudamqp.com"));
    });
    assertEquals("Bad user info in AMQP URI: xx:yyy:zzz", exception.getMessage());
  }

  /**
   * Test method for {@link BinderConfiguration#setConnectionUri(java.net.URI)}.
   */
  @Test
  public void testSetConnectionUri_server_only() {
    assertSame(binderConfig,
        binderConfig.setConnectionUri(URI.create("Amqp://flamingo.rmq.cloudamqp.com/")));

    verify(config).setHosts(Collections.singleton(new Address("flamingo.rmq.cloudamqp.com", 5672)));
  }

  /**
   * Test method for {@link BinderConfiguration#setConnectionUri(java.net.URI)}.
   */
  @Test
  public void testSetConnectionUri_illegal_virtual_host() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      binderConfig.setConnectionUri(URI.create("amqp://flamingo.rmq.cloudamqp.com/xxxx/vvv"));
    });
    assertEquals("Multiple segments in path of AMQP URI: /xxxx/vvv", exception.getMessage());
  }

  /**
   * Test method for {@link BinderConfiguration#setConnectionUri(java.net.URI)}.
   */
  @Test
  public void testSetConnectionUri_server_and_port() {
    assertSame(binderConfig,
        binderConfig.setConnectionUri(URI.create("amqp://flamingo.rmq.cloudamqp.com:1234")));

    verify(config).setHosts(Collections.singleton(new Address("flamingo.rmq.cloudamqp.com", 1234)));
  }

  /**
   * Test method for {@link BinderConfiguration#setConnectionUri(java.net.URI)}.
   */
  @Test
  public void testSetConnectionUri_secured_server_only() {
    assertSame(binderConfig,
        binderConfig.setConnectionUri(URI.create("amqps://flamingo.rmq.cloudamqp.com")));

    verify(config).setHosts(Collections.singleton(new Address("flamingo.rmq.cloudamqp.com", 5671)));
    verify(config).setSecure(true);
  }

  /**
   * Test method for {@link BinderConfiguration#setConnectionUri(java.net.URI)}.
   */
  @Test
  public void testSetConnectionUri_secured_server_user_port() {
    assertSame(binderConfig,
        binderConfig.setConnectionUri(URI.create("Amqps://test@flamingo.rmq.cloudamqp.com:1234")));

    verify(config).setUsername("test");
    verify(config).setHosts(Collections.singleton(new Address("flamingo.rmq.cloudamqp.com", 1234)));
    verify(config).setSecure(true);
  }


  /**
   * Test method for {@link BinderConfiguration#setConnectionUri(java.net.URI)}.
   */
  @Test
  public void testSetConnectionUri_secured() {
    assertSame(binderConfig, binderConfig
        .setConnectionUri(URI.create("amqps://user:password@flamingo.rmq.cloudamqp.com/nkjoriiy")));

    verify(config).setUsername("user");
    verify(config).setPassword("password");
    verify(config).setHosts(Collections.singleton(new Address("flamingo.rmq.cloudamqp.com", 5671)));
    verify(config).setSecure(true);
    verify(config).setVirtualHost("nkjoriiy");
  }

  @Test
  public void testSetConnectTimeout() {
    assertSame(binderConfig, binderConfig.setConnectTimeout(4000));
    verify(config).setConnectTimeout(4000);
  }

  @Test
  public void testSetConnectRetryWaitTime() {
    assertSame(binderConfig, binderConfig.setConnectRetryWaitTime(3000));
    verify(config).setConnectRetryWaitTime(3000);
  }

  @Test
  public void testSetRequestedConnectionHeartbeatTimeout() {
    assertSame(binderConfig, binderConfig.setRequestedConnectionHeartbeatTimeout(5));
    verify(config).setRequestedConnectionHeartbeatTimeout(5);
  }

  @Test
  public void testSetFailedConsumerActivationRetryTime() {
    assertSame(binderConfig, binderConfig.setFailedConsumerActivationRetryTime(4000));
    verify(config).setFailedConsumerActivationRetryTime(4000);
  }
}
