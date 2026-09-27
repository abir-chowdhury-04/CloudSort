module com.abir_2307055.cloudsort.cloudsort {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    opens com.abir_2307055.cloudsort.cloudsort to javafx.fxml;
    opens com.abir_2307055.cloudsort.cloudsort.model to javafx.base;

    exports com.abir_2307055.cloudsort.cloudsort;
}