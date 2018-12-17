package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ConnectionRepositoryTest
{
  @Mock
  private Connection connection;
  @Mock
  private ConnectionConfiguration config;
  @Mock
  private ConnectionListener listener;
  @Mock
  private ConnectionFactory connectionFactory;

  private ConnectionRepository connectionRepository;

  @BeforeEach
  public void setUp() {
    connectionRepository = new ConnectionRepository(() -> connectionFactory);
  }

  @Test
  public void testClose() {
    connectionRepository.close();

    connectionRepository.registerConnectionListener(config, listener);
    connectionRepository.close();
    verify(listener).onConnectionClosed(null);
  }

  @Test
  public void testRegisterConnectionListener() {
    connectionRepository.registerConnectionListener(config, listener);
  }

  @Test
  public void testRemoveConnectionListener() {
    connectionRepository.removeConnectionListener(config, listener);
  }

  @Nested
  class ConnectionManagerTest
  {
    private ConnectionManager connectionManager;

    @BeforeEach
    public void setUp() {
      connectionManager = new ConnectionManager(config);
      connectionManager.listeners().add(listener);
    }

    @Test
    public void getConnection_closed() throws Exception {
      connectionManager.changeState(ConnectionState.CLOSED);
      verify(listener).onConnectionClosed(null);
      Throwable exception = assertThrows(IOException.class, () -> {
        connectionManager.getConnection(() -> connectionFactory);
      });
      assertEquals(exception.getMessage(),
          "Attempt to retrieve a connection from a closed connection factory");
    }
  }
}
