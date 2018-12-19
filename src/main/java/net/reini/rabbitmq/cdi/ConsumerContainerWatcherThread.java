package net.reini.rabbitmq.cdi;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerContainerWatcherThread extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainerWatcherThread.class);
  private final ConsumerContainer consumerContainer;
  private long retryTime;
  private final ReentrantLock lock;
  private final Condition noConnectionCondition;
  private volatile long attempt;
  private final ThreadStopper threadStopper;

  public ConsumerContainerWatcherThread(ConsumerContainer consumerContainer, long retryTime, ReentrantLock lock, Condition noConnectionCondition) {
    this.threadStopper=new ThreadStopper();
    this.consumerContainer = consumerContainer;
    this.retryTime = retryTime;
    this.lock = lock;
    this.noConnectionCondition = noConnectionCondition;
    this.setDaemon(true);
    this.setName("consumer watcher thread");
  }

  @Override
  public void run() {
    while (Thread.currentThread().isInterrupted() == false) {
      boolean allConsumersActive = false;
      try {
        lock.lock();
        if (consumerContainer.isConnectionAvailable()) {
          attempt++;
          allConsumersActive = consumerContainer.ensureConsumersAreActive();
        }
        if (allConsumersActive || consumerContainer.isConnectionAvailable() == false) {
          attempt = 0;
          this.noConnectionCondition.await();
        }
      } catch (InterruptedException e) {
        LOGGER.info("interrupted while waiting for notification");
        Thread.currentThread().interrupt();
      } finally {
        lock.unlock();
      }
      if (allConsumersActive == false && attempt > 0) {
        LOGGER.warn("could not activate all consumer. Retry to activate failed consumers");
        try {
          Thread.sleep(retryTime);
        } catch (InterruptedException e) {
          LOGGER.info("interrupted while sleeping", e);
          Thread.currentThread().interrupt();
        }
      }

    }

  }

  public void stopThread() {
    threadStopper.stopThread(this);
  }
}