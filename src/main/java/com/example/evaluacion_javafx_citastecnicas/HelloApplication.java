package com.example.evaluacion_javafx_citastecnicas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("inicio-view.fxml"));
        stage.setScene(new Scene(loader.load(), 1000, 780));
        stage.setTitle("TecnoAsistencia · Gestión de clientes y citas");
        stage.setMinWidth(900);
        stage.setMinHeight(740);
        stage.show();
    }
}
