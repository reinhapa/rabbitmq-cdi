package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ThreadStopperTest {

  private volatile boolean threadShouldStop = false;
  private volatile boolean threadShouldStopCalled = false;

  @Test
  void testThreadWillNotJoinAndJoinGetsInterrupted() throws InterruptedException {
    ThreadStopper sut = new ThreadStopper();
    Thread threadToStop = new Thread(() -> {
      while (threadShouldStop == false) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          //ignore
        }
      }
    });

    threadToStop.start();

    Thread stopperThread = new Thread(() -> {
      sut.stopThread(threadToStop);
      threadShouldStopCalled=true;
    });
    stopperThread.start();
    Thread.sleep(100);
    assertTrue(stopperThread.isAlive());
    stopperThread.interrupt();
    stopperThread.join(500);
    assertFalse(stopperThread.isAlive());
    assertTrue(threadShouldStopCalled);
    threadShouldStop=true;
    threadToStop.join(500);
    assertFalse(threadToStop.isAlive());
    

  }


}