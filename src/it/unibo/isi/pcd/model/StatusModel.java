package it.unibo.isi.pcd.model;

import java.util.List;

public final class StatusModel {

  private transient int numSteps;
  public static double deltaT = 0.2;
  public static double k = 50;
  public static double kAttr = 0.05;
  private List<Particle> particles;

  public StatusModel(final List<Particle> particles) {
    this.particles = particles;
  }

  public List<Particle> getParticles() {
    return this.particles;
  }

  public void updateParticles(final List<Particle> particles) {
    this.particles = particles;
  }

  public void updateParticle(final Particle p, final int i) {
    this.particles.set(i, p);
  }

  public void setNumSteps(final int numSteps) {
    this.numSteps = numSteps;
  }

  public int getNumSteps() {
    return this.numSteps;
  }

}
