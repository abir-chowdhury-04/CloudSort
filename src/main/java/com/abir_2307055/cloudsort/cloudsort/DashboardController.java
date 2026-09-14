package com.abir_2307055.cloudsort.cloudsort;

import com.abir_2307055.cloudsort.cloudsort.model.FileItem;
import com.abir_2307055.cloudsort.cloudsort.service.Categorizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.concurrent.Task;

import java.io.File;

public class DashboardController {

    @FXML
    private Label folderPathLabel;

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

    private File selectedFolder;

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
        }
    }

    @FXML
    protected void onScanClicked() {
        if (selectedFolder == null) {
            folderPathLabel.setText("Please select a folder first");
            return;
        }

        fileItems.clear();

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
        });

        scanTask.setOnFailed(event -> {
            folderPathLabel.setText("Scan failed: " + scanTask.getException().getMessage());
        });

        Thread backgroundThread = new Thread(scanTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
}