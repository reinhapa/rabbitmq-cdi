package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;

@ExtendWith(MockitoExtension.class)
class ConsumerContainerTest {
  @Mock
  private ConsumerContainer consumerContainer;
  @Mock
  private ConnectionProducer connectionProducer;
  @Mock
  private Connection connection;
  @Mock
  private Channel channelOne;
  @Mock
  private Channel channelTwo;
  @Mock
  private EventConsumer consumer;
  @Mock
  private EventConsumer consumerAutoAck;

  private ConnectionConfig config;

  @BeforeEach
  void prepare() {
    consumerContainer = new ConsumerContainer(config, connectionProducer);
  }

  @Test
  void testCreateChannel() throws NoSuchAlgorithmException, IOException, TimeoutException {
    when(connectionProducer.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channelOne);

    assertEquals(channelOne, consumerContainer.createChannel());
  }

  @Test
  void testStartAllConsumers() throws NoSuchAlgorithmException, IOException, TimeoutException {
    ArgumentCaptor<ConnectionListener> listenerCaptor =
        ArgumentCaptor.forClass(ConnectionListener.class);

    when(connectionProducer.getConnection(config)).thenReturn(connection);
    when(connection.createChannel()).thenReturn(channelOne, channelTwo, channelOne)
        .thenThrow(new IOException("channel create error"));
    doNothing().doThrow(new IOException("channel close error")).when(channelOne).close();
    doNothing().when(channelTwo).close();

    consumerContainer.addConsumer(consumer, "queueOne", false);
    consumerContainer.addConsumer(consumerAutoAck, "queueTwo", true);
    consumerContainer.startAllConsumers();

    verify(channelOne).basicConsume(eq("queueOne"), eq(false), any(Consumer.class));
    verify(channelTwo).basicConsume(eq("queueTwo"), eq(true), any(Consumer.class));

    verify(connectionProducer).registerConnectionListener(eq(config), listenerCaptor.capture());
    assertEquals(1, listenerCaptor.getAllValues().size());

    ConnectionListener listener = listenerCaptor.getValue();
    listener.onConnectionLost(connection);
    listener.onConnectionEstablished(connection);
    listener.onConnectionClosed(connection);
  }

}
