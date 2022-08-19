package it.unibo.isi.pcd.util;

public class ShutdownEvent implements Event {

  @Override
  public EventType getType() {
    return EventType.SHUTDOWN;
  }

}
