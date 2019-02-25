package net.reini.rabbitmq.cdi;

final class ThreadStopper {
  
  public void stopThread(StoppableThread thread) {
    thread.interrupt();
    thread.setStopped(true);
  }
}
