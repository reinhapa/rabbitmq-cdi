package net.reini.rabbitmq.cdi;

abstract class StoppableThread extends Thread {
  protected volatile boolean stopped=false;

  public StoppableThread() {
  }

  public StoppableThread(Runnable target) {
    super(target);
  }

  public void setStopped(boolean stopped) {
    this.stopped = stopped;
  }
}