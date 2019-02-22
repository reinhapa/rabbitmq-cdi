package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ThreadStopperTest {

  private volatile boolean threadShouldStop = false;
  private volatile boolean threadShouldStopCalled = false;

  @Test
  void testThreadWillNotJoinAndJoinGetsInterrupted() throws InterruptedException {
    ThreadStopper sut = new ThreadStopper();
    StopAbleThread threadToStop = new StopAbleThread(() -> {
      while (threadShouldStop == false) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }) {
    };

    threadToStop.start();

    Thread stopperThread = new Thread(() -> {
      sut.stopThread(threadToStop);
      threadShouldStopCalled = true;
    });
    stopperThread.start();
    waitForAliveWithTimeout(50,stopperThread);
    stopperThread.interrupt();
    stopperThread.join(500);
    assertFalse(stopperThread.isAlive());
    assertTrue(threadShouldStopCalled);
    threadShouldStop = true;
    threadToStop.join(500);
    assertFalse(threadToStop.isAlive());


  }

  private void waitForAliveWithTimeout(int retries, Thread stopperThread) throws InterruptedException {
    int count=1;
    while(stopperThread.isAlive()==false || count >=retries);
    {
      Thread.sleep(100);
    }
  }


}
