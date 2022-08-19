package it.unibo.isi.pcd.util;

import java.util.Map;

import it.unibo.isi.pcd.model.Particle;

public class UpdateEvent implements Event {

  private final EventType type;
  private final Map<Integer, Particle> map;

  /*
   * .
   */
  public UpdateEvent(final Map<Integer, Particle> mp) {
    this.type = EventType.UPDATE;
    this.map = mp;
  }

  @Override
  public EventType getType() {

    return this.type;
  }

  public Map<Integer, Particle> getContent() {
    return this.map;
  }

}
