package com.dopamine.cobblemontrainermaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppController {

    @FXML private TextField trainerNameField;
    @FXML private TextField identityField;
    @FXML private ComboBox<String> battleFormatBox;
    @FXML private Spinner<Integer> maxItemUsesSpinner;
    @FXML private TextField maxSelectMarginField;
    @FXML private TextField bagItemField;
    @FXML private Spinner<Integer> bagQuantitySpinner;
    @FXML private ListView<String> bagListView;
    @FXML private TextArea showdownInput;
    @FXML private TextField bagDropItemField;
    @FXML private ComboBox<String> defeatComparatorBox;
    @FXML private Spinner<Integer> defeatCountSpinner;
    @FXML private ListView<String> guaranteedDropListView;
    @FXML private ToggleButton fixedRollsToggle;
    @FXML private ToggleButton binomialRollsToggle;
    @FXML private HBox fixedRollsBox;
    @FXML private HBox binomialRollsBox;
    @FXML private Spinner<Integer> weightedRollsSpinner;
    @FXML private Spinner<Integer> binomialNSpinner;
    @FXML private TextField binomialPField;
    @FXML private ComboBox<String> entryTypeBox;
    @FXML private TextField entryNameField;
    @FXML private Spinner<Integer> entryWeightSpinner;
    @FXML private TextField levelMinField;
    @FXML private TextField levelMaxField;
    @FXML private ListView<String> weightedEntryListView;

    private final ObservableList<String> bagDisplayItems = FXCollections.observableArrayList();
    private final ObservableList<String> guaranteedDropItems = FXCollections.observableArrayList();
    private final ObservableList<String> weightedEntryItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        battleFormatBox.getItems().addAll(
                "GEN_9_SINGLES", "GEN_9_DOUBLES",
                "GEN_8_SINGLES", "GEN_8_DOUBLES",
                "GEN_7_SINGLES", "GEN_7_DOUBLES"
        );
        battleFormatBox.setValue("GEN_9_SINGLES");

        maxItemUsesSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 3)
        );
        bagQuantitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1)
        );

        bagListView.setItems(bagDisplayItems);

        defeatComparatorBox.getItems().addAll("==", "<=", ">=", "<", ">", "%");
        defeatComparatorBox.setValue("==");

        entryTypeBox.getItems().addAll("minecraft:item", "minecraft:loot_table");
        entryTypeBox.setValue("minecraft:loot_table");

        defeatCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        weightedRollsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        binomialNSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        entryWeightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 100));

        guaranteedDropListView.setItems(guaranteedDropItems);
        weightedEntryListView.setItems(weightedEntryItems);

        ToggleGroup rollsGroup = new ToggleGroup();
        fixedRollsToggle.setToggleGroup(rollsGroup);
        binomialRollsToggle.setToggleGroup(rollsGroup);
        fixedRollsToggle.setSelected(true);
        rollsGroup.selectedToggleProperty().addListener((obs, old, newToggle) -> {
            if (newToggle == null) {
                rollsGroup.selectToggle(old);
                return;
            }
            boolean isBinomial = newToggle == binomialRollsToggle;
            fixedRollsBox.setVisible(!isBinomial);
            fixedRollsBox.setManaged(!isBinomial);
            binomialRollsBox.setVisible(isBinomial);
            binomialRollsBox.setManaged(isBinomial);
        });
    }

    @FXML
    protected void onAddBagItem() {
        String item = bagItemField.getText().trim();
        if (item.isEmpty()) return;
        int qty = bagQuantitySpinner.getValue();
        bagDisplayItems.add(item + " | x" + qty);
        bagItemField.clear();
        bagQuantitySpinner.getValueFactory().setValue(1);
    }

    @FXML
    protected void onRemoveBagItem() {
        int idx = bagListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) bagDisplayItems.remove(idx);
    }

    @FXML
    protected void onAddGuaranteedDrop() {
        String item = bagDropItemField.getText().trim();
        if (item.isEmpty()) return;
        String comparator = defeatComparatorBox.getValue();
        int count = defeatCountSpinner.getValue();
        guaranteedDropItems.add(item + " | " + comparator + " x" + count);
        bagDropItemField.clear();
        defeatCountSpinner.getValueFactory().setValue(1);
    }

    @FXML
    protected void onRemoveGuaranteedDrop() {
        int idx = guaranteedDropListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) guaranteedDropItems.remove(idx);
    }

    @FXML
    protected void onAddWeightedEntry() {
        String name = entryNameField.getText().trim();
        if (name.isEmpty()) return;
        String type = entryTypeBox.getValue();
        int weight = entryWeightSpinner.getValue();
        String lvlMin = levelMinField.getText().trim();
        String lvlMax = levelMaxField.getText().trim();
        StringBuilder entry = new StringBuilder(type + " | " + name + " | w=" + weight);
        if (!lvlMin.isEmpty() && !lvlMax.isEmpty()) {
            entry.append(" | lvl=").append(lvlMin).append("-").append(lvlMax);
        }
        weightedEntryItems.add(entry.toString());
        entryNameField.clear();
        entryWeightSpinner.getValueFactory().setValue(100);
        levelMinField.clear();
        levelMaxField.clear();
    }

    @FXML
    protected void onRemoveWeightedEntry() {
        int idx = weightedEntryListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) weightedEntryItems.remove(idx);
    }

    @FXML
    protected void onGenerateJson() {
        String identity = identityField.getText().trim();
        if (identity.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Identity field is required — it will be used as the filename.");
            return;
        }

        String showdown = showdownInput.getText();
        if (showdown.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Paste a Showdown team first.");
            return;
        }

        ShowdownParser parser = new ShowdownParser();
        try {
            parser.parse(showdown);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error parsing team: " + e.getMessage());
            return;
        }

        String teamJson = parser.getTeam();
        String name = trainerNameField.getText().trim();
        String format = battleFormatBox.getValue();
        int maxItemUses = maxItemUsesSpinner.getValue();
        double margin;
        try {
            margin = Double.parseDouble(maxSelectMarginField.getText().trim());
        } catch (NumberFormatException e) {
            margin = 0.05;
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"name\": \"").append(name).append("\",\n");
        json.append("  \"identity\": \"").append(identity).append("\",\n");
        json.append("  \"ai\": {\n");
        json.append("    \"type\": \"rct\",\n");
        json.append("    \"data\": { \"maxSelectMargin\": ").append(margin).append(" }\n");
        json.append("  },\n");
        json.append("  \"battleFormat\": \"").append(format).append("\",\n");
        json.append("  \"battleRules\": { \"maxItemUses\": ").append(maxItemUses).append(" },\n");
        json.append("  \"bag\": ").append(buildBagJson()).append(",\n");
        json.append("  \"team\": ").append(teamJson.replace("\n", "\n  ")).append("\n");
        json.append("}");

        try {
            Path dir = Paths.get("trainers");
            Files.createDirectories(dir);
            Path file = dir.resolve(identity + ".json");
            Files.writeString(file, json.toString());
            StringBuilder successMsg = new StringBuilder("Saved to ").append(file.toAbsolutePath());
            if (!guaranteedDropItems.isEmpty() || !weightedEntryItems.isEmpty()) {
                Path lootDir = Paths.get("trainers", "loot_tables");
                Files.createDirectories(lootDir);
                Path lootFile = lootDir.resolve(identity + ".json");
                Files.writeString(lootFile, buildLootTableJson());
                successMsg.append("\n+ loot table saved to ").append(lootFile.toAbsolutePath());
            }
            showAlert(Alert.AlertType.INFORMATION, successMsg.toString());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to write file: " + e.getMessage());
        }
    }

    private String buildBagJson() {
        if (bagDisplayItems.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < bagDisplayItems.size(); i++) {
            String entry = bagDisplayItems.get(i);
            int sep = entry.lastIndexOf(" | x");
            String item = sep >= 0 ? entry.substring(0, sep) : entry;
            int qty = 1;
            if (sep >= 0) {
                try { qty = Integer.parseInt(entry.substring(sep + 4)); }
                catch (NumberFormatException ignored) {}
            }
            sb.append("    { \"item\": \"").append(item).append("\", \"quantity\": ").append(qty).append(" }");
            if (i < bagDisplayItems.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]");
        return sb.toString();
    }

    private String buildLootTableJson() {
        StringBuilder sb = new StringBuilder("{\n  \"pools\": [\n");
        boolean firstPool = true;

        if (!guaranteedDropItems.isEmpty()) {
            firstPool = false;
            sb.append("    {\n");
            sb.append("      \"rolls\": 1,\n");
            sb.append("      \"entries\": [\n");
            for (int i = 0; i < guaranteedDropItems.size(); i++) {
                String entry = guaranteedDropItems.get(i);
                String[] parts = entry.split(" \\| ");
                String item = parts[0].trim();
                String comparatorCount = parts.length > 1 ? parts[1].trim() : "== x1";
                int xIdx = comparatorCount.lastIndexOf(" x");
                String comparator = xIdx >= 0 ? comparatorCount.substring(0, xIdx).trim() : "==";
                int count = 1;
                if (xIdx >= 0) {
                    try { count = Integer.parseInt(comparatorCount.substring(xIdx + 2)); }
                    catch (NumberFormatException ignored) {}
                }
                sb.append("        { \"type\": \"minecraft:item\", \"name\": \"").append(item)
                  .append("\", \"conditions\": [{ \"condition\": \"rctmod:defeat_count\", \"count\": ").append(count);
                if (!"==".equals(comparator)) {
                    sb.append(", \"comparator\": \"").append(comparator).append("\"");
                }
                sb.append(" }] }");
                if (i < guaranteedDropItems.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("      ]\n    }");
        }

        if (!weightedEntryItems.isEmpty()) {
            if (!firstPool) sb.append(",\n");
            firstPool = false;
            sb.append("    {\n");
            if (binomialRollsToggle.isSelected()) {
                double p = 0.35;
                try { p = Double.parseDouble(binomialPField.getText().trim()); }
                catch (NumberFormatException ignored) {}
                sb.append("      \"rolls\": { \"type\": \"minecraft:binomial\", \"n\": ")
                  .append(binomialNSpinner.getValue()).append(", \"p\": ").append(p).append(" },\n");
            } else {
                sb.append("      \"rolls\": ").append(weightedRollsSpinner.getValue()).append(",\n");
            }
            sb.append("      \"entries\": [\n");
            for (int i = 0; i < weightedEntryItems.size(); i++) {
                String entry = weightedEntryItems.get(i);
                String[] parts = entry.split(" \\| ");
                String type = parts[0].trim();
                String name = parts.length > 1 ? parts[1].trim() : "";
                int weight = 1;
                if (parts.length > 2 && parts[2].trim().startsWith("w=")) {
                    try { weight = Integer.parseInt(parts[2].trim().substring(2)); }
                    catch (NumberFormatException ignored) {}
                }
                String lvlRange = parts.length > 3 && parts[3].trim().startsWith("lvl=")
                        ? parts[3].trim().substring(4) : null;
                sb.append("        { \"type\": \"").append(type).append("\", \"name\": \"").append(name)
                  .append("\", \"weight\": ").append(weight);
                if (lvlRange != null) {
                    String[] lr = lvlRange.split("-");
                    int min = 1, max = 100;
                    try { min = Integer.parseInt(lr[0]); } catch (NumberFormatException ignored) {}
                    if (lr.length > 1) { try { max = Integer.parseInt(lr[1]); } catch (NumberFormatException ignored) {} }
                    sb.append(", \"conditions\": [{ \"condition\": \"rctmod:level_range\", \"range\": { \"min\": ")
                      .append(min).append(", \"max\": ").append(max).append(" } }]");
                }
                sb.append(" }");
                if (i < weightedEntryItems.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("      ]\n    }");
        }

        sb.append("\n  ]\n}");
        return sb.toString();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.showAndWait();
    }
}