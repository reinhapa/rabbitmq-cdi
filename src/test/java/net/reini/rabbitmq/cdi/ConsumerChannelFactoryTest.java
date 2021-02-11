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
import com.rabbitmq.client.RecoverableChannel;

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
  private RecoverableChannel channelMock;

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
