import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class GUI extends Application {
    @Override
    public void start(Stage stage) {
        Scene mainMenuScene = GUI.newMainMenuScene(stage, null);
        stage.setTitle("Sudoku");
        stage.setOnShowing(windowEvent -> stage.centerOnScreen());

        stage.setScene(mainMenuScene);
        stage.show();
    }

    private static Scene newMainMenuScene(Stage stage, Scene previousGridScene) {
        GridPane root = new GridPane();
        root.getStyleClass().add("root");
        root.setVgap(20);

        HBox rootBox = new HBox(root);
        rootBox.setAlignment(Pos.CENTER);
        HBox.setMargin(root, new Insets(20, 20, 20, 20));

        Scene scene = new Scene(rootBox);
        scene.getStylesheets().add("style.css");

        Label cluesCountLabel = new Label("Number of clues");
        final int minCluesCount = 0;
        final int maxCluesCount = 81;
        final int defaultCluesCount = (int) (maxCluesCount * 0.5);
        Slider cluesCountSlider = new Slider(minCluesCount, maxCluesCount, defaultCluesCount);
        cluesCountSlider.setShowTickLabels(true);
        cluesCountSlider.setShowTickMarks(true);
        cluesCountSlider.setBlockIncrement(1);
        cluesCountLabel.setLabelFor(cluesCountSlider);
        Text cluesCountText = new Text(Integer.toString(Double.valueOf(cluesCountSlider.getValue()).intValue()));
        cluesCountSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            cluesCountSlider.setValue(newValue.intValue());
            cluesCountText.setText(Integer.toString(newValue.intValue()));
        });
        root.add(cluesCountLabel, 0, 0);
        root.add(cluesCountSlider, 0, 1);
        root.add(cluesCountText, 0, 2);

        Button newPuzzleButton = new Button("New puzzle");
        newPuzzleButton.setOnAction(actionEvent -> {
            int cluesCount = Double.valueOf(cluesCountSlider.getValue()).intValue();
            stage.hide();
            stage.setScene(newGridSceneWithNewGrid(cluesCount, stage));
            stage.show();
        });
        root.add(newPuzzleButton, 0, 3);
        newPuzzleButton.requestFocus();

        if (previousGridScene != null) {
            Button resumeButton = new Button("Resume");
            resumeButton.setOnAction(actionEvent -> {
                stage.hide();
                stage.setScene(previousGridScene);
                stage.show();
            });
            root.add(resumeButton, 0, 4);
        }

        return scene;
    }

    private static Scene newGridSceneWithNewGrid(int cluesCount, Stage stage) {
        final GridLayout gridLayout = GridLayout.newClassic();
        final Map<List<Integer>, Integer> completedCellValues = CellValues.newRandomCompleted(gridLayout);
        final Map<List<Integer>, Integer> partiallyCompletedCellValues = CellValues.newRandomPartiallyCompleted(gridLayout, completedCellValues, cluesCount);
        final Map<List<Integer>, Integer> partiallyCompletedCellValuesCopy = new ConcurrentHashMap<>(partiallyCompletedCellValues);

        //

        final List<Integer> defaultCellIndex = gridLayout.cellIndexes.stream().findFirst().orElseThrow();

        final Map<List<Integer>, Pane> cellPanes = new HashMap<>();
        final Map<List<Integer>, Label> cellLabels = new HashMap<>();

        Label messageLabel = new Label();

        ObservableMap<List<Integer>, Integer> observablePartiallyCompletedCellValuesCopy = FXCollections.observableMap(partiallyCompletedCellValuesCopy);
        observablePartiallyCompletedCellValuesCopy.addListener((MapChangeListener<List<Integer>, Integer>) change -> {
            if (change.wasRemoved()) {
                cellLabels.get(change.getKey()).setText("");
            }
            if (change.wasAdded()) {
                cellLabels.get(change.getKey()).setText(Integer.toString(change.getValueAdded()));
            }

            boolean completedAndValid = CellValues.isCompleted(gridLayout, observablePartiallyCompletedCellValuesCopy)
                    && CellValues.isValid(gridLayout, observablePartiallyCompletedCellValuesCopy);

            messageLabel.setText("");
            if (completedAndValid) {
                messageLabel.setText("Grid is completed and valid");
            }
        });

        ObservableSet<List<Integer>> selectedCellContainer = FXCollections.observableSet(new HashSet<>());
        selectedCellContainer.addListener((SetChangeListener<List<Integer>>) change -> {
            if (change.wasRemoved()) {
                List<Integer> cellIndex = change.getElementRemoved();
                cellPanes.get(cellIndex).getStyleClass().remove("selected-cell-pane");
                gridLayout.cellPeers.get(cellIndex).forEach(peer -> cellPanes.get(peer).getStyleClass().remove("selected-cell-peer-pane"));
            }
            if (change.wasAdded()) {
                List<Integer> cellIndex = change.getElementAdded();
                cellPanes.get(cellIndex).getStyleClass().add("selected-cell-pane");
                gridLayout.cellPeers.get(cellIndex).forEach(peer -> cellPanes.get(peer).getStyleClass().add("selected-cell-peer-pane"));
            }
        });

        //

        GridPane root = new GridPane();
        root.getStyleClass().add("root");

        HBox rootBox = new HBox(root);
        rootBox.setAlignment(Pos.CENTER);
        HBox.setMargin(root, new Insets(20, 20, 20, 20));

        Scene scene = new Scene(rootBox);
        scene.getStylesheets().add("style.css");

        final int cellLength = 40;
        final int cellGap = 1;

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("grid-pane");
        gridPane.setHgap(cellGap);
        gridPane.setVgap(cellGap);
        for (int columnIndex = 0; columnIndex < gridLayout.columnsCount; columnIndex++) {
            ColumnConstraints columnConstraints = new ColumnConstraints(cellLength);
            columnConstraints.setHgrow(Priority.NEVER);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        for (int rowIndex = 0; rowIndex < gridLayout.rowsCount; rowIndex++) {
            RowConstraints rowConstraints = new RowConstraints(cellLength);
            rowConstraints.setVgrow(Priority.NEVER);
            gridPane.getRowConstraints().add(rowConstraints);
        }
        for (List<Integer> cellIndex : gridLayout.cellIndexes) {
            Optional<Integer> cellValue = Optional.ofNullable(observablePartiallyCompletedCellValuesCopy.get(cellIndex));
            String cellValueView = cellValue.map(value -> Integer.toString(value)).orElse("");

            StackPane cellPane = new StackPane();
            cellPane.getStyleClass().add("cell-pane");
            cellPane.setOnMousePressed(mouseEvent -> selectedCellContainer.stream().findFirst()
                    .ifPresentOrElse(
                            selectedCellIndex -> {
                                if (selectedCellIndex.equals(cellIndex)) {
                                    selectedCellContainer.remove(selectedCellIndex);
                                    return;
                                }
                                selectedCellContainer.remove(selectedCellIndex);
                                selectedCellContainer.add(cellIndex);
                            },
                            () -> selectedCellContainer.add(cellIndex)
                    ));
            cellPanes.put(cellIndex, cellPane);
            if (partiallyCompletedCellValues.containsKey(cellIndex)) {
                cellPane.getStyleClass().add("clue-cell-pane");
            }

            Label cellText = new Label();
            cellText.setTextAlignment(TextAlignment.CENTER);
            cellText.setText(cellValueView);
            cellPane.getChildren().add(cellText);
            cellLabels.put(cellIndex, cellText);

            gridPane.add(cellPane, CellIndex.getColumnIndex(cellIndex), CellIndex.getRowIndex(cellIndex));
        }

        HBox gridPaneBox = new HBox(gridPane);
        gridPaneBox.setAlignment(Pos.CENTER);
        HBox.setMargin(gridPane, new Insets(0, 0, 20, 0));
        root.add(gridPaneBox, 0, 0);
        gridPane.requestFocus();

        //

        GridPane cellValuesPane = new GridPane();
        cellValuesPane.setHgap(5);
        cellValuesPane.setVgap(5);
        for (int value : gridLayout.values) {
            Button cellValueButton = new Button(Integer.toString(value));
            cellValueButton.getStyleClass().add("cell-value-button");
            cellValueButton.setOnAction(actionEvent -> selectedCellContainer.stream().findFirst()
                    .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                    .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, value)));
            cellValuesPane.add(cellValueButton, value, 0);
        }
        Button removeCellValueButton = new Button("");
        removeCellValueButton.getStyleClass().add("cell-value-button");
        removeCellValueButton.setOnAction(actionEvent -> selectedCellContainer.stream().findFirst()
                .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                .ifPresent(observablePartiallyCompletedCellValuesCopy::remove));
        cellValuesPane.add(removeCellValueButton, gridLayout.values.size() + 1, 0);

        HBox cellValuesPaneBox = new HBox(cellValuesPane);
        cellValuesPaneBox.setAlignment(Pos.CENTER);
        HBox.setMargin(cellValuesPane, new Insets(0, 0, 0, 0));
        root.add(cellValuesPaneBox, 0, 1);

        //

        Button mainMenuButton = new Button("Main menu");
        mainMenuButton.setOnAction(actionEvent -> {
            stage.hide();
            stage.setScene(newMainMenuScene(stage, scene));
            stage.show();
        });
        Button restartButton = new Button("Restart");
        restartButton.setOnAction(actionEvent -> observablePartiallyCompletedCellValuesCopy.keySet().stream()
                .filter(Predicate.not(partiallyCompletedCellValues.keySet()::contains))
                .forEach(observablePartiallyCompletedCellValuesCopy::remove));
        FlowPane actionsPane = new FlowPane(mainMenuButton, restartButton);
        FlowPane.setMargin(mainMenuButton, new Insets(0, 20, 0, 0));
        FlowPane.setMargin(restartButton, new Insets(0, 20, 0, 0));
        HBox actionsPaneBox = new HBox(actionsPane);
        actionsPaneBox.setAlignment(Pos.TOP_LEFT);
        HBox.setMargin(actionsPane, new Insets(20, 0, 0, 5));
        root.add(actionsPaneBox, 0, 2);

        //

        HBox messageLabelBox = new HBox(messageLabel);
        HBox.setMargin(messageLabel, new Insets(20, 0, 0, 5));
        root.add(messageLabelBox, 0, 3);

        //

        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP -> selectedCellContainer.stream().findFirst().ifPresentOrElse(
                        selectedCellIndex -> gridLayout.getTopCellIndex(selectedCellIndex).ifPresent(cellIndex -> {
                            selectedCellContainer.remove(selectedCellIndex);
                            selectedCellContainer.add(cellIndex);
                        }),
                        () -> selectedCellContainer.add(defaultCellIndex)
                );
                case DOWN -> selectedCellContainer.stream().findFirst().ifPresentOrElse(
                        selectedCellIndex -> gridLayout.getBottomCellIndex(selectedCellIndex).ifPresent(cellIndex -> {
                            selectedCellContainer.remove(selectedCellIndex);
                            selectedCellContainer.add(cellIndex);
                        }),
                        () -> selectedCellContainer.add(defaultCellIndex)
                );
                case LEFT -> selectedCellContainer.stream().findFirst().ifPresentOrElse(
                        selectedCellIndex -> gridLayout.getLeftCellIndex(selectedCellIndex).ifPresent(cellIndex -> {
                            selectedCellContainer.remove(selectedCellIndex);
                            selectedCellContainer.add(cellIndex);
                        }),
                        () -> selectedCellContainer.add(defaultCellIndex)
                );
                case RIGHT -> selectedCellContainer.stream().findFirst().ifPresentOrElse(
                        selectedCellIndex -> gridLayout.getRightCellIndex(selectedCellIndex).ifPresent(cellIndex -> {
                            selectedCellContainer.remove(selectedCellIndex);
                            selectedCellContainer.add(cellIndex);
                        }),
                        () -> selectedCellContainer.add(defaultCellIndex)
                );

                case DIGIT0 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(observablePartiallyCompletedCellValuesCopy::remove);
                case DIGIT1 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, 1));
                case DIGIT2 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, 2));
                case DIGIT3 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, 3));
                case DIGIT4 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, 4));
                case DIGIT5 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, 5));
                case DIGIT6 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, 6));
                case DIGIT7 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, 7));
                case DIGIT8 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, 8));
                case DIGIT9 -> selectedCellContainer.stream().findFirst()
                        .filter(Predicate.not(partiallyCompletedCellValues::containsKey))
                        .ifPresent(selectedCellIndex -> observablePartiallyCompletedCellValuesCopy.put(selectedCellIndex, 9));
            }
        });

        return scene;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
