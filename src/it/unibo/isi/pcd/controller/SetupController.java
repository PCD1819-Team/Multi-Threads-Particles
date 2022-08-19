package it.unibo.isi.pcd.controller;

import it.unibo.isi.pcd.view.MainView;

public class SetupController {

  public static void main(final String[] args) {
    final MainController mainController = new MainController(MainView.getInstance(args));
    mainController.execute();

  }

}
