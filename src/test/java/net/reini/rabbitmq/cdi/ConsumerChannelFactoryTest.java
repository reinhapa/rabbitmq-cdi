package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

@ExtendWith(MockitoExtension.class)
public class ConsumerChannelFactoryTest {
  private ConsumerChannelFactory sut;
  @Mock
  private ConnectionRepository connectionRepositoryMock;
  @Mock
  private ConnectionConfig configMock;
  @Mock
  private Connection connectionMock;
  @Mock
  private Channel channelMock;

  @BeforeEach
  void setUp() {
    this.sut = new ConsumerChannelFactory(connectionRepositoryMock, configMock);
  }

  @Test
  void createChannel() throws IOException {
    when(connectionRepositoryMock.getConnection(configMock)).thenReturn(connectionMock);
    when(connectionMock.createChannel()).thenReturn(channelMock);

    Channel channel = sut.createChannel();
    assertSame(channel, channelMock);
  }

  @Test
  void createChannelFailed() throws IOException {
    when(connectionRepositoryMock.getConnection(configMock)).thenThrow(new IOException("failed"));
    assertThrows(IOException.class, () -> {
      sut.createChannel();
    });
  }

}
