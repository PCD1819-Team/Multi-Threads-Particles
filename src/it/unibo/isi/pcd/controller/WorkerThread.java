package it.unibo.isi.pcd.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import it.unibo.isi.pcd.model.FunctionalModel;
import it.unibo.isi.pcd.model.Particle;
import it.unibo.isi.pcd.util.Barrier;
import it.unibo.isi.pcd.util.Observable;
import it.unibo.isi.pcd.util.Observer;
import it.unibo.isi.pcd.util.SwitchBarrier;
import it.unibo.isi.pcd.util.UpdateEvent;
import javafx.util.Pair;

public class WorkerThread extends Thread implements Observable {

  private final FunctionalModel model;
  private int margineBasso;
  private int margineAlto;
  private Optional<Barrier> barrier;
  private Map<Integer, Particle> map;
  private final Set<Observer> obs;
  private volatile boolean stopFlag;
  private volatile boolean pauseFlag;

  private volatile int step;
  private Optional<SwitchBarrier> sbarrier;

  WorkerThread(final FunctionalModel mod) {
    this.model = mod;
    this.margineAlto = 0;
    this.stopFlag = true;
    this.pauseFlag = true;
    this.obs = new HashSet<>();
    this.margineBasso = 0;
    this.barrier = Optional.empty();
  }

  public void setMargini(final Pair<Integer, Integer> margini) {
    if (margini.getKey() <= margini.getValue()) {
      this.margineBasso = margini.getKey();
      this.margineAlto = margini.getValue();
    } else {
      throw new IllegalArgumentException();
    }

  }

  public void stopThread() {
    this.stopFlag = false;
  }

  public void togglePause() {
    this.pauseFlag = this.pauseFlag == true ? false : true;
  }

  public void setBarrier(final Barrier bar) {
    if (bar == null) {
      this.barrier = Optional.empty();

    } else {
      this.barrier = Optional.of(bar);
    }
  }

  public void setsBarrier(final SwitchBarrier sbar) {
    if (sbar == null) {
      this.sbarrier = Optional.empty();

    } else {
      this.sbarrier = Optional.of(sbar);
    }
  }

  @Override
  public void run() {
    int internalStep = 0;
    while (this.stopFlag) {
      while (this.pauseFlag) {
        this.map = this.model.computeParticles(this.margineBasso, this.margineAlto);
        this.barrier.get().attendiInBarriera();
        this.obs.forEach(observer -> {
          observer.notifyEvent(new UpdateEvent(new HashMap<>(this.map)));
        });
        if (++internalStep >= this.step) {
          break;
        }
      }
      this.sbarrier.get().block();
    }

  }

  public void setSteps(final int step) {
    this.step = step;
  }

  @Override
  public void addObserver(final Observer obs) {
    this.obs.add(obs);
  }

  @Override
  public void removeObserver(final Observer obs) {
    this.obs.remove(obs);
  }

}
