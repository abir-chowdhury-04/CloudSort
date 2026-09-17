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

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        extensionColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        fileTableView.setItems(fileItems);
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
    protected void onScanClicked() {
        if (selectedFolder == null) {
            folderPathLabel.setText("Please select a folder first");
            return;
        }

        fileItems.clear();
        organizeButton.setDisable(true);

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
        });

        scanTask.setOnFailed(event -> {
            folderPathLabel.setText("Scan failed: " + scanTask.getException().getMessage());
        });

        Thread backgroundThread = new Thread(scanTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    @FXML
    protected void onOrganizeClicked() {
        if (fileItems.isEmpty()) {
            return;
        }

        File targetRoot = (destinationFolder != null) ? destinationFolder : selectedFolder;

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

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

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
                        Path destinationPath = categoryFolder.resolve(item.getName());

                        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
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
        });

        moveTask.setOnFailed(event -> {
            organizeButton.setDisable(false);
            folderPathLabel.setText("Organize failed: " + moveTask.getException().getMessage());
        });

        Thread backgroundThread = new Thread(moveTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
}