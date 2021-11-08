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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ThreadStopperTest {

  private volatile boolean threadShouldStop = false;
  private volatile boolean threadShouldStopCalled = false;

  @Test
  void testThreadWillNotJoinAndJoinGetsInterrupted() throws InterruptedException {
    ThreadStopper sut = new ThreadStopper();
    StoppableThread threadToStop = new StoppableThread(() -> {
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
