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
