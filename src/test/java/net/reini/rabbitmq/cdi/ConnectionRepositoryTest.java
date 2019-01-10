package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConnectionRepositoryTest {

  @Mock
  private ConnectionConfig configMock;
  @Mock
  private ConnectionListener listener;

  @Mock
  private ConnectionManager connectionManagerMock;

  @Mock
  private Function<ConnectionConfig, ConnectionManager> factoryFunctionMock;

  private ConnectionRepository sut;


  @BeforeEach
  public void setUp() {
    lenient().when(factoryFunctionMock.apply(Mockito.any())).thenReturn(connectionManagerMock);
    sut = new ConnectionRepository(factoryFunctionMock);
  }

  @Test
  public void testClose() {
    sut.close();

    sut.registerConnectionListener(configMock, listener);
    sut.close();
    verify(connectionManagerMock).close();
  }

  @Test
  public void testRegisterConnectionListener() {
    sut.registerConnectionListener(configMock, listener);
    verify(connectionManagerMock).addListener(listener);
  }

  @Test
  public void testRemoveConnectionListener() {

    sut.removeConnectionListener(configMock, listener);
    verify(connectionManagerMock).removeListener(listener);
  }

  @Test
  void testConnect() {
    sut.connect(configMock);
    verify(factoryFunctionMock).apply(configMock);
    verify(connectionManagerMock).connect();
  }

  @Test
  void testConnectTwice() {
    sut.connect(configMock);
    sut.connect(configMock);
    verify(factoryFunctionMock).apply(configMock);
  }

  @Test
  void testGetConnection() throws IOException {
    sut.getConnection(configMock);
    verify(connectionManagerMock).getConnection();

  }

  @Test
  void testConstructor() {
    ConnectionRepository connectionRepository = new ConnectionRepository();
    connectionRepository.connect(configMock);
    Assertions.assertNotNull(connectionRepository);
  }
}
