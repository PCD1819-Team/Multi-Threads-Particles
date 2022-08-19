package it.unibo.isi.pcd.util;

public interface Event {

  enum EventType {
    STOP, START, PAUSE, SHUTDOWN, UPDATE;
  }

  EventType getType();

}
