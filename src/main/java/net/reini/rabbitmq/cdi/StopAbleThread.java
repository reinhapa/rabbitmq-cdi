package net.reini.rabbitmq.cdi;

abstract class StopAbleThread extends Thread {
  protected boolean stopped=false;

  public StopAbleThread() {
  }

  public StopAbleThread(Runnable target) {
    super(target);
  }

  public void setStopped(boolean stopped) {
    this.stopped = stopped;
  }
}