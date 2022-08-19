package it.unibo.isi.pcd.util;

public class Barrier {

  private int dimensioneAttuale;
  private int dimensioneMassima;
  private boolean ready;
  private boolean clean;

  public Barrier(final int dim) {
    super();
    this.reset(dim);
    this.clean = false;
  }

  public synchronized void reset(final int dim) {
    this.dimensioneAttuale = 0;
    this.dimensioneMassima = dim;
    this.ready = true;
  }

  public synchronized void attendiInBarriera() {

    while (!this.ready && !this.clean) {
      try {
        this.wait();
      } catch (final InterruptedException e) {
      }
    }
    this.dimensioneAttuale++;

    while (((this.dimensioneAttuale < this.dimensioneMassima) && this.ready) && !this.clean) {

      try {
        this.wait();
      } catch (final InterruptedException e) {
        continue;
      }
    }

    this.dimensioneAttuale--;
    if (this.dimensioneAttuale <= 0) {
      this.reset(this.dimensioneMassima);
    } else {
      this.ready = false;
    }
    this.notifyAll();
  }

  public synchronized void cleanALL() {
    this.clean = true;
    this.notifyAll();
  }

}
