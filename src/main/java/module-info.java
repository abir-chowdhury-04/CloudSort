module com.abir_2307055.cloudsort.cloudsort {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.abir_2307055.cloudsort.cloudsort to javafx.fxml;
    exports com.abir_2307055.cloudsort.cloudsort;
}