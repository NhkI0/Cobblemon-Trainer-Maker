package com.dopamine.cobblemontrainermaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    private final ObservableList<String> bagDisplayItems = FXCollections.observableArrayList();

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
            showAlert(Alert.AlertType.INFORMATION, "Saved to " + file.toAbsolutePath());
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

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.showAndWait();
    }
}