module com.example.evaluacion_javafx_citastecnicas {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.evaluacion_javafx_citastecnicas to javafx.fxml;
    exports com.example.evaluacion_javafx_citastecnicas;
}