package it.unibo.isi.pcd.model;

import com.sun.javafx.geom.Vec3d;

import javafx.geometry.Point3D;

public class Particle {
  private final double alfaConst;
  private final double mConst;
  private Point3D currentPosition;
  private Vec3d currentSpeed;
  private Vec3d currentForce;

  public Particle(final double alfaConst, final double mConst, final Point3D startingPosition,
      final Vec3d startingSpeed, final Vec3d startingForce) {
    this.alfaConst = alfaConst;
    this.mConst = mConst;
    this.currentPosition = startingPosition;
    this.currentForce = startingForce;
    this.currentSpeed = startingSpeed;
  }

  public Particle(final Particle p) {
    this.alfaConst = p.alfaConst;
    this.mConst = p.mConst;
    this.currentForce = p.currentForce;
    this.currentPosition = p.currentPosition;
    this.currentSpeed = p.currentSpeed;
  }

  public double getAlfaConst() {
    return this.alfaConst;
  }

  public double getmConst() {
    return this.mConst;
  }

  public Point3D getPosition() {
    return this.currentPosition;
  }

  public Vec3d getCurrentSpeed() {
    return this.currentSpeed;
  }

  public Vec3d getCurrentForce() {
    return this.currentForce;
  }

  public void updatePosition(final Point3D newPos) {
    this.currentPosition = newPos;
  }

  public void updateSpeed(final Vec3d newSpeed) {
    this.currentSpeed = newSpeed;
  }

  public void updateForce(final Vec3d newForce) {
    this.currentForce = newForce;
  }

}
