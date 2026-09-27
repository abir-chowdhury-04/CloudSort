package com.abir_2307055.cloudsort.cloudsort;

import com.abir_2307055.cloudsort.cloudsort.model.MoveRecord;
import com.abir_2307055.cloudsort.cloudsort.model.ScanSettings;
import com.abir_2307055.cloudsort.cloudsort.repository.MoveHistoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextInputDialog;

import java.sql.SQLException;
import java.util.Optional;

public class HistoryController {

    @FXML
    private TableView<MoveRecord> historyTableView;

    @FXML
    private TableColumn<MoveRecord, Long> idColumn;

    @FXML
    private TableColumn<MoveRecord, Long> sessionIdColumn;

    @FXML
    private TableColumn<MoveRecord, String> originalPathColumn;

    @FXML
    private TableColumn<MoveRecord, String> newPathColumn;

    @FXML
    private TableColumn<MoveRecord, String> categoryColumn;

    @FXML
    private TableColumn<MoveRecord, String> movedAtColumn;

    @FXML
    private TableColumn<MoveRecord, String> statusColumn;

    private final ObservableList<MoveRecord> records = FXCollections.observableArrayList();

    private final MoveHistoryDAO moveHistoryDAO = new MoveHistoryDAO();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sessionIdColumn.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        originalPathColumn.setCellValueFactory(new PropertyValueFactory<>("originalPath"));
        newPathColumn.setCellValueFactory(new PropertyValueFactory<>("newPath"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        movedAtColumn.setCellValueFactory(new PropertyValueFactory<>("movedAt"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        historyTableView.setItems(records);
        loadHistory();
    }

    @FXML
    protected void onRefreshClicked() {
        loadHistory();
    }

    private void loadHistory() {
        try {
            records.setAll(moveHistoryDAO.getAllMovesWithSessionInfo());
        } catch (SQLException e) {
            showError("Could not load history: " + e.getMessage());
        }
    }

    @FXML
    protected void onClearHistoryClicked() {
        if (scanSettings != null && scanSettings.getConfirmationPassphrase() != null
                && !scanSettings.getConfirmationPassphrase().isEmpty()) {

            TextInputDialog passphraseDialog = new TextInputDialog();
            passphraseDialog.setTitle("Passphrase Required");
            passphraseDialog.setHeaderText(null);
            passphraseDialog.setContentText("Enter the passphrase to clear history:");

            Optional<String> enteredPassphrase = passphraseDialog.showAndWait();

            if (enteredPassphrase.isEmpty() || !enteredPassphrase.get().equals(scanSettings.getConfirmationPassphrase())) {
                Alert wrongPassAlert = new Alert(Alert.AlertType.ERROR);
                wrongPassAlert.setTitle("Incorrect Passphrase");
                wrongPassAlert.setHeaderText(null);
                wrongPassAlert.setContentText("The passphrase was incorrect. History was not cleared.");
                wrongPassAlert.showAndWait();
                return;
            }
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete all move history? This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Clear History");
        confirm.setHeaderText(null);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                moveHistoryDAO.clearHistory();
                loadHistory();
            } catch (SQLException e) {
                showError("Could not clear history: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ScanSettings scanSettings;

    public void setScanSettings(ScanSettings settings) {
        this.scanSettings = settings;
    }
}