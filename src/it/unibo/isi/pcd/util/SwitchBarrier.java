package it.unibo.isi.pcd.util;

public class SwitchBarrier {

  private boolean wait;
  private int entered;

  public SwitchBarrier() {
    this.wait = false;
    this.entered = 0;
  }

  public synchronized void block() {
    this.entered++;
    while (this.wait) {
      try {
        this.wait();
      } catch (final InterruptedException e) {
      }
    }

    this.notifyAll();

    if (--this.entered <= 0) {
      this.reset();
    }
  }

  public synchronized void setWait() {
    this.wait = true;
  }

  public synchronized boolean getWait() {
    return this.wait;
  }

  public synchronized void reset() {
    this.wait = false;
    this.notifyAll();
  }

}
