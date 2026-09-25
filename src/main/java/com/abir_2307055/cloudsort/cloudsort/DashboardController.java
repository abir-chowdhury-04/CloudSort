package com.abir_2307055.cloudsort.cloudsort;

import com.abir_2307055.cloudsort.cloudsort.model.FileItem;
import com.abir_2307055.cloudsort.cloudsort.service.Categorizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.Scene;
import com.abir_2307055.cloudsort.cloudsort.service.SettingsManager;

import java.io.File;

public class DashboardController {

    @FXML
    private Label folderPathLabel;

    @FXML
    private Label destinationLabel;

    @FXML
    private TableView<FileItem> fileTableView;

    @FXML
    private TableColumn<FileItem, String> nameColumn;

    @FXML
    private TableColumn<FileItem, String> extensionColumn;

    @FXML
    private TableColumn<FileItem, String> categoryColumn;

    @FXML
    private TableColumn<FileItem, String> reasonColumn;

    @FXML
    private Button organizeButton;

    private File selectedFolder;
    private File destinationFolder;

    private final ObservableList<FileItem> fileItems = FXCollections.observableArrayList();

    private final Categorizer categorizer = Categorizer.withDefaultRules();

    private boolean operationInProgress = false;

    private boolean darkModeEnabled = false;

    private final SettingsManager settingsManager = new SettingsManager();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        extensionColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        fileTableView.setItems(fileItems);
        fileTableView.setRowFactory(tableView -> new javafx.scene.control.TableRow<FileItem>() {
            @Override
            protected void updateItem(FileItem item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll("category-documents", "category-images", "category-other");

                if (empty || item == null) {
                    return;
                }

                switch (item.getCategory()) {
                    case "Documents" -> getStyleClass().add("category-documents");
                    case "Images" -> getStyleClass().add("category-images");
                    default -> getStyleClass().add("category-other");
                }
            }
        });
    }

    public void setInitialDarkModeState(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }

    @FXML
    protected void onToggleThemeClicked(javafx.event.ActionEvent event) {
        Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
        scene.getStylesheets().clear();

        if (darkModeEnabled) {
            scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        }

        darkModeEnabled = !darkModeEnabled;
        settingsManager.saveDarkModePreference(darkModeEnabled);
    }

    @FXML
    protected void onSelectFolderClicked(javafx.event.ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a folder to organize");

        Window ownerWindow = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File chosenDirectory = directoryChooser.showDialog(ownerWindow);

        if (chosenDirectory != null) {
            selectedFolder = chosenDirectory;
            folderPathLabel.setText(selectedFolder.getAbsolutePath());
            organizeButton.setDisable(true);
        }
    }

    @FXML
    protected void onSelectDestinationClicked(javafx.event.ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a destination folder");

        Window ownerWindow = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File chosenDirectory = directoryChooser.showDialog(ownerWindow);

        if (chosenDirectory != null) {
            destinationFolder = chosenDirectory;
            destinationLabel.setText("Destination: " + destinationFolder.getAbsolutePath());
        }
    }

    @FXML
    protected void onResetDestinationClicked() {
        destinationFolder = null;
        destinationLabel.setText("Destination: same as source folder");
    }

    @FXML
    protected void onScanClicked() {
        if (operationInProgress) {
            return;
        }
        if (selectedFolder == null) {
            folderPathLabel.setText("Please select a folder first");
            return;
        }

        fileItems.clear();
        organizeButton.setDisable(true);
        operationInProgress = true;

        Task<ObservableList<FileItem>> scanTask = new Task<>() {
            @Override
            protected ObservableList<FileItem> call() {
                ObservableList<FileItem> scannedItems = FXCollections.observableArrayList();

                File[] files = selectedFolder.listFiles(File::isFile);
                if (files == null) {
                    return scannedItems;
                }

                for (File file : files) {
                    String name = file.getName();
                    String extension = "";
                    int dotIndex = name.lastIndexOf('.');
                    if (dotIndex > 0) {
                        extension = name.substring(dotIndex + 1);
                    }

                    String category = categorizer.getCategory(extension);
                    String reason = categorizer.getReason(extension);

                    scannedItems.add(new FileItem(name, extension, category, reason));
                }

                return scannedItems;
            }
        };

        scanTask.setOnSucceeded(event -> {
            fileItems.setAll(scanTask.getValue());
            organizeButton.setDisable(fileItems.isEmpty());
            operationInProgress = false;
        });

        scanTask.setOnFailed(event -> {
            folderPathLabel.setText("Scan failed: " + scanTask.getException().getMessage());
            operationInProgress = false;
        });

        Thread backgroundThread = new Thread(scanTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private boolean allFilesShareOneCategory(ObservableList<FileItem> items) {
        if (items.isEmpty()) {
            return false;
        }
        String firstCategory = items.get(0).getCategory();
        for (FileItem item : items) {
            if (!item.getCategory().equals(firstCategory)) {
                return false;
            }
        }
        return true;
    }

    private Path resolveNonConflictingPath(Path categoryFolder, String originalName) {
        Path candidate = categoryFolder.resolve(originalName);
        if (!Files.exists(candidate)) {
            return candidate;
        }

        String baseName = originalName;
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = originalName.substring(0, dotIndex);
            extension = originalName.substring(dotIndex);
        }

        int counter = 1;
        Path renamedCandidate;
        do {
            String newName = baseName + " (" + counter + ")" + extension;
            renamedCandidate = categoryFolder.resolve(newName);
            counter++;
        } while (Files.exists(renamedCandidate));

        return renamedCandidate;
    }

    private boolean isSameOrNestedPath(File folderA, File folderB) {
        Path pathA = folderA.toPath().toAbsolutePath().normalize();
        Path pathB = folderB.toPath().toAbsolutePath().normalize();
        return pathA.equals(pathB) || pathA.startsWith(pathB) || pathB.startsWith(pathA);
    }

    @FXML
    protected void onOrganizeClicked() {
        if (operationInProgress) {
            return;
        }
        if (fileItems.isEmpty()) {
            return;
        }

        File targetRoot = (destinationFolder != null) ? destinationFolder : selectedFolder;

        if (destinationFolder != null && isSameOrNestedPath(selectedFolder, destinationFolder)) {
            Alert overlapAlert = new Alert(Alert.AlertType.ERROR);
            overlapAlert.setTitle("Invalid Destination");
            overlapAlert.setHeaderText(null);
            overlapAlert.setContentText("The destination folder cannot be the same as, or inside/containing, "
                    + "the source folder. Please choose a different destination.");
            overlapAlert.showAndWait();
            return;
        }

        Map<String, Integer> countPerCategory = new HashMap<>();
        for (FileItem item : fileItems) {
            countPerCategory.merge(item.getCategory(), 1, Integer::sum);
        }

        StringBuilder summary = new StringBuilder();
        summary.append(fileItems.size()).append(" file(s) will be moved into:\n");
        for (Map.Entry<String, Integer> entry : countPerCategory.entrySet()) {
            summary.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" file(s)\n");
        }
        summary.append("\nDestination: ").append(targetRoot.getAbsolutePath());

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Organize");
        confirmAlert.setHeaderText("Move files now?");
        confirmAlert.setContentText(summary.toString());
        confirmAlert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        confirmAlert.setResizable(true);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        if (allFilesShareOneCategory(fileItems)) {
            String onlyCategory = fileItems.get(0).getCategory();

            Alert warnAlert = new Alert(Alert.AlertType.WARNING);
            warnAlert.setTitle("Possible Duplicate Organizing");
            warnAlert.setHeaderText(null);
            warnAlert.setContentText("All files in this folder already belong to one category ("
                    + onlyCategory + "). This folder may already be sorted.\n\n"
                    + "Proceed anyway?");
            warnAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);
            warnAlert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            warnAlert.setResizable(true);

            Optional<ButtonType> warnResult = warnAlert.showAndWait();
            if (warnResult.isEmpty() || warnResult.get() != ButtonType.YES) {
                return;
            }
        }

        operationInProgress = true;
        organizeButton.setDisable(true);

        Task<int[]> moveTask = new Task<>() {
            @Override
            protected int[] call() {
                int successCount = 0;
                int failureCount = 0;

                for (FileItem item : fileItems) {
                    try {
                        Path sourcePath = new File(selectedFolder, item.getName()).toPath();
                        Path categoryFolder = new File(targetRoot, item.getCategory()).toPath();
                        Files.createDirectories(categoryFolder);
                        Path destinationPath = resolveNonConflictingPath(categoryFolder, item.getName());

                        Files.move(sourcePath, destinationPath);
                        successCount++;
                    } catch (Exception e) {
                        failureCount++;
                    }
                }

                return new int[]{successCount, failureCount};
            }
        };

        moveTask.setOnSucceeded(event -> {
            int[] resultCounts = moveTask.getValue();
            Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
            resultAlert.setTitle("Organize Complete");
            resultAlert.setHeaderText(null);
            resultAlert.setContentText("Moved successfully: " + resultCounts[0]
                    + "\nFailed: " + resultCounts[1]);
            resultAlert.showAndWait();

            fileItems.clear();
            organizeButton.setDisable(true);
            operationInProgress = false;
        });

        moveTask.setOnFailed(event -> {
            organizeButton.setDisable(false);
            folderPathLabel.setText("Organize failed: " + moveTask.getException().getMessage());
            operationInProgress = false;
        });

        Thread backgroundThread = new Thread(moveTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
}