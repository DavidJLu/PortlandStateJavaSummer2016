package edu.pdx.cs410J.whitlock.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

/**
 * A basic GWT class that makes sure that we can send an appointment book back from the server
 */
public class GameOfLifeGwt implements EntryPoint {
  private final Alerter alerter;

  @VisibleForTesting
  Button startGame;

  Button nextGeneration;
  ToggleButton automaticallyAdvance;

  TextBox numberOfRows;
  TextBox numberOfColumns;
  TextArea grid;
  private GenerationServiceAsync generationService;

  Timer timer;

  public GameOfLifeGwt() {
    this(new Alerter() {
      @Override
      public void alert(String message) {
        Window.alert(message);
      }
    });
  }

  @VisibleForTesting
  GameOfLifeGwt(Alerter alerter) {
    this.alerter = alerter;

    addWidgets();
  }

  private void addWidgets() {
    generationService = GWT.create(GenerationService.class);

    startGame = new Button("Start new Game of Life");
    startGame.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        createGrid();
        startNewGameOfLife();
      }
    });

    numberOfColumns = new TextBox();
    showOnly3Charaters(numberOfColumns);

    numberOfRows = new TextBox();
    showOnly3Charaters(numberOfRows);

    this.grid = new TextArea();

    this.nextGeneration = new Button("Compute Next Generation");
    this.nextGeneration.setEnabled(false);
    this.nextGeneration.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        displayNextGeneration();
      }
    });

    this.automaticallyAdvance = new ToggleButton("Start", "Stop");
    this.automaticallyAdvance.setEnabled(false);
    this.automaticallyAdvance.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        if (event.getValue()) {
          startAutomaticAdvancement();

        } else {
          stopAutomaticAdvancement();
        }
      }
    });

    this.timer = new Timer() {
      @Override
      public void run() {
        displayNextGeneration();
      }
    };
  }

  private void stopAutomaticAdvancement() {
    this.timer.cancel();
  }

  private void startAutomaticAdvancement() {
    this.timer.scheduleRepeating(500);
  }

  private void displayNextGeneration() {
    this.generationService.getNextGeneration(new AsyncCallback<Generation>() {
      @Override
      public void onFailure(Throwable throwable) {
        alert(throwable);
      }

      @Override
      public void onSuccess(Generation generation) {
        displayGeneration(generation);
      }
    });

  }

  private void createGrid() {
    int numberOfRows = getNumberOfRows();
    int numberOfColumns = getNumberOfColumns();
    this.grid.setCharacterWidth(numberOfColumns);
    this.grid.setVisibleLines(numberOfRows);
  }

  private int getNumberOfColumns() {
    return parseInteger(this.numberOfColumns.getText());
  }

  private int getNumberOfRows() {
    return parseInteger(this.numberOfRows.getText());
  }

  private int parseInteger(String text) {
    return Integer.parseInt(text);
  }

  private static void showOnly3Charaters(TextBox numberOfColumns) {
    numberOfColumns.setMaxLength(3);
    numberOfColumns.setVisibleLength(3);
  }

  private void startNewGameOfLife() {
    int numberOfRows = getNumberOfRows();
    int numberOfColumns = getNumberOfColumns();

    generationService.createNewGameOfLife(numberOfRows, numberOfColumns, new AsyncCallback<Generation>() {

      @Override
      public void onFailure(Throwable ex) {
        alert(ex);
      }

      @Override
      public void onSuccess(Generation generation) {
        displayGeneration(generation);
      }

    });
  }

  private void alert(Throwable ex) {
    alerter.alert(ex.toString());
  }

  private void displayGeneration(Generation generation) {
    StringBuilder sb = new StringBuilder();

    for (int row = 0; row < generation.getNumberOfRows(); row++) {
      sb.append(generation.getRow(row));
      sb.append("\n");
    }

    this.grid.setText(sb.toString());

    this.nextGeneration.setEnabled(true);
    this.automaticallyAdvance.setEnabled(true);
  }

  @Override
  public void onModuleLoad() {
    RootPanel rootPanel = RootPanel.get();

    HorizontalPanel input = new HorizontalPanel();
    input.add(new Label("Rows"));
    input.add(numberOfRows);
    input.add(new Label("by Columns"));
    input.add(numberOfColumns);
    input.add(startGame);
    input.add(nextGeneration);
    input.add(automaticallyAdvance);

    DockPanel panel = new DockPanel();
    panel.add(input, DockPanel.NORTH);

    panel.add(this.grid, DockPanel.CENTER);

    rootPanel.add(panel);
  }

  @VisibleForTesting
  interface Alerter {
    void alert(String message);
  }

}
