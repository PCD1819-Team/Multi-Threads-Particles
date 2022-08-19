package it.unibo.isi.pcd.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibo.isi.pcd.model.FunctionalModel;
import it.unibo.isi.pcd.model.Particle;
import it.unibo.isi.pcd.util.Barrier;
import it.unibo.isi.pcd.util.Event;
import it.unibo.isi.pcd.util.ObservableQueue;
import it.unibo.isi.pcd.util.Observer;
import it.unibo.isi.pcd.util.StartEvent;
import it.unibo.isi.pcd.util.SwitchBarrier;
import it.unibo.isi.pcd.util.UpdateEvent;
import it.unibo.isi.pcd.view.MainView;
import javafx.util.Pair;

public class MainController implements Observer {
  private static final int STANDARD_SIZE = 4;
  private final MainView gui;
  private final List<WorkerThread> workers;

  private FunctionalModel model;

  private Barrier barrier;
  private SwitchBarrier sbarrier;
  private final Map<Integer, Particle> map;

  private final ObservableQueue<Event> list;

  private int threadNumber;
  private int step;
  private int partCounter;
  private int stepCounter;

  public MainController(final MainView view) {
    view.addObserver(this);
    this.gui = view;
    this.partCounter = 0;
    this.workers = new ArrayList<>();
    this.threadNumber = MainController.STANDARD_SIZE;
    this.list = new ObservableQueue<>();
    this.step = 0;
    this.map = new HashMap<>();
    this.partCounter = 0;
  }

  @Override
  public void notifyEvent(final Event ev) {
    this.list.offer(ev);

  }

  private void setThreadPoolSize(final Integer size) {
    this.threadNumber = size;
  }

  public void execute() {
    do {
      final Event event = this.list.poll();
      switch (event.getType()) {

        case PAUSE:
          this.pause();
          break;

        case STOP:
          this.stop();
          break;

        case START:
          this.start(event);
          break;

        case SHUTDOWN:
          this.shutdown();
          break;

        case UPDATE:

          this.update(event);
          break;

        default:
          break;
      }
    } while (true);

  }

  private void pause() {
    if (!this.sbarrier.getWait()) {
      this.sbarrier.setWait();
      this.workers.forEach(worker -> {
        worker.togglePause();
      });
    } else {
      this.workers.forEach(worker -> {
        worker.togglePause();
      });
      this.sbarrier.reset();
    }
  }

  private void update(final Event ev) {
    final UpdateEvent event = (UpdateEvent) ev;
    this.map.putAll(event.getContent());
    if (this.partCounter++ >= (this.threadNumber - 1)) {
      this.partCounter = 0;
      this.model.updateParticles(this.map);
      this.gui.updatePosition(new ArrayList<>(this.model.getPositions()));
      if (++this.stepCounter >= this.step) {
        this.stop();
      }
      this.map.clear();
    }
  }

  private void stop() {
    if (this.sbarrier.getWait()) {
      this.sbarrier.reset();
    } else {
      this.sbarrier.reset();
      this.workers.forEach(worker -> {
        worker.togglePause();
      });
    }
    this.barrier.cleanALL();
    this.workers.forEach(worker -> worker.stopThread());
    this.workers.clear();
    this.gui.enableAll();
  }

  private void start(final Event ev) {

    final StartEvent event = (StartEvent) ev;
    if (event.getPositions() != null) {
      this.partCounter = 0;
      this.stepCounter = 0;
      this.map.clear();
      this.list.clear();
      this.model = new FunctionalModel(this.step, event.getPositions());
      this.step = event.getNumSteps();
      this.setThreadPoolSize(event.getNumThreads());

      final int particelleApprossimato = event.getPositions().size() / this.threadNumber;
      int particelleResto = event.getPositions().size() % this.threadNumber;
      final List<Pair<Integer, Integer>> marginiDiLavoro = new ArrayList<>();

      for (int margineSuperiore, margineInferiore = 0, i = 0; i < this.threadNumber; i++) {
        margineSuperiore = particelleApprossimato + margineInferiore;
        margineSuperiore += particelleResto-- > 0 ? 1 : 0;
        marginiDiLavoro.add(new Pair<>(margineInferiore, margineSuperiore - 1));
        margineInferiore = margineSuperiore;
      }

      this.barrier = new Barrier(this.threadNumber);
      this.sbarrier = new SwitchBarrier();

      marginiDiLavoro.forEach(coppia -> {
        final WorkerThread thread = new WorkerThread(this.model);
        thread.setMargini(coppia);
        thread.setBarrier(this.barrier);
        thread.setsBarrier(this.sbarrier);
        this.workers.add(thread);
        thread.addObserver(this);
        thread.setSteps(this.step);
      });
      for (final WorkerThread worker : this.workers) {
        worker.start();
      }
    }
  }

  private void shutdown() {
    System.exit(0);
  }

}
