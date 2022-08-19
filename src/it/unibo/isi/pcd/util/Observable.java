package it.unibo.isi.pcd.util;

public interface Observable {

  void addObserver(Observer obs);

  void removeObserver(Observer obs);

}
