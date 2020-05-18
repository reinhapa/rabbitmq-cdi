/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015, 2019 Patrick Reinhart
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

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
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
  public void testContainsConnectionListener() {
    sut.containsConnectionListener(configMock, listener);
    verify(connectionManagerMock, times( 1 )).containsListener( listener );
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
