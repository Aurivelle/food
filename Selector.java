import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Selector {

    private final Set<String> foods;
    private final Path dataDir;
    private final Path filePath;

    public Selector(String filePathStr) throws IOException {
        this.foods = new LinkedHashSet<>();
        this.dataDir = Paths.get("data");
        this.filePath = dataDir.resolve(filePathStr);

        if (Files.notExists(dataDir)) {
            Files.createDirectories(dataDir);
        }

        loadFromFile();
    }

    /**
     * 新增食物，若已存在則不新增
     *
     * @param food 食物名稱
     * @return 是否成功新增
     */
    public boolean add(String food) {
        return foods.add(food);
    }

    /**
     * 刪除食物
     *
     * @param food 食物名稱
     * @return 是否成功刪除
     */
    public boolean remove(String food) {
        return foods.remove(food);
    }

    /**
     * 隨機選取一個食物
     *
     * @return 被選取的食物名稱，若無食物則返回空字串
     */
    public String select() {
        if (foods.isEmpty()) {
            return "";
        }
        int index = (int) (Math.random() * foods.size());
        return foods.stream().skip(index).findFirst().orElse("");
    }

    /**
     * 儲存食物列表至檔案
     *
     * @throws IOException 儲存失敗時拋出
     */
    public void saveToFile() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String food : foods) {
                writer.write(food);
                writer.newLine();
            }
        }
    }

    /**
     * 從檔案載入食物列表
     *
     * @throws IOException 載入失敗時拋出
     */
    public void loadFromFile() throws IOException {
        if (Files.notExists(filePath)) {
            saveToFile(); // 建立空檔案
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            foods.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    foods.add(trimmed);
                }
            }
        }
    }

    /**
     * 獲取所有食物列表
     *
     * @return 食物列表的不可變副本
     */
    public List<String> getFoods() {
        return foods.stream().collect(Collectors.toList());
    }
}
