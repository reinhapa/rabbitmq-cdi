package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

@ExtendWith(MockitoExtension.class)
class ConnectionManagerWatcherThreadTest {



  @Test
  void testEstablishConnectionNotPossible() throws InterruptedException {
    ReentrantLock lock=new ReentrantLock();
    Condition condition=lock.newCondition();
    ConnectionManager connectionManagerMock = mock(ConnectionManager.class);
    when(connectionManagerMock.tryToEstablishConnection()).thenReturn(false);
    when(connectionManagerMock.getState()).thenReturn(ConnectionState.NEVER_CONNECTED);
    ConnectionManagerWatcherThread sut = new ConnectionManagerWatcherThread(lock,condition,connectionManagerMock, 50);
    sut.start();
    Thread.sleep(300);
    assertTrue(sut.isAlive());
    verify(connectionManagerMock, atLeast(2)).tryToEstablishConnection();
    assertEquals(State.TIMED_WAITING, sut.getState());
    killThreadAndVerifyState(sut);
  }

  @Test
  void testEstablishConnectionSuccessfull() throws InterruptedException {
    ReentrantLock lock=new ReentrantLock();
    Condition condition=lock.newCondition();
    ConnectionManager connectionManagerMock = mock(ConnectionManager.class);
    when(connectionManagerMock.tryToEstablishConnection()).thenReturn(true);
    when(connectionManagerMock.getState()).thenReturn(ConnectionState.NEVER_CONNECTED);
    ConnectionManagerWatcherThread sut = new ConnectionManagerWatcherThread(lock,condition, connectionManagerMock, 50);
    sut.start();
    Thread.sleep(200);
    assertTrue(sut.isAlive());
    assertEquals(sut.isAlive(),sut.isRunning());
    verify(connectionManagerMock).tryToEstablishConnection();
    assertEquals(State.WAITING, sut.getState());
    killThreadAndVerifyState(sut);
    assertEquals(sut.isAlive(),sut.isRunning());
  }


  @Test
  void testEstablishConnectionSuccessfullButLostAfterSomeTime() throws InterruptedException {
    ReentrantLock lock=new ReentrantLock();
    Condition condition=lock.newCondition();
    ConnectionManager connectionManagerMock = mock(ConnectionManager.class);
    when(connectionManagerMock.tryToEstablishConnection()).thenReturn(true);
    when(connectionManagerMock.getState()).thenReturn(ConnectionState.NEVER_CONNECTED);
    ConnectionManagerWatcherThread sut = new ConnectionManagerWatcherThread(lock,condition, connectionManagerMock, 50);
    sut.start();
    Thread.sleep(200);
    assertTrue(sut.isAlive());
    verify(connectionManagerMock).tryToEstablishConnection();
    assertEquals(State.WAITING, sut.getState());
    try
    {
      lock.lock();
      condition.signalAll();
    }
    finally {
      lock.unlock();
    }
    Thread.sleep(200);
    verify(connectionManagerMock, times(2)).tryToEstablishConnection();
    assertEquals(State.WAITING, sut.getState());
    killThreadAndVerifyState(sut);
  }
  
  private void killThreadAndVerifyState(ConnectionManagerWatcherThread sut) throws InterruptedException {
    sut.stopThread();
    assertFalse(sut.isAlive());
  }
}