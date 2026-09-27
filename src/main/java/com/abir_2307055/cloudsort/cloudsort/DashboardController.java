package com.abir_2307055.cloudsort.cloudsort;

import com.abir_2307055.cloudsort.cloudsort.model.FileItem;
import com.abir_2307055.cloudsort.cloudsort.model.ScanSettings;
import com.abir_2307055.cloudsort.cloudsort.service.Categorizer;
import javafx.application.Platform;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.abir_2307055.cloudsort.cloudsort.repository.MoveHistoryDAO;
import java.sql.SQLException;
import com.abir_2307055.cloudsort.cloudsort.repository.SessionDAO;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @FXML
    protected void onExitClicked() {
        Platform.exit();
    }

    private File selectedFolder;
    private File destinationFolder;

    private final ObservableList<FileItem> fileItems = FXCollections.observableArrayList();

    private final Categorizer categorizer = Categorizer.withDefaultRules();

    private boolean operationInProgress = false;

    private final MoveHistoryDAO moveHistoryDAO = new MoveHistoryDAO();
    private final SessionDAO sessionDAO = new SessionDAO();

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private final ScanSettings scanSettings = new ScanSettings();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        extensionColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        fileTableView.setItems(fileItems);
        fileTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

        executorService.submit(scanTask);
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
    protected void onViewHistoryClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("history-view.fxml"));
            Scene historyScene = new Scene(loader.load(), 700, 400);

            HistoryController historyController = loader.getController();
            historyController.setScanSettings(scanSettings);

            Stage historyStage = new Stage();
            historyStage.setTitle("Move History");
            historyStage.setScene(historyScene);
            historyStage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open history window: " + e.getMessage());
            alert.showAndWait();
        }
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
            warnAlert.getDialogPane().setMinHeight(180);

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

                long sessionId;
                try {
                    sessionId = sessionDAO.insertSession(selectedFolder.getAbsolutePath(), targetRoot.getAbsolutePath());
                } catch (SQLException e) {
                    sessionId = -1;
                }

                for (FileItem item : fileItems) {
                    Path sourcePath = new File(selectedFolder, item.getName()).toPath();
                    Path categoryFolder = new File(targetRoot, item.getCategory()).toPath();
                    String originalPathText = sourcePath.toString();
                    String newPathText;

                    final long finalSessionId = sessionId;
                    final long[] insertedId = {-1};

                    try {
                        Files.createDirectories(categoryFolder);
                        Path destinationPath = resolveNonConflictingPath(categoryFolder, item.getName());
                        newPathText = destinationPath.toString();

                        Thread insertThread = new Thread(() -> {
                            try {
                                insertedId[0] = moveHistoryDAO.insertMove(finalSessionId, originalPathText, newPathText, item.getCategory());
                            } catch (SQLException e) {
                                System.err.println("Failed to log move: " + e.getMessage());
                            }
                        });
                        insertThread.setDaemon(true);
                        insertThread.start();
                        insertThread.join();

                        Files.move(sourcePath, destinationPath);
                        successCount++;

                        Thread updateThread = new Thread(() -> {
                            try {
                                if (insertedId[0] != -1) {
                                    moveHistoryDAO.updateStatus(insertedId[0], "moved");
                                }
                            } catch (SQLException e) {
                                System.err.println("Failed to update move status: " + e.getMessage());
                            }
                        });
                        updateThread.setDaemon(true);
                        updateThread.start();
                        updateThread.join();

                    } catch (Exception e) {
                        failureCount++;
                        if (insertedId[0] != -1) {
                            Thread failThread = new Thread(() -> {
                                try {
                                    moveHistoryDAO.updateStatus(insertedId[0], "failed");
                                } catch (SQLException ex) {
                                    System.err.println("Failed to update failure status: " + ex.getMessage());
                                }
                            });
                            failThread.setDaemon(true);
                            failThread.start();
                            try {
                                failThread.join();
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
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

        executorService.submit(moveTask);
    }

    @FXML
    protected void onSettingsClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("settings-view.fxml"));
            Scene settingsScene = new Scene(loader.load(), 450, 600);

            SettingsController settingsController = loader.getController();
            settingsController.setScanSettings(scanSettings);

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.setScene(settingsScene);
            settingsStage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open settings window: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void shutdownExecutor() {
        executorService.shutdown();
    }
}