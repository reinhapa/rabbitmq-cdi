package net.reini.rabbitmq.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ThreadStopper {
  private static final Logger LOGGER = LoggerFactory.getLogger(ThreadStopper.class);

  public void stopThread(Thread thread) {
    thread.interrupt();
    try {
      thread.join();
    } catch (InterruptedException e) {
      LOGGER.debug("thread was interrupted while joining", e);
      Thread.currentThread().interrupt();
    }
  }
}