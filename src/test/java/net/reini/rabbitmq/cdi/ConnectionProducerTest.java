package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import net.reini.rabbitmq.cdi.ConnectionProducer.ConnectionState;
import net.reini.rabbitmq.cdi.ConnectionProducer.State;

@ExtendWith(MockitoExtension.class)
public class ConnectionProducerTest {
  @Mock
  private Connection connection;
  @Mock
  private ConnectionConfig config;
  @Mock
  private ConnectionListener listener;
  @Mock
  private ConnectionFactory connectionFactory;

  private ConnectionProducer connectionProducer;

  @BeforeEach
  public void setUp() {
    connectionProducer = new ConnectionProducer(() -> connectionFactory);
  }

  @Test
  public void testGetConnection() throws Exception {
    when(config.createConnection(connectionFactory)).thenReturn(connection);
    when(connection.isOpen()).thenReturn(true);

    assertEquals(connection, connectionProducer.getConnection(config));
  }

  @Test
  public void testClose() {
    connectionProducer.close();

    connectionProducer.registerConnectionListener(config, listener);
    connectionProducer.close();
    verify(listener).onConnectionClosed(null);
  }

  @Test
  public void testRegisterConnectionListener() {
    connectionProducer.registerConnectionListener(config, listener);
  }

  @Test
  public void testRemoveConnectionListener() {
    connectionProducer.removeConnectionListener(config, listener);
  }

  @Nested
  class ConnectionStateTest {
    private ConnectionState connectionState;

    @BeforeEach
    public void setUp() {
      connectionState = new ConnectionState(config);
      connectionState.listeners().add(listener);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, mode = Mode.EXCLUDE,
        names = {"CONNECTED", "CONNECTING", "CLOSED"})
    public void testChangeState(State state) {
      connectionState.changeState(state);
    }

    @Test
    public void getConnection_closed() throws Exception {
      connectionState.changeState(State.CLOSED);
      verify(listener).onConnectionClosed(null);
      Throwable exception = assertThrows(IOException.class, () -> {
        connectionState.getConnection(() -> connectionFactory);
      });
      assertEquals(exception.getMessage(),
          "Attempt to retrieve a connection from a closed connection factory");
    }
  }
}
