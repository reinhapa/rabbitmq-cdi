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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Thread.State;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("boxing")
@ExtendWith(MockitoExtension.class)
class ConsumerContainerWatcherThreadTest {
  @Mock
  ConsumerContainer consumerContainerMock;

  @Test
  void testNoConnectionBehaviour() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    Condition noConnectionCondition = lock.newCondition();
    when(consumerContainerMock.isConnectionAvailable()).thenReturn(false);
    ConsumerContainerWatcherThread consumerContainerWatcherThread =
        new ConsumerContainerWatcherThread(consumerContainerMock, 100, lock, noConnectionCondition);
    consumerContainerWatcherThread.start();
    Thread.sleep(300);
    assertTrue(consumerContainerWatcherThread.isAlive());
    assertEquals(State.WAITING, consumerContainerWatcherThread.getState());
    verify(consumerContainerMock, never()).ensureConsumersAreActive();

    killThreadAndCheckState(consumerContainerWatcherThread);
  }

  @Test
  void testConnectionAvailableAllConsumersActive() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    Condition noConnectionCondition = lock.newCondition();
    when(consumerContainerMock.isConnectionAvailable()).thenReturn(true);
    when(consumerContainerMock.ensureConsumersAreActive()).thenReturn(true);
    ConsumerContainerWatcherThread consumerContainerWatcherThread =
        new ConsumerContainerWatcherThread(consumerContainerMock, 100, lock, noConnectionCondition);
    consumerContainerWatcherThread.start();
    Thread.sleep(300);
    verify(consumerContainerMock).ensureConsumersAreActive();
    assertTrue(consumerContainerWatcherThread.isAlive());
    assertEquals(State.WAITING, consumerContainerWatcherThread.getState());

    killThreadAndCheckState(consumerContainerWatcherThread);
  }

  @Test
  void testConnectionAvailableNotAllConsumersActive() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    Condition noConnectionCondition = lock.newCondition();
    when(consumerContainerMock.isConnectionAvailable()).thenReturn(true);
    when(consumerContainerMock.ensureConsumersAreActive()).thenReturn(false);
    ConsumerContainerWatcherThread consumerContainerWatcherThread =
        new ConsumerContainerWatcherThread(consumerContainerMock, 100, lock, noConnectionCondition);
    consumerContainerWatcherThread.start();
    Thread.sleep(350);
    verify(consumerContainerMock, atLeast(3)).ensureConsumersAreActive();
    assertTrue(consumerContainerWatcherThread.isAlive());
    assertNotEquals(State.WAITING, consumerContainerWatcherThread.getState());
  }

  @Test
  void testNoConnectionEstablishedAfterSomeTime() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    Condition noConnectionCondition = lock.newCondition();
    when(consumerContainerMock.isConnectionAvailable()).thenReturn(false);
    when(consumerContainerMock.ensureConsumersAreActive()).thenReturn(true);
    ConsumerContainerWatcherThread consumerContainerWatcherThread =
        new ConsumerContainerWatcherThread(consumerContainerMock, 50, lock, noConnectionCondition);
    consumerContainerWatcherThread.start();
    Thread.sleep(300);
    assertTrue(consumerContainerWatcherThread.isAlive());
    assertEquals(State.WAITING, consumerContainerWatcherThread.getState());
    verify(consumerContainerMock, never()).ensureConsumersAreActive();

    when(consumerContainerMock.isConnectionAvailable()).thenReturn(true);
    try {
      lock.lock();
      noConnectionCondition.signalAll();
    } finally {
      lock.unlock();
    }
    Thread.sleep(100);

    verify(consumerContainerMock).ensureConsumersAreActive();
    assertEquals(State.WAITING, consumerContainerWatcherThread.getState());

    killThreadAndCheckState(consumerContainerWatcherThread);
  }

  @Test
  void testInterruptDuringRetrySleep() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    Condition noConnectionCondition = lock.newCondition();
    when(consumerContainerMock.isConnectionAvailable()).thenReturn(true);
    when(consumerContainerMock.ensureConsumersAreActive()).thenReturn(false);
    ConsumerContainerWatcherThread consumerContainerWatcherThread =
        new ConsumerContainerWatcherThread(consumerContainerMock, 1000000, lock,
            noConnectionCondition);
    consumerContainerWatcherThread.start();
    Thread.sleep(350);
    assertTrue(consumerContainerWatcherThread.isAlive());
    killThreadAndCheckState(consumerContainerWatcherThread);
  }

  private void killThreadAndCheckState(
      ConsumerContainerWatcherThread consumerContainerWatcherThread) throws InterruptedException {
    consumerContainerWatcherThread.interrupt();
    consumerContainerWatcherThread.join(100);
    assertFalse(consumerContainerWatcherThread.isAlive());
  }

}
