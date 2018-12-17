package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConnectionRepositoryTest {

  @Mock
  private ConnectionConfiguration config;
  @Mock
  private ConnectionListener listener;
  @Mock
  private ConnectionFactory connectionFactory;

  @Mock
  private ConnectionManager connectionManagerMock;

  private Supplier<ConnectionFactory> connectionFactorySupplier;

  private ConnectionRepository sut;


  private Function<ConnectionConfiguration, ConnectionManager> factoryFunction = (ConnectionConfiguration connectionConfiguration) -> {
    return connectionManagerMock;
  };

  @BeforeEach
  public void setUp() {
    this.connectionFactorySupplier = () -> connectionFactory;
    sut = new ConnectionRepository(connectionFactorySupplier, factoryFunction);
  }

  @Test
  public void testClose() {
    sut.close();

    sut.registerConnectionListener(config, listener);
    sut.close();
    verify(connectionManagerMock).close();
  }

  @Test
  public void testRegisterConnectionListener() {
    sut.registerConnectionListener(config, listener);
    verify(connectionManagerMock).addListener(listener);
  }

  @Test
  public void testRemoveConnectionListener() {

    sut.removeConnectionListener(config, listener);
    verify(connectionManagerMock).removeListener(listener);
  }

  @Nested
  class ConnectionManagerTest {

    private ConnectionManager connectionManager;

    @BeforeEach
    public void setUp() {
      connectionManager = new ConnectionManager(config);
      connectionManager.addListener(listener);
    }

    @Test
    public void testConnect() {
      sut.connect(config);
      verify(connectionManagerMock).connect(connectionFactorySupplier);
    }

    @Test
    public void testGetConnection() throws IOException {
      sut.getConnection(config);
      verify(connectionManagerMock).getConnection(connectionFactorySupplier);
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

    @Test
    public void testNotifyConnecting(){
      connectionManager.changeState(ConnectionState.CONNECTING);
      verify(listener).onConnectionLost(null);
    }

    @Test
    public void testNotifyConnected(){
      connectionManager.changeState(ConnectionState.CONNECTED);
      verify(listener).onConnectionEstablished(null);
    }


  }

}
