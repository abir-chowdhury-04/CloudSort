package com.abir_2307055.cloudsort.cloudsort;

import com.abir_2307055.cloudsort.cloudsort.repository.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CloudSortApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Database.initializeDatabase();

        FXMLLoader fxmlLoader = new FXMLLoader(CloudSortApplication.class.getResource("dashboard-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("CloudSort");
        stage.setScene(scene);
        stage.show();
    }
}