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
    // 常量定義
    private static final String FILE_NAME = "foods.txt";
    private static final double CANVAS_SIZE = 400;
    private static final double POINTER_WIDTH = 30;
    private static final double POINTER_HEIGHT = 30;
    private static final double WHEEL_RADIUS = 180;
    private static final Duration SPIN_DURATION = Duration.seconds(4);

    // UI 元件
    private Selector selector;
    private Canvas canvas;
    private GraphicsContext gc;
    private TextArea displayArea;
    private ListView<String> foodListView;

    // 旋轉狀態：用 DoubleProperty 以支援動畫屬性綁定
    private final DoubleProperty currentAngle = new SimpleDoubleProperty(0);

    @Override
    public void start(Stage primaryStage) {
        try {
            // 初始化 Selector，讀取預設檔案
            selector = new Selector(FILE_NAME);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Failed to load food data.");
            return; // 讀檔失敗就不啟動
        }

        // 建立 Canvas 與 GraphicsContext
        canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        gc = canvas.getGraphicsContext2D();
        // 文字輸出區
        displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setWrapText(true);
        displayArea.setPrefSize(200, CANVAS_SIZE);

        // 食物清單 ListView
        foodListView = new ListView<>();
        updateFoodListView();

        // 先繪製一次轉盤
        drawWheel(currentAngle.get());

        // 每當 currentAngle 改變，就重新繪製轉盤
        currentAngle.addListener((obs, oldVal, newVal) -> drawWheel(newVal.doubleValue()));

        // 建立指針（三角形）
        Polygon pointer = createPointer();

        // 建立按鈕
        Button addButton = new Button("Add Food");
        Button removeButton = new Button("Remove Food");
        Button spinButton = new Button("Spin the Wheel");
        Button displayButton = new Button("Display Food List");

        // 設置按鈕事件
        addButton.setOnAction(e -> showAddFoodDialog());
        removeButton.setOnAction(e -> showRemoveFoodDialog());
        spinButton.setOnAction(e -> spinWheel());
        displayButton.setOnAction(e -> displayFoodList());

        // 左側區域：放「轉盤 + 指針」
        StackPane wheelPane = new StackPane(canvas, pointer);
        wheelPane.setAlignment(Pos.TOP_CENTER); // 指針頂部對齊

        // 中間控制面板：包含按鈕 + 顯示區
        VBox controlPane = new VBox(10, displayButton, addButton, removeButton, spinButton, displayArea);
        controlPane.setAlignment(Pos.TOP_CENTER);
        controlPane.setPadding(new Insets(10));

        // 右邊額外區域：食物列表
        VBox rightPane = new VBox(10, controlPane, new Label("Food List:"), foodListView);
        rightPane.setAlignment(Pos.TOP_CENTER);
        rightPane.setPadding(new Insets(10));

        // 主佈局（水平擺放）
        HBox root = new HBox(20, wheelPane, rightPane);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Food Selector");
        // 關閉時儲存檔案
        primaryStage.setOnCloseRequest(e -> {
            try {
                selector.saveToFile();
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save food data.");
            }
        });
        primaryStage.show();
    }

    /**
     * 建立指針圖形，朝上
     */
    private Polygon createPointer() {
        double centerX = CANVAS_SIZE / 2;
        double pointerHeight = POINTER_HEIGHT;
        double pointerWidth = POINTER_WIDTH;
        // 使指針位於畫布頂端，指向正上方
        Polygon pointer = new Polygon(
                centerX - pointerWidth / 2, 0, // 左下角
                centerX + pointerWidth / 2, 0, // 右下角
                centerX, pointerHeight // 頂端
        );
        pointer.setFill(Color.RED);
        return pointer;
    }

    /**
     * 繪製轉盤
     *
     * @param angle 當前轉盤旋轉角度
     */
    private void drawWheel(double angle) {
        List<String> foods = selector.getFoods();
        // 先清空畫布
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        if (foods.isEmpty()) {
            // 若沒有任何食物，顯示灰色圓形 + 提示文字
            gc.setFill(Color.GRAY);
            gc.fillOval((CANVAS_SIZE - WHEEL_RADIUS * 2) / 2, (CANVAS_SIZE - WHEEL_RADIUS * 2) / 2,
                    WHEEL_RADIUS * 2, WHEEL_RADIUS * 2);
            gc.setFill(Color.WHITE);
            gc.fillText("No Foods Available", CANVAS_SIZE / 2 - 50, CANVAS_SIZE / 2);
            return;
        }

        double centerX = CANVAS_SIZE / 2;
        double centerY = CANVAS_SIZE / 2;
        int sliceCount = foods.size();
        double sliceAngle = 360.0 / sliceCount; // 每個扇形的角度

        // 將畫布的原點移到中心，並旋轉畫布
        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(angle);
        gc.translate(-centerX, -centerY);

        for (int i = 0; i < sliceCount; i++) {
            // 每個扇形的起始角度
            double startAngle = sliceAngle * i;

            // 繪製彩色扇形
            gc.setFill(Color.hsb(i * (360.0 / sliceCount), 0.8, 0.9));
            gc.fillArc(centerX - WHEEL_RADIUS, centerY - WHEEL_RADIUS,
                    WHEEL_RADIUS * 2, WHEEL_RADIUS * 2,
                    startAngle, sliceAngle, javafx.scene.shape.ArcType.ROUND);

            // ===== 文字繪製：保持文字水平 =====
            double textAngle = startAngle + sliceAngle / 2; // 扇形中央角度
            double radians = Math.toRadians(textAngle - 90); // 扣 90°，把 0° 設定在正上方
            double textRadius = WHEEL_RADIUS * 0.5; // 讓文字位在較內圈
            double textX = centerX + textRadius * Math.cos(radians);
            double textY = centerY + textRadius * Math.sin(radians);

            // 設定字體和顏色
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 14));

            // 取得要顯示的文字
            String text = foods.get(i);

            // 計算文字寬高（使用 Text 類，以便獲得精準尺寸）
            Text tempText = new Text(text);
            tempText.setFont(gc.getFont());
            double textWidth = tempText.getLayoutBounds().getWidth();
            double textHeight = tempText.getLayoutBounds().getHeight();

            // 以 (textX, textY) 為中心點，繪製文字
            gc.fillText(text, textX - textWidth / 2, textY + textHeight / 4);
        }

        gc.restore(); // 恢復畫布狀態
    }

    /**
     * 顯示新增食物的對話框
     */
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
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Duplicate Entry", "The food already exists.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Food name cannot be empty.");
            }
        });
    }

    /**
     * 顯示刪除食物的對話框
     */
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
            } else {
                showAlert(Alert.AlertType.ERROR, "Removal Error", "Failed to remove the selected food.");
            }
        });
    }

    /**
     * 顯示食物列表
     */
    private void displayFoodList() {
        List<String> foods = selector.getFoods();
        if (foods.isEmpty()) {
            updateDisplay("No foods available.");
            return;
        }
        displayArea.clear();
        displayArea.appendText("List of foods:\n");
        foods.forEach(food -> displayArea.appendText(food + "\n"));
    }

    /**
     * 更新食物列表視圖
     */
    private void updateFoodListView() {
        List<String> foods = selector.getFoods();
        foodListView.getItems().setAll(foods);
    }

    /**
     * 旋轉轉盤動畫（**核心修改：事先決定要中的扇形**）
     */
    private void spinWheel() {
        List<String> foods = selector.getFoods();
        if (foods.isEmpty()) {
            updateDisplay("No foods available to spin.");
            return;
        }

        int sliceCount = foods.size();
        double sliceAngle = 360.0 / sliceCount;

        // ===========【 1. 隨機挑一個中獎索引 】==========
        int randomIndex = (int) (Math.random() * sliceCount);

        // ===========【 2. 計算該扇形中心角度 】==========
        // 若我們把 0° 設在畫布頂部(指針位置 = 90°)，
        // 那麼扇形 i 的中心就是 (i * sliceAngle + sliceAngle/2)「相對於右邊水平 0°」。
        // 要把它對上 90°，就得做換算。
        double sliceCenterAngle = (randomIndex * sliceAngle) + (sliceAngle / 2);

        // ===========【 3. 轉到「正上方 90°」所需角度 】==========
        // neededAngleAtTop = 90 - sliceCenterAngle
        double neededAngleAtTop = 45 - sliceCenterAngle;
        // 正規化到 0 ~ 360
        neededAngleAtTop = (neededAngleAtTop % 360 + 360) % 360;

        // ===========【 4. 從 currentAngle 出發，加上 neededAngleAtTop 】==========
        double baseAngle = (currentAngle.get() % 360) + neededAngleAtTop;

        // ===========【 5. 至少多轉 5 圈 】==========
        double extraSpins = 360 * 5; // 可以改成隨機圈數
        double targetAngle = baseAngle + extraSpins;

        // ===========【 6. 動畫：currentAngle -> targetAngle 】==========
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(currentAngle, currentAngle.get())),
                new KeyFrame(SPIN_DURATION, new KeyValue(currentAngle, targetAngle, Interpolator.EASE_OUT)));

        // ===========【 7. 動畫結束後，直接用 randomIndex 取食物 】==========
        timeline.setOnFinished(e -> {
            currentAngle.set(targetAngle % 360); // 校正
            String selectedFood = foods.get(randomIndex); // 已經事先決定哪塊中了
            updateDisplay("Selected: " + selectedFood);
        });

        timeline.play();
    }

    /**
     * 更新顯示區域
     */
    private void updateDisplay(String text) {
        displayArea.appendText(text + "\n");
    }

    /**
     * 顯示警告對話框
     */
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
