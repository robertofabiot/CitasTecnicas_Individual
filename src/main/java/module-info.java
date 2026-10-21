module com.example.evaluacion_javafx_citastecnicas {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.evaluacion_javafx_citastecnicas to javafx.fxml;
    opens com.example.evaluacion_javafx_citastecnicas.database to javafx.fxml;
    opens com.example.evaluacion_javafx_citastecnicas.model to javafx.base, javafx.fxml;
    opens com.example.evaluacion_javafx_citastecnicas.dao to javafx.fxml;
    opens ni.edu.uam.practicahilos to javafx.fxml;

    exports com.example.evaluacion_javafx_citastecnicas;
    exports com.example.evaluacion_javafx_citastecnicas.database;
    exports com.example.evaluacion_javafx_citastecnicas.model;
    exports com.example.evaluacion_javafx_citastecnicas.dao;
    exports ni.edu.uam.practicahilos;
}
