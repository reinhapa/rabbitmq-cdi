package net.reini.rabbitmq.cdi;

final class ThreadStopper {
  
  public void stopThread(StopAbleThread thread) {
    thread.interrupt();
    thread.setStopped(true);
  }
}
