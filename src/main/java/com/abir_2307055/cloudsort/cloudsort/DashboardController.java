package com.abir_2307055.cloudsort.cloudsort;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public class DashboardController {

    @FXML
    private Label folderPathLabel;

    @FXML
    protected void onSelectFolderClicked(javafx.event.ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a folder to organize");

        Window ownerWindow = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(ownerWindow);

        if (selectedDirectory != null) {
            folderPathLabel.setText(selectedDirectory.getAbsolutePath());
        }
        // If the user cancels the dialog, selectedDirectory is null,
        // and we simply leave the label showing whatever it said before.
    }
}