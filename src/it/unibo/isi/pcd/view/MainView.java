package it.unibo.isi.pcd.view;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import it.unibo.isi.pcd.util.EmptyEvent;
import it.unibo.isi.pcd.util.Event;
import it.unibo.isi.pcd.util.Observable;
import it.unibo.isi.pcd.util.Observer;
import it.unibo.isi.pcd.util.ShutdownEvent;
import it.unibo.isi.pcd.util.StartEvent;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class MainView extends Application implements Observable {

  private static final int NUMERO_CORE = Runtime.getRuntime().availableProcessors();
  private static final int DISTANZA_MINIMA_SFERE = 50;
  private static final int PADDING_BARRA_COMADI = 20;
  private static final int RANGE_COLORI = 256;
  private static final double MOLTIPLICATORE_SCALA_INIZIALE = 1.5;
  private static final int RAGGIO_SFERE = 50;
  private static final int DISTANZA_PASSI_TASTIERA = 100;
  private static final double FATTORE_ZOOM = 1.05;
  private static final int ROTAZIONE_ASSI_CAMERA = 20;
  private static final int RANGE_STANDARD_SPAWN_SFERE = 100;
  private static final int STEP_SPAWN_SFERE = 100;
  private static final int SOGLIA_ESPANSIONE_AREA_SPAWN_SFERE = 10;
  private static final int DISTANZA_MASSIMA_DI_VISIONE = 990000;
  private static final int DISTANZA_CAMERA_INIZIALE_DAL_CENTRO = 200;
  private static final int DIVISORE_DIMENSIONE_SCHERMO = 50;
  private static final double MOTLIPLICATORE_MOVIMENTO_CAMERA_CON_MOUSE = 0.25;
  private static final String GRIGIO = "#F4F4F4";
  private static final String BIANCO = "FFFFFF";
  private static final String ROSSO = "#F44242";
  private static final String TESTO_START = "Start";
  private static final String TESTO_STOP = "Stop";
  private static final String APP_TITLE = "Assignment";
  private static final String CSS_BARRA_COMANDI = "-fx-border-color: black;\n"
      + "-fx-border-width:  0 0 0 5;\n" + "-fx-border-style: solid;\n";
  private transient double mousePosX;
  private transient double mousePosY;

  private transient double mouseOldX;
  private transient double mouseOldY;

  private final transient Rotate rotateX;
  private final transient Rotate rotateY;

  private final List<Control> controls;
  private final Set<Observer> observers;
  private ObservableList<Node> listaDiNodi;
  private static MainView viewObject = null;

  private Button bottonePausa;
  private Button bottoneStartStop;

  public MainView() {
    super();
    this.observers = new HashSet<>();
    this.controls = new ArrayList<>();
    this.mouseOldX = 0;
    this.mouseOldY = 0;
    this.rotateX = new Rotate(-20, Rotate.X_AXIS);
    this.rotateY = new Rotate(-20, Rotate.Y_AXIS);
    MainView.viewObject = this;
  }

  @Override
  public void stop() throws Exception {
    this.observers.forEach(obs -> {
      obs.notifyEvent(new ShutdownEvent());
    });
    super.stop();
  }

  @Override
  public void start(final Stage primaryStage) throws Exception {

    final Button bottoneReset = new Button("Reset Camera");
    this.bottonePausa = new Button("Pausa");
    this.bottoneStartStop = new Button(MainView.TESTO_START);
    final Label numeroParticelleLabel = new Label("Numero di Particelle");
    final Label numeroPassiLabel = new Label("Numero di Passi");
    final TextField numeroParticelle = new TextField();
    final TextField numeroPassi = new TextField();
    final Slider numeroThread = new Slider(1, MainView.NUMERO_CORE, MainView.NUMERO_CORE / 2);
    final Label scrittaNumeroCores = new Label("Numero di core");

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final Camera camera3D = new PerspectiveCamera(true);
    final Group gruppo3D = new Group();
    final SubScene sottoScena3D = new SubScene(gruppo3D, 0, 0, true, SceneAntialiasing.DISABLED);
    final BorderPane contenitorePrincipale2D3D = new BorderPane();
    final Scene scenaPrincipale = new Scene(contenitorePrincipale2D3D);
    final VBox barraComandi = new VBox(
        screenSize.getHeight() / MainView.DIVISORE_DIMENSIONE_SCHERMO, scrittaNumeroCores,
        numeroThread, numeroParticelleLabel, numeroParticelle, numeroPassiLabel, numeroPassi,
        bottoneReset, this.bottonePausa, this.bottoneStartStop);

    this.controls.addAll(Arrays.asList(numeroParticelle, numeroPassi, numeroThread));

    this.listaDiNodi = gruppo3D.getChildren();

    numeroThread.setBlockIncrement(1);
    numeroThread.setMajorTickUnit(1);
    numeroThread.setMinorTickCount(0);
    numeroThread.setShowTickLabels(true);
    numeroThread.setSnapToTicks(true);

    barraComandi.setAlignment(Pos.TOP_CENTER);
    barraComandi.setBackground(new Background(
        new BackgroundFill(Color.web(MainView.GRIGIO), CornerRadii.EMPTY, Insets.EMPTY)));
    barraComandi.setPadding(new Insets(MainView.PADDING_BARRA_COMADI,
        screenSize.getWidth() / MainView.DIVISORE_DIMENSIONE_SCHERMO, MainView.PADDING_BARRA_COMADI,
        screenSize.getWidth() / MainView.DIVISORE_DIMENSIONE_SCHERMO));
    barraComandi.setStyle(MainView.CSS_BARRA_COMANDI);

    camera3D.getTransforms().addAll(this.rotateX, this.rotateY,
        new Translate(0, 0, -MainView.DISTANZA_CAMERA_INIZIALE_DAL_CENTRO));
    camera3D.setNearClip(1);
    camera3D.setFarClip(MainView.DISTANZA_MASSIMA_DI_VISIONE);

    contenitorePrincipale2D3D.setCenter(sottoScena3D);
    contenitorePrincipale2D3D.setRight(barraComandi);

    this.bottonePausa.setDisable(true);

    sottoScena3D.setFill(Color.AQUAMARINE);
    sottoScena3D.setCamera(camera3D);
    sottoScena3D.heightProperty().bind(contenitorePrincipale2D3D.heightProperty());
    sottoScena3D.widthProperty().bind(contenitorePrincipale2D3D.widthProperty());
    sottoScena3D.setManaged(false);

    primaryStage.setWidth(screenSize.getWidth() / MainView.MOLTIPLICATORE_SCALA_INIZIALE);
    primaryStage.setHeight(screenSize.getHeight() / MainView.MOLTIPLICATORE_SCALA_INIZIALE);
    primaryStage.setTitle(MainView.APP_TITLE);
    primaryStage.setScene(scenaPrincipale);
    primaryStage.show();

    bottoneReset.setOnAction(e -> {
      this.rotateX.setAngle(-MainView.ROTAZIONE_ASSI_CAMERA);
      this.rotateY.setAngle(-MainView.ROTAZIONE_ASSI_CAMERA);
    });

    this.bottoneStartStop.setOnAction(ev -> {
      if (this.bottoneStartStop.getText().equals(MainView.TESTO_START)) {
        final int numeroParticelleInserito;
        try {
          numeroParticelleInserito = Integer.parseInt(numeroParticelle.getText());
          if (numeroParticelleInserito <= 0) {
            this.insertError(numeroParticelle);
            return;
          }
          this.resetError(numeroParticelle);
        } catch (final NumberFormatException exept) {
          this.insertError(numeroParticelle);
          return;
        }

        final int numeroPassiInserito;
        try {
          numeroPassiInserito = Integer.parseInt(numeroPassi.getText());
          if (numeroPassiInserito <= 0) {
            this.insertError(numeroPassi);
            return;
          }
          this.resetError(numeroPassi);
          this.disableAll();
          this.bottoneStartStop.setText(MainView.TESTO_STOP);
          this.bottonePausa.setDisable(false);
        } catch (final NumberFormatException exept) {
          this.insertError(numeroPassi);
          return;
        }

        this.createSphere(numeroParticelleInserito, this.listaDiNodi);
        final ArrayList<Point3D> dataList = (ArrayList<Point3D>) this.listaDiNodi.stream()
            .map(node -> {
              return new Point3D(node.getTranslateX(), node.getTranslateY(), node.getTranslateZ());
            }).collect(Collectors.toList());

        for (final Observer obs : this.observers) {
          obs.notifyEvent(
              new StartEvent(numeroPassiInserito, (int) numeroThread.getValue(), dataList));
        }

      } else {
        this.enableAll();
        this.bottoneStartStop.setText(MainView.TESTO_START);
        this.bottonePausa.setDisable(false);

        for (final Observer obs : this.observers) {
          obs.notifyEvent(new EmptyEvent(Event.EventType.STOP));
        }

      }

    });

    sottoScena3D.setOnScroll((final ScrollEvent ev) -> {
      double zoomFactor = MainView.FATTORE_ZOOM;
      final double deltaY = ev.getDeltaY();
      if (deltaY < 0) {
        zoomFactor = 2.0 - zoomFactor;
      }
      gruppo3D.setScaleX(gruppo3D.getScaleX() * zoomFactor);
      gruppo3D.setScaleY(gruppo3D.getScaleY() * zoomFactor);
      gruppo3D.setScaleZ(gruppo3D.getScaleZ() * zoomFactor);
      ev.consume();
    });

    sottoScena3D.setOnMousePressed((final MouseEvent me) -> {
      try {
        this.mouseOldX = me.getSceneX();
        this.mouseOldY = me.getSceneY();
        me.consume();
      } catch (final Exception e) {
        e.printStackTrace();
      }

    });

    this.bottonePausa.setOnAction(event -> {
      if (this.bottonePausa.getText().equals("Pausa")) {
        this.bottonePausa.setText("Riprendi");
      } else {
        this.bottonePausa.setText("Pausa");
      }
      this.observers.forEach(obs -> {
        obs.notifyEvent(new EmptyEvent(Event.EventType.PAUSE));
      });
    });

    sottoScena3D.setOnMouseDragged((final MouseEvent me) -> {
      try {
        this.mousePosX = me.getSceneX();
        this.mousePosY = me.getSceneY();
        this.rotateX.setAngle(this.rotateX.getAngle() - ((this.mousePosY - this.mouseOldY)
            * MainView.MOTLIPLICATORE_MOVIMENTO_CAMERA_CON_MOUSE));
        this.rotateY.setAngle(this.rotateY.getAngle() + ((this.mousePosX - this.mouseOldX)
            * MainView.MOTLIPLICATORE_MOVIMENTO_CAMERA_CON_MOUSE));
        this.mouseOldX = this.mousePosX;
        this.mouseOldY = this.mousePosY;
        me.consume();
      } catch (final Exception e) {
        e.printStackTrace();
      }

    });

    primaryStage.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {

      switch (ev.getCode()) {
        case W:
          camera3D.translateZProperty()
              .set(camera3D.getTranslateZ() + MainView.DISTANZA_PASSI_TASTIERA);
          break;
        case S:
          camera3D.translateZProperty()
              .set(camera3D.getTranslateZ() - MainView.DISTANZA_PASSI_TASTIERA);

          break;
        case A:
          camera3D.translateXProperty()
              .set(camera3D.getTranslateX() - MainView.DISTANZA_PASSI_TASTIERA);
          break;
        case D:
          camera3D.translateXProperty()
              .set(camera3D.getTranslateX() + MainView.DISTANZA_PASSI_TASTIERA);
          break;
        case Q:
          camera3D.translateYProperty()
              .set(camera3D.getTranslateY() - MainView.DISTANZA_PASSI_TASTIERA);
          break;
        case E:
          camera3D.translateYProperty()
              .set(camera3D.getTranslateY() + MainView.DISTANZA_PASSI_TASTIERA);
          break;
        default:
          break;
      }
      ev.consume();
    });
  }

  private void internalUpdatePosition(final List<Point3D> newPos) {
    final AnimationTimer animator = new AnimationTimer() {

      @Override
      public void handle(final long now) {
        for (int i = 0; i < newPos.size(); i++) {
          MainView.this.listaDiNodi.get(i).setTranslateX(newPos.get(i).getX());
          MainView.this.listaDiNodi.get(i).setTranslateY(newPos.get(i).getY());
          MainView.this.listaDiNodi.get(i).setTranslateZ(newPos.get(i).getZ());
        }

        this.stop();
      }

    };
    animator.start();

  }

  public void updatePosition(final List<Point3D> newPos) {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> this.internalUpdatePosition(newPos));
    } else {
      this.internalUpdatePosition(newPos);
    }
  }

  private void createSphere(final int numeroSfere, final ObservableList<Node> listaDiNodi) {
    listaDiNodi.clear();

    long rengeMassimoSpawnSfere = MainView.RANGE_STANDARD_SPAWN_SFERE;
    for (int i = 0; i < numeroSfere; i++) {
      int count = 0;
      Sphere sferaRandom;
      sferaRandom = new Sphere(MainView.RAGGIO_SFERE, 1);

      final int canaleR = ThreadLocalRandom.current().nextInt(0, MainView.RANGE_COLORI);
      final int canaleV = ThreadLocalRandom.current().nextInt(0, MainView.RANGE_COLORI);
      final int canaleB = ThreadLocalRandom.current().nextInt(0, MainView.RANGE_COLORI);

      final PhongMaterial texture = new PhongMaterial(Color.rgb(canaleR, canaleV, canaleB));
      sferaRandom.setMaterial(texture);

      do {
        sferaRandom
            .setTranslateX(ThreadLocalRandom.current().nextDouble(0, rengeMassimoSpawnSfere));
        sferaRandom
            .setTranslateY(ThreadLocalRandom.current().nextDouble(0, rengeMassimoSpawnSfere));
        sferaRandom
            .setTranslateZ(ThreadLocalRandom.current().nextDouble(0, rengeMassimoSpawnSfere));

        if (count++ >= MainView.SOGLIA_ESPANSIONE_AREA_SPAWN_SFERE) {
          rengeMassimoSpawnSfere += MainView.STEP_SPAWN_SFERE;
          count = 0;
        }

      } while (listaDiNodi.stream().anyMatch(item -> {
        if (((Math.abs(
            (sferaRandom.getTranslateX() - item.getTranslateX())) > ((MainView.RAGGIO_SFERE * 2)
                + MainView.DISTANZA_MINIMA_SFERE))
            || (Math.abs(
                (sferaRandom.getTranslateY() - item.getTranslateY())) > ((MainView.RAGGIO_SFERE * 2)
                    + MainView.DISTANZA_MINIMA_SFERE))
            || (Math.abs(
                (sferaRandom.getTranslateZ() - item.getTranslateZ())) > ((MainView.RAGGIO_SFERE * 2)
                    + MainView.DISTANZA_MINIMA_SFERE)))) {
          return false;
        } else {
          return true;
        }
      }));
      listaDiNodi.add(sferaRandom);
    }
  }

  private void insertError(final TextField field) {
    field.setBackground(new Background(
        new BackgroundFill(Color.web(MainView.ROSSO), CornerRadii.EMPTY, Insets.EMPTY)));
    field.setText("Inserici un numero valido");
  }

  private void resetError(final TextField field) {
    field.setBackground(new Background(
        new BackgroundFill(Color.web(MainView.BIANCO), CornerRadii.EMPTY, Insets.EMPTY)));
  }

  @Override
  public void addObserver(final Observer obs) {
    this.observers.add(obs);
  }

  @Override
  public void removeObserver(final Observer obs) {
    this.observers.remove(obs);

  }

  public void disableAll() {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> this.internalDisableAll());
    } else {
      this.internalDisableAll();
    }

  }

  private void internalDisableAll() {
    this.bottonePausa.setDisable(false);
    this.controls.forEach(control -> {
      control.setDisable(true);
    });
  }

  private void internalEnableAll() {
    this.bottoneStartStop.setText(MainView.TESTO_START);
    this.bottonePausa.setText("Pausa");
    this.bottonePausa.setDisable(true);
    this.controls.forEach(control -> {
      control.setDisable(false);
    });
  }

  public void enableAll() {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> this.internalEnableAll());
    } else {
      this.internalEnableAll();
    }
  }

  public static synchronized MainView getInstance(final String[] args) {
    if ((MainView.viewObject == null)) {
      (new Thread() {
        @Override
        public void run() {
          Application.launch(MainView.class, args);
        }
      }).start();

      while ((MainView.viewObject == null)) {
        try {
          Thread.sleep(100);
        } catch (final InterruptedException e) {
        }
      }

    }
    return MainView.viewObject;
  }
}
