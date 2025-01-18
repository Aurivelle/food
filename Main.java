import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {
    private Selector selector = new Selector();
    private TextArea displayArea = new TextArea();
    private final String filePath = "data/foods.txt";

    @Override
    public void start(Stage primaryStage) {
        selector.loadFromFile(filePath);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Food Selector");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button addButton = new Button("Add Food");
        Button removeButton = new Button("Remove Food");
        Button selectButton = new Button("Select Random Food");
        Button displayButton = new Button("Display Food List");

        addButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Food");
            dialog.setHeaderText("Enter the name of the food:");
            dialog.setContentText("Food:");
            dialog.showAndWait().ifPresent(food -> {
                selector.add(food);
                updateDisplay("Added: " + food);
            });
        });

        removeButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Remove Food");
            dialog.setHeaderText("Enter the name of the food:");
            dialog.setContentText("Food:");
            dialog.showAndWait().ifPresent(food -> {
                selector.remove(food);
                updateDisplay("Removed: " + food);
            });
        });

        selectButton.setOnAction(event -> {
            String food = selector.select();
            updateDisplay("Selected: " + food);
        });

        displayButton.setOnAction(event -> {
            updateDisplay("List of foods:");
            for (String food : selector.getFoods()) {
                updateDisplay(food);
            }
        });

        HBox buttonBox = new HBox(10, addButton, removeButton, selectButton, displayButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        displayArea.setEditable(false);
        displayArea.setWrapText(true);
        displayArea.setPrefHeight(200);

        root.getChildren().addAll(title, buttonBox, displayArea);

        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("Food Selector");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> selector.saveToFile(filePath));
        primaryStage.show();
    }

    private void updateDisplay(String text) {
        displayArea.appendText(text + System.lineSeparator());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
