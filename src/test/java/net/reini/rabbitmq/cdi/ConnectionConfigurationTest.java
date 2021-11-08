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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.SslContextFactory;

/**
 * Tests the {@link ConnectionConfiguration} implementation.
 *
 * @author Patrick Reinhart
 */
@ExtendWith(MockitoExtension.class)
public class ConnectionConfigurationTest {
  @Mock
  private ConnectionFactory connectionFactory;
  @Mock
  private SSLContextFactory sslContextFactoryMock;

  private Address expectedAddress;
  private ConnectionConfiguration configuration;

  @BeforeEach
  public void setUp() {
    expectedAddress = new Address("somehost.somedomain", 5672);
    configuration = new ConnectionConfiguration();
  }

  /**
   * Test method for {@link ConnectionConfiguration#setUsername(String)}.
   */
  @Test
  public void testSetUsername_null_value() throws Exception {
    configuration.setUsername(null);
    assertConnection("guest", null, null, false, null);
  }

  /**
   * Test method for {@link ConnectionConfiguration#setUsername(String)}.
   */
  @Test
  public void testSetUsername() throws Exception {
    configuration.setUsername("username");
    assertConnection("username", null, null, false, null);
  }

  /**
   * Test method for {@link ConnectionConfiguration#setPassword(String)}.
   */
  @Test
  public void testSetPassword_null_value() throws Exception {
    configuration.setPassword(null);
    assertConnection(null, "guest", null, false, null);
  }

  /**
   * Test method for {@link ConnectionConfiguration#setPassword(String)}.
   */
  @Test
  public void testSetPassword() throws Exception {
    configuration.setPassword("password");
    assertConnection(null, "password", null, false, null);
  }

  /**
   * Test method for {@link ConnectionConfiguration#setVirtualHost(String)}.
   */
  @Test
  public void testSetVirtualHost() throws Exception {
    configuration.setVirtualHost("virtualHost");
    assertConnection(null, null, "virtualHost", false, null);
  }

  @Test
  public void testSetRequestedConnectionHeartbeatTimeout() throws Exception {
    configuration.setRequestedConnectionHeartbeatTimeout(300);
    Address hostAddress = new Address("host.somedomain", 5671);
    configuration.addHost(hostAddress);
    configuration.createConnection(connectionFactory);
    verify(connectionFactory).setRequestedHeartbeat(300);
  }

  @Test
  public void testSetConnectTimeout() throws Exception {
    configuration.setConnectTimeout(300);
    Address hostAddress = new Address("host.somedomain", 5671);
    configuration.addHost(hostAddress);
    configuration.createConnection(connectionFactory);
    verify(connectionFactory).setConnectionTimeout(300);
  }

  @Test
  public void testSetConnectRetryWaitTime() throws Exception {
    configuration.setConnectRetryWaitTime(300);
    assertEquals(300, configuration.getConnectRetryWaitTime());
  }

  @Test
  public void testSetFailedConsumerActivationRetryTime() throws Exception {
    configuration.setFailedConsumerActivationRetryTime(300);
    assertEquals(300, configuration.getFailedConsumerActivationRetryTime());
  }

  /**
   * Test method for {@link ConnectionConfiguration#setSecure(boolean)}.
   */
  @Test
  public void testSetSecure() throws Exception {
    configuration.setSecure(true);
    assertConnection(null, null, null, true, null);
  }

  /**
   * Test method for {@link ConnectionConfiguration#addHost(Address)}.
   */
  @Test
  public void testAddHost() throws Exception {
    Address hostAddress = new Address("host.somedomain", 5671);
    configuration.addHost(hostAddress);
    assertConnection(null, null, null, false, asList(hostAddress));
  }

  /**
   * Test method for {@link ConnectionConfiguration#setHosts(Set)}.
   */
  @Test
  public void testSetHosts() throws Exception {
    Address host1 = new Address("somehost1.somedomain", 5672);
    Address host2 = new Address("somehost2.somedomain", 5672);
    Set<Address> hosts = new LinkedHashSet<>();
    hosts.add(host1);
    hosts.add(host2);
    configuration.setHosts(hosts);
    assertConnection(null, null, null, false, asList(host1, host2));
  }

  /**
   * Test method for {@link ConnectionConfiguration#createConnection(ConnectionFactory)}.
   */
  @Test
  public void testCreateConnection_no_broker_host() throws Exception {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      configuration.createConnection(connectionFactory);
    });
    assertEquals("No broker host defined", exception.getMessage());
    verify(connectionFactory).setUsername("guest");
    verify(connectionFactory).setPassword("guest");
  }

  /**
   * Test method for {@link ConnectionConfiguration#toString()}.
   */
  @Test
  public void testToString() {
    assertEquals("broker hosts: [], connect user: guest", configuration.toString());
    Set<Address> hosts = new LinkedHashSet<>();
    hosts.add(expectedAddress);
    hosts.add(new Address("somehost2.somedomain", 5672));
    configuration.setHosts(hosts);
    configuration.setUsername("username");
    assertEquals(
        "broker hosts: [somehost.somedomain:5672, somehost2.somedomain:5672], connect user: username",
        configuration.toString());
  }

  /**
   * Test method for {@link ConnectionConfiguration#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(Arrays.asList().hashCode(), configuration.hashCode());
    configuration.addHost(expectedAddress);
    assertEquals(Arrays.asList(expectedAddress).hashCode(), configuration.hashCode());
    Address hostAddress = new Address("host.somedomain", 5671);
    configuration.addHost(hostAddress);
    assertEquals(Arrays.asList(expectedAddress, hostAddress).hashCode(), configuration.hashCode());
  }

  /**
   * Test method for {@link ConnectionConfiguration#equals(Object)}.
   */
  @Test
  public void testEquals() {
    assertNotEquals(configuration, null);
    assertNotEquals(configuration, new Object());

    assertEquals(configuration, configuration);

    ConnectionConfiguration configuration1 = new ConnectionConfiguration();
    assertEquals(configuration, configuration1);
    assertEquals(configuration1, configuration);

    ConnectionConfiguration configuration2 = new ConnectionConfiguration();
    assertEquals(configuration, configuration2);

    assertEquals(configuration1, configuration2);

    assertEquals(configuration.hashCode(), configuration1.hashCode());
    assertEquals(configuration.hashCode(), configuration2.hashCode());
    assertEquals(configuration1.hashCode(), configuration2.hashCode());

    // remaining false cases
    assertNotEquals(configuration, connectionConfiguration(c -> c.addHost(new Address("host"))));
    assertNotEquals(configuration, connectionConfiguration(c -> c.setPassword("password")));
    assertNotEquals(configuration, connectionConfiguration(c -> c.setUsername("username")));
    assertNotEquals(configuration, connectionConfiguration(c -> c.setVirtualHost("virtualHost")));
    assertNotEquals(configuration, connectionConfiguration(c -> c.setSecure(true)));
  }


  @Test
  public void testProblemWithSSLContextInitialisation() throws Exception {
    doThrow(new NoSuchAlgorithmException()).when(sslContextFactoryMock).createSSLContext();

    Throwable exception = assertThrows(IllegalStateException.class, () -> {
      configuration = new ConnectionConfiguration(sslContextFactoryMock);
      configuration.setSecure(true);
      configuration.addHost(expectedAddress);
      configuration.createConnection(connectionFactory);

    });
    assertEquals("error during connect, fatal system configuration", exception.getMessage());
  }

  private static ConnectionConfiguration connectionConfiguration(
      Consumer<ConnectionConfiguration> configurator) {
    ConnectionConfiguration tested = new ConnectionConfiguration();
    configurator.accept(tested);
    return tested;
  }

  private void assertConnection(String username, String password, String virtualHost,
      boolean secure, List<Address> addresses) throws Exception {
    if (addresses == null) {
      configuration.addHost(expectedAddress);
    }
    configuration.createConnection(connectionFactory);

    ArgumentCaptor<SslContextFactory> sslFactoryCapture =
        ArgumentCaptor.forClass(SslContextFactory.class);

    verify(connectionFactory).setUsername(username == null ? "guest" : username);
    verify(connectionFactory).setPassword(password == null ? "guest" : password);
    if (secure) {
      verify(connectionFactory).setSslContextFactory(sslFactoryCapture.capture());
      assertEquals(SSLContext.getDefault(), sslFactoryCapture.getValue().create(null));
    }
    if (virtualHost != null) {
      verify(connectionFactory).setVirtualHost(virtualHost);
    }
    verify(connectionFactory)
        .newConnection(addresses == null ? asList(expectedAddress) : addresses);
  }

}
