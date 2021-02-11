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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Thread.State;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("boxing")
@ExtendWith(MockitoExtension.class)
class ConnectionManagerWatcherThreadTest {

  @Test
  void testEstablishConnectionNotPossible() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    ConnectionManager connectionManagerMock = mock(ConnectionManager.class);
    when(connectionManagerMock.tryToEstablishConnection()).thenReturn(false);
    when(connectionManagerMock.getState()).thenReturn(ConnectionState.NEVER_CONNECTED);
    ConnectionManagerWatcherThread sut =
        new ConnectionManagerWatcherThread(lock, condition, connectionManagerMock, 50);
    sut.start();
    Thread.sleep(500);
    assertTrue(sut.isAlive());
    verify(connectionManagerMock, atLeast(2)).tryToEstablishConnection();
    assertEquals(State.TIMED_WAITING, sut.getState());
    killThreadAndVerifyState(sut);
  }

  @Test
  void testEstablishConnectionNotPossibleWhileAlreadyConnecting() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    ConnectionManager connectionManagerMock = mock(ConnectionManager.class);
    when(connectionManagerMock.tryToEstablishConnection()).thenReturn(false);
    when(connectionManagerMock.getState()).thenReturn(ConnectionState.CONNECTING);
    ConnectionManagerWatcherThread sut =
        new ConnectionManagerWatcherThread(lock, condition, connectionManagerMock, 50);
    sut.start();
    Thread.sleep(500);
    assertTrue(sut.isAlive());
    verify(connectionManagerMock, atLeast(2)).tryToEstablishConnection();
    assertEquals(State.TIMED_WAITING, sut.getState());
    killThreadAndVerifyState(sut);
  }

  @Test
  void testEstablishConnectionSuccessfull() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    ConnectionManager connectionManagerMock = mock(ConnectionManager.class);
    when(connectionManagerMock.tryToEstablishConnection()).thenReturn(true);
    when(connectionManagerMock.getState()).thenReturn(ConnectionState.NEVER_CONNECTED);
    ConnectionManagerWatcherThread sut =
        new ConnectionManagerWatcherThread(lock, condition, connectionManagerMock, 50);
    sut.start();
    Thread.sleep(200);
    assertTrue(sut.isAlive());
    assertEquals(sut.isAlive(), sut.isRunning());
    verify(connectionManagerMock).tryToEstablishConnection();
    assertEquals(State.WAITING, sut.getState());
    killThreadAndVerifyState(sut);
    assertEquals(sut.isAlive(), sut.isRunning());
  }


  @Test
  void testEstablishConnectionSuccessfullButLostAfterSomeTime() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    ConnectionManager connectionManagerMock = mock(ConnectionManager.class);
    when(connectionManagerMock.tryToEstablishConnection()).thenReturn(true);
    when(connectionManagerMock.getState()).thenReturn(ConnectionState.NEVER_CONNECTED);
    ConnectionManagerWatcherThread sut =
        new ConnectionManagerWatcherThread(lock, condition, connectionManagerMock, 50);
    sut.start();
    Thread.sleep(300);
    assertTrue(sut.isAlive());
    verify(connectionManagerMock).tryToEstablishConnection();
    assertEquals(State.WAITING, sut.getState());
    try {
      lock.lock();
      condition.signalAll();
    } finally {
      lock.unlock();
    }
    Thread.sleep(200);
    verify(connectionManagerMock, times(2)).tryToEstablishConnection();
    assertEquals(State.WAITING, sut.getState());
    killThreadAndVerifyState(sut);
  }

  private void killThreadAndVerifyState(ConnectionManagerWatcherThread sut) throws InterruptedException {
    sut.stopThread();
    waitForThreadDeathWithTimeout(50,sut);
  }

  private void waitForThreadDeathWithTimeout(int retries, Thread stopperThread) throws InterruptedException {
    int count=1;
    while(stopperThread.isAlive() || count >=retries);
    {
      Thread.sleep(100);
    }
  }

}
