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
     * Add foods and see if it is successful
     *
     * @param food food name
     * @return see if it is successful
     */
    public boolean add(String food) {
        return foods.add(food);
    }

    /**
     * Delete foods
     *
     * @param food food name
     * @return see if it is successful
     */
    public boolean remove(String food) {
        return foods.remove(food);
    }

    /**
     * choose randomly
     *
     * @return name or empty string
     */
    public String select() {
        if (foods.isEmpty()) {
            return "";
        }
        int index = (int) (Math.random() * foods.size());
        return foods.stream().skip(index).findFirst().orElse("");
    }

    /**
     * Store
     *
     * @throws IOException if fails
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
     * load foods
     *
     * @throws IOException if fails
     */
    public void loadFromFile() throws IOException {
        if (Files.notExists(filePath)) {
            saveToFile(); // if empty
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
     * get all the foods
     *
     * @return unchangeable food list
     */
    public List<String> getFoods() {
        return foods.stream().collect(Collectors.toList());
    }
}
