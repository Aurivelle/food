import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Selector {

    private final ArrayList<String> foods; // 食物清單
    private final Random random;          // 隨機生成器

    // 構造函數
    public Selector() {
        foods = new ArrayList<>();
        random = new Random();
        // 確保資料夾存在
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdir(); // 自動建立資料夾
            System.out.println("Created directory: " + dataDir.getAbsolutePath());
        }
    }

    // 新增食物
    public void add(String food) {
        if (!foods.contains(food)) {
            foods.add(food);
            System.out.println("Added " + food + " to the list of foods.");
        } else {
            System.out.println(food + " is already in the list.");
        }
    }

    // 刪除食物
    public void remove(String food) {
        if (foods.remove(food)) {
            System.out.println("Removed " + food + " from the list of foods.");
        } else {
            System.out.println(food + " is not in the list.");
        }
    }

    // 隨機選擇食物
    public String select() {
        if (foods.isEmpty()) {
            return "No foods to select from.";
        }
        int index = random.nextInt(foods.size());
        return foods.get(index);
    }

    // 儲存食物清單到檔案
    public void saveToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String food : foods) {
                writer.write(food);
                writer.newLine();
            }
            System.out.println("Foods have been saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving to file: " + e.getMessage());
        }
    }

    // 從檔案加載食物清單
    public void loadFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found, creating a new file: " + filePath);
            saveToFile(filePath); // 自動創建空檔案
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String food;
            while ((food = reader.readLine()) != null) {
                if (!foods.contains(food)) { // 避免重複
                    foods.add(food);
                }
            }
            System.out.println("Foods have been loaded from " + filePath);
        } catch (IOException e) {
            System.err.println("Error loading from file: " + e.getMessage());
        }
    }

    // 返回當前的食物清單
    public List<String> getFoods() {
        return new ArrayList<>(foods); // 返回副本，避免直接修改原始清單
    }
}
