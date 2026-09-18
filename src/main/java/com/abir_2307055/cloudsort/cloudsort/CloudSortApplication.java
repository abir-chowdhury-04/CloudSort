package com.abir_2307055.cloudsort.cloudsort;

import com.abir_2307055.cloudsort.cloudsort.service.SettingsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CloudSortApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CloudSortApplication.class.getResource("dashboard-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        SettingsManager settingsManager = new SettingsManager();
        boolean darkModeEnabled = settingsManager.loadDarkModePreference();

        String themeFile = darkModeEnabled ? "dark-theme.css" : "light-theme.css";
        scene.getStylesheets().add(CloudSortApplication.class.getResource(themeFile).toExternalForm());

        stage.setTitle("CloudSort");
        stage.setScene(scene);
        stage.show();

        DashboardController controller = fxmlLoader.getController();
        controller.setInitialDarkModeState(darkModeEnabled);
    }
}