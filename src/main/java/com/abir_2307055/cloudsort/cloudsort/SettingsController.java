package com.abir_2307055.cloudsort.cloudsort;

import com.abir_2307055.cloudsort.cloudsort.model.ScanSettings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class SettingsController {

    @FXML private CheckBox includeHiddenCheckBox;
    @FXML private CheckBox skipEmptyCheckBox;
    @FXML private CheckBox caseSensitiveCheckBox;
    @FXML private ComboBox<String> sortOrderComboBox;
    @FXML private RadioButton autoRenameRadio;
    @FXML private RadioButton skipExistingRadio;
    @FXML private RadioButton overwriteRadio;
    @FXML private TextField customCategoryField;
    @FXML private Spinner<Integer> maxHistorySpinner;
    @FXML private PasswordField passphraseField;
    @FXML private ProgressBar sampleProgressBar;

    private ScanSettings scanSettings;
    private ToggleGroup conflictToggleGroup;

    @FXML
    public void initialize() {
        sortOrderComboBox.getItems().addAll("Name", "Category", "Extension");
        sortOrderComboBox.setValue("Name");

        conflictToggleGroup = new ToggleGroup();
        autoRenameRadio.setToggleGroup(conflictToggleGroup);
        skipExistingRadio.setToggleGroup(conflictToggleGroup);
        overwriteRadio.setToggleGroup(conflictToggleGroup);
        autoRenameRadio.setSelected(true);

        maxHistorySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 50));

        customCategoryField.setOnAction(event -> {
            String newCategory = customCategoryField.getText().trim();
            if (!newCategory.isEmpty()) {
                System.out.println("Custom category added: " + newCategory);
                customCategoryField.clear();
            }
        });
    }

    public void setScanSettings(ScanSettings settings) {
        this.scanSettings = settings;
        includeHiddenCheckBox.setSelected(settings.isIncludeHiddenFiles());
        skipEmptyCheckBox.setSelected(settings.isSkipEmptyFiles());
        caseSensitiveCheckBox.setSelected(settings.isCaseSensitiveMatching());
        sortOrderComboBox.setValue(settings.getSortOrder());
        maxHistorySpinner.getValueFactory().setValue(settings.getMaxHistoryResults());

        switch (settings.getConflictStrategy()) {
            case "Skip existing" -> skipExistingRadio.setSelected(true);
            case "Overwrite" -> overwriteRadio.setSelected(true);
            default -> autoRenameRadio.setSelected(true);
        }
    }

    @FXML
    protected void onSaveSettingsClicked() {
        if (scanSettings == null) {
            return;
        }
        scanSettings.setIncludeHiddenFiles(includeHiddenCheckBox.isSelected());
        scanSettings.setSkipEmptyFiles(skipEmptyCheckBox.isSelected());
        scanSettings.setCaseSensitiveMatching(caseSensitiveCheckBox.isSelected());
        scanSettings.setSortOrder(sortOrderComboBox.getValue());
        scanSettings.setMaxHistoryResults(maxHistorySpinner.getValue());
        scanSettings.setConfirmationPassphrase(passphraseField.getText());

        if (autoRenameRadio.isSelected()) {
            scanSettings.setConflictStrategy("Auto-rename");
        } else if (skipExistingRadio.isSelected()) {
            scanSettings.setConflictStrategy("Skip existing");
        } else {
            scanSettings.setConflictStrategy("Overwrite");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your settings have been saved for this session.");
        alert.showAndWait();
    }

    @FXML
    protected void onSimulateProgressClicked() {
        sampleProgressBar.setProgress(0);
        Timeline timeline = new Timeline();
        for (int i = 1; i <= 10; i++) {
            double progressValue = i / 10.0;
            KeyFrame frame = new KeyFrame(Duration.millis(i * 200),
                    event -> sampleProgressBar.setProgress(progressValue));
            timeline.getKeyFrames().add(frame);
        }
        timeline.play();
    }
}