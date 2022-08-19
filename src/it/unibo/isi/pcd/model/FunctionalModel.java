package it.unibo.isi.pcd.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.sun.javafx.geom.Vec3d;

import javafx.geometry.Point3D;

public class FunctionalModel {

  private final StatusModel statusModel;

  public FunctionalModel(final int numSteps, final List<Point3D> positions) {

    final List<Particle> particles = new ArrayList<>();
    for (int i = 0; i < positions.size(); i++) {

      particles.add(new Particle(ThreadLocalRandom.current().nextDouble(10, 100),
          ThreadLocalRandom.current().nextDouble(0.05, 0.5), positions.get(i), new Vec3d(0, 0, 0),
          new Vec3d(0, 0, 0)));
    }
    this.statusModel = new StatusModel(particles);
    this.statusModel.setNumSteps(numSteps);

  }

  public List<Point3D> getPositions() {
    return this.statusModel.getParticles().stream().map(p -> p.getPosition())
        .collect(Collectors.toList());
  }

  public Map<Integer, Particle> computeParticles(final int lowerBound, final int upperBound) {
    final Map<Integer, Particle> computedParticles = new HashMap<>();
    for (int i = lowerBound; i <= upperBound; i++) {
      computedParticles.put(i, this.computeParticle(i));

    }
    return computedParticles;
  }

  private Particle computeParticle(final int i) {
    final Particle p = new Particle(this.statusModel.getParticles().get(i));
    final Vec3d newForce = this.computeNewForce(i, this.statusModel.getParticles());
    p.updateForce(newForce);
    p.updatePosition(this.computeNewPosition(p));
    p.updateSpeed(this.computeNewSpeed(p, newForce));
    return p;
  }

  public void updateParticles(final Map<Integer, Particle> allComputedParticles) {
    if (allComputedParticles.size() == this.statusModel.getParticles().size()) {
      for (int i = 0; i < this.statusModel.getParticles().size(); i++) {
        this.statusModel.updateParticle(allComputedParticles.get(i), i);
      }
    } else {
      throw new IllegalStateException("All particles must have been computed");
    }

  }

  private Point3D computeNewPosition(final Particle p) {
    final Vec3d currentSpeed = p.getCurrentSpeed();
    return new Point3D(p.getPosition().getX() + (currentSpeed.x * StatusModel.deltaT),
        p.getPosition().getY() + (currentSpeed.y * StatusModel.deltaT),
        p.getPosition().getZ() + (currentSpeed.z * StatusModel.deltaT));
  }

  private Vec3d computeNewSpeed(final Particle p, final Vec3d newForce) {

    return new Vec3d(p.getCurrentSpeed().x + ((StatusModel.deltaT * newForce.x) / p.getmConst()),
        p.getCurrentSpeed().y + ((StatusModel.deltaT * newForce.y) / p.getmConst()),
        p.getCurrentSpeed().z + ((StatusModel.deltaT * newForce.z) / p.getmConst()));
  }

  private Vec3d computeNewForce(final int partIndex, final List<Particle> parts) {
    final Particle partI = parts.get(partIndex);
    final Vec3d force = parts.get(partIndex).getCurrentForce();
    double dist = 0;
    double dx = 0;
    double dy = 0;
    double dz = 0;
    double commonFormulaPart = 0;
    for (int j = 0; j < parts.size(); j++) {
      if (j != partIndex) {
        final Particle partJ = parts.get(j);
        dx = partI.getPosition().getX() - partJ.getPosition().getX();
        dy = partI.getPosition().getY() - partJ.getPosition().getY();
        dz = partI.getPosition().getZ() - partJ.getPosition().getZ();
        dist = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2) + Math.pow(dz, 2));

        commonFormulaPart = (StatusModel.k * partI.getAlfaConst() * partJ.getAlfaConst())
            / Math.pow(dist, 3);
        force.x += (commonFormulaPart * dx);
        force.y += (commonFormulaPart * dy);
        force.z += (commonFormulaPart * dz);
      }
    }

    force.x = (force.x - (StatusModel.kAttr * partI.getCurrentSpeed().x)) <= 0 ? 0
        : force.x + (-StatusModel.kAttr * partI.getCurrentSpeed().x);
    force.y = (force.y - (StatusModel.kAttr * partI.getCurrentSpeed().y)) <= 0 ? 0
        : force.y + (-StatusModel.kAttr * partI.getCurrentSpeed().y);
    force.z = (force.z - (StatusModel.kAttr * partI.getCurrentSpeed().z)) <= 0 ? 0
        : force.z + (-StatusModel.kAttr * partI.getCurrentSpeed().z);

    return force;

  }
}