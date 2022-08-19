package it.unibo.isi.pcd.util;

import java.util.List;

import javafx.geometry.Point3D;

public class StartEvent implements Event {
  private final int numSteps;
  private final int numThreads;
  private final List<Point3D> positions;

  private final EventType type;

  /*
   * .
   */

  public StartEvent(final int numSteps, final int numThreads, final List<Point3D> positions) {
    this.type = EventType.START;
    this.numThreads = numThreads;
    this.numSteps = numSteps;
    this.positions = positions;
  }

  @Override
  public EventType getType() {

    return this.type;
  }

  public int getNumSteps() {
    return this.numSteps;
  }

  public int getNumThreads() {
    return this.numThreads;
  }

  public List<Point3D> getPositions() {
    return this.positions;
  }
}
