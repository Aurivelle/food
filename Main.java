import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class Main extends Application {

    private static final String FILE_NAME = "foods.txt";
    private static final double CANVAS_SIZE = 400;
    private static final double POINTER_WIDTH = 30;
    private static final double POINTER_HEIGHT = 30;
    private static final double WHEEL_RADIUS = 180;
    private static final Duration SPIN_DURATION = Duration.seconds(4);

    private Selector selector;
    private Canvas canvas;
    private GraphicsContext gc;
    private TextArea displayArea;
    private ListView<String> foodListView;
    private Button spinButton;

    private final DoubleProperty currentAngle = new SimpleDoubleProperty(0);

    @Override
    public void start(Stage primaryStage) {
        try {
            selector = new Selector(FILE_NAME);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Failed to load food data.");
            return;
        }

        canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        gc = canvas.getGraphicsContext2D();

        displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setWrapText(true);
        displayArea.setPrefSize(200, CANVAS_SIZE);

        foodListView = new ListView<>();
        updateFoodListView();

        drawWheel(currentAngle.get());

        currentAngle.addListener((obs, oldVal, newVal) -> drawWheel(newVal.doubleValue()));

        Polygon pointer = createPointer();

        Button addButton = new Button("Add Food");
        Button removeButton = new Button("Remove Food");
        spinButton = new Button("Spin the Wheel");
        Button displayButton = new Button("Display Food List");

        addButton.setOnAction(e -> showAddFoodDialog());
        removeButton.setOnAction(e -> showRemoveFoodDialog());
        spinButton.setOnAction(e -> spinWheel());
        displayButton.setOnAction(e -> displayFoodList());

        StackPane wheelPane = new StackPane(canvas, pointer);
        wheelPane.setAlignment(Pos.TOP_CENTER);

        VBox controlPane = new VBox(10, displayButton, addButton, removeButton, spinButton, displayArea);
        controlPane.setAlignment(Pos.TOP_CENTER);
        controlPane.setPadding(new Insets(10));

        VBox rightPane = new VBox(10, controlPane, new Label("Food List:"), foodListView);
        rightPane.setAlignment(Pos.TOP_CENTER);
        rightPane.setPadding(new Insets(10));

        HBox root = new HBox(20, wheelPane, rightPane);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Food Selector");

        primaryStage.setOnCloseRequest(e -> {
            try {
                selector.saveToFile();
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save food data.");
            }
        });
        primaryStage.show();
    }

    private Polygon createPointer() {
        double centerX = CANVAS_SIZE / 2;
        return new Polygon(
                centerX - POINTER_WIDTH / 2, 0,
                centerX + POINTER_WIDTH / 2, 0,
                centerX, POINTER_HEIGHT);
    }

    /**
     * 繪製轉盤 (解法 B：讓 -90° = 12 點鐘)
     * 
     * @param angle 當前轉盤旋轉角度
     */
    private void drawWheel(double angle) {
        List<String> foods = selector.getFoods();
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        if (foods.isEmpty()) {
            gc.setFill(Color.GRAY);
            gc.fillOval((CANVAS_SIZE - WHEEL_RADIUS * 2) / 2, (CANVAS_SIZE - WHEEL_RADIUS * 2) / 2,
                    WHEEL_RADIUS * 2, WHEEL_RADIUS * 2);
            gc.setFill(Color.WHITE);
            String noFoodText = "No Foods Available";
            Text tempText = new Text(noFoodText);
            tempText.setFont(new Font("Arial", 14));
            double textWidth = tempText.getLayoutBounds().getWidth();
            gc.fillText(noFoodText, (CANVAS_SIZE - textWidth) / 2, CANVAS_SIZE / 2);
            return;
        }

        double centerX = CANVAS_SIZE / 2;
        double centerY = CANVAS_SIZE / 2;
        int sliceCount = foods.size();
        double sliceAngle = 360.0 / sliceCount;

        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(angle);
        gc.translate(-centerX, -centerY);

        for (int i = 0; i < sliceCount; i++) {

            double startAngle = -90 + i * sliceAngle;

            gc.setFill(Color.hsb(i * (360.0 / sliceCount), 0.8, 0.9));
            gc.fillArc(centerX - WHEEL_RADIUS, centerY - WHEEL_RADIUS,
                    WHEEL_RADIUS * 2, WHEEL_RADIUS * 2,
                    startAngle, sliceAngle, javafx.scene.shape.ArcType.ROUND);

            String text = foods.get(i);
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 14));

            Text tempText = new Text(text);
            tempText.setFont(gc.getFont());
            double textWidth = tempText.getLayoutBounds().getWidth();
            double textHeight = tempText.getLayoutBounds().getHeight();

            double textAngle = startAngle + sliceAngle / 2;

            double radians = Math.toRadians(textAngle);

            double textRadius = WHEEL_RADIUS * 0.5;
            double textX = centerX + textRadius * Math.cos(radians);
            double textY = centerY + textRadius * Math.sin(radians);

            gc.fillText(text, textX - textWidth / 2, textY + textHeight / 4);
        }

        gc.restore();
    }

    private void showAddFoodDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Food");
        dialog.setHeaderText("Enter the name of the food:");
        dialog.setContentText("Food:");

        dialog.showAndWait().ifPresent(food -> {
            String trimmedFood = food.trim();
            if (!trimmedFood.isEmpty()) {
                boolean added = selector.add(trimmedFood);
                if (added) {
                    updateFoodListView();
                    updateDisplay("Added: " + trimmedFood);
                    drawWheel(currentAngle.get());
                    try {
                        selector.saveToFile();
                    } catch (IOException ex) {
                        showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save food data.");
                    }
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Duplicate Entry", "The food already exists.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Food name cannot be empty.");
            }
        });
    }

    private void showRemoveFoodDialog() {
        List<String> foods = selector.getFoods();
        if (foods.isEmpty()) {
            updateDisplay("No foods available to remove.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(foods.get(0), foods);
        dialog.setTitle("Remove Food");
        dialog.setHeaderText("Select the food to remove:");
        dialog.setContentText("Food:");

        dialog.showAndWait().ifPresent(food -> {
            boolean removed = selector.remove(food);
            if (removed) {
                updateFoodListView();
                updateDisplay("Removed: " + food);
                drawWheel(currentAngle.get());
                try {
                    selector.saveToFile();
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save food data.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Removal Error", "Failed to remove the selected food.");
            }
        });
    }

    private void displayFoodList() {
        List<String> foods = selector.getFoods();
        displayArea.clear();
        if (foods.isEmpty()) {
            updateDisplay("No foods available.");
            return;
        }
        displayArea.appendText("List of foods:\n");
        foods.forEach(food -> displayArea.appendText(food + "\n"));
    }

    private void updateFoodListView() {
        List<String> foods = selector.getFoods();
        foodListView.getItems().setAll(foods);
    }

    private void spinWheel() {
        List<String> foods = selector.getFoods();
        if (foods.isEmpty()) {
            updateDisplay("No foods available to spin.");
            return;
        }

        spinButton.setDisable(true);

        int sliceCount = foods.size();
        double sliceAngle = 360.0 / sliceCount;
        int randomIndex = (int) (Math.random() * sliceCount);

        double sliceCenterAngle = -90 + randomIndex * sliceAngle + (sliceAngle / 2);

        double neededAngleAtTop = -sliceCenterAngle;

        double currentAngleNormalized = currentAngle.get() % 360;
        double baseAngle = currentAngleNormalized + neededAngleAtTop;
        double extraSpins = 360 * 5;
        double targetAngle = baseAngle + extraSpins;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(currentAngle, currentAngle.get())),
                new KeyFrame(SPIN_DURATION, new KeyValue(currentAngle, targetAngle, Interpolator.EASE_OUT)));

        timeline.setOnFinished(e -> {
            currentAngle.set(targetAngle % 360);
            String selectedFood = foods.get(randomIndex);
            updateDisplay("Selected: " + selectedFood);
            spinButton.setDisable(false);
        });

        timeline.play();
    }

    private void updateDisplay(String text) {
        displayArea.appendText(text + "\n");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
