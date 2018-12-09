package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

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
  public void testConnectionProducer() {
    connectionProducer = new ConnectionProducer();
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

    @Test
    public void testShutdownCompleted_non_hard_error() {
      connectionState.shutdownCompleted(new ShutdownSignalException(false, false, null, null),
          () -> connectionFactory);
    }

    @Test
    public void testShutdownCompleted()
        throws NoSuchAlgorithmException, IOException, TimeoutException {
      when(config.createConnection(connectionFactory)).thenThrow(new IOException())
          .thenReturn(connection);

      connectionState.shutdownCompleted(new ShutdownSignalException(true, false, null, null),
          () -> connectionFactory);
    }

    @Test
    public void testShutdownCompleted_interrupted()
        throws NoSuchAlgorithmException, IOException, TimeoutException, InterruptedException {
      when(config.createConnection(connectionFactory)).thenThrow(new IOException());

      Thread test = new Thread(() -> {
        connectionState.shutdownCompleted(new ShutdownSignalException(true, false, null, null),
            () -> connectionFactory);
      });
      test.start();
      Thread.sleep(3000);
      test.interrupt();
    }

    @Test
    public void testClose() throws NoSuchAlgorithmException, IOException, TimeoutException {
      when(config.createConnection(connectionFactory)).thenReturn(connection);

      connectionState.establishConnection(() -> connectionFactory);
      connectionState.close();
      // test second close is ignored
      connectionState.close();

      IOException ioEx = assertThrows(IOException.class, () -> {
        connectionState.establishConnection(() -> connectionFactory);
      });
      assertEquals("Attempt to establish a connection with a closed connection factory",
          ioEx.getMessage());
    }

    @Test
    public void testClose_failing() throws NoSuchAlgorithmException, IOException, TimeoutException {
      when(config.createConnection(connectionFactory)).thenReturn(connection);
      doThrow(new IOException()).when(connection).close();

      connectionState.establishConnection(() -> connectionFactory);
      // multiple call
      connectionState.establishConnection(() -> connectionFactory);

      connectionState.close();
    }

    @Test
    public void testClose_listner() throws NoSuchAlgorithmException, IOException, TimeoutException {
      ArgumentCaptor<ShutdownListener> listenerCaptor =
          ArgumentCaptor.forClass(ShutdownListener.class);
      when(config.createConnection(connectionFactory)).thenReturn(connection);

      connectionState.establishConnection(() -> connectionFactory);
      connectionState.close();

      verify(connection).addShutdownListener(listenerCaptor.capture());
      listenerCaptor.getValue()
          .shutdownCompleted(new ShutdownSignalException(true, false, null, null));
    }

    @Test
    public void testClose_failing_still_open()
        throws NoSuchAlgorithmException, IOException, TimeoutException {
      when(config.createConnection(connectionFactory)).thenReturn(connection);
      doThrow(new IOException()).when(connection).close();
      when(connection.isOpen()).thenReturn(true);

      connectionState.establishConnection(() -> connectionFactory);

      connectionState.close();
    }

    @Test
    public void testGetConnection_fails()
        throws NoSuchAlgorithmException, IOException, TimeoutException {
      when(config.createConnection(connectionFactory)).thenReturn(connection);
      when(connection.isOpen()).thenReturn(false);

      IOException ioEx = assertThrows(IOException.class, () -> {
        connectionState.getConnection(() -> connectionFactory);
      });
      assertEquals("Unable to retrieve connection", ioEx.getMessage());
    }
  }
}
