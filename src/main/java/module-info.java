module com.example.evaluacion_javafx_citastecnicas {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.evaluacion_javafx_citastecnicas to javafx.fxml;
    opens com.example.evaluacion_javafx_citastecnicas.database to javafx.fxml;
    exports com.example.evaluacion_javafx_citastecnicas;
    exports com.example.evaluacion_javafx_citastecnicas.database;
}