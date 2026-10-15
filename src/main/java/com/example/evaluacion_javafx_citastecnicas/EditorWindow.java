package com.example.evaluacion_javafx_citastecnicas;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;

/** Ventana modal independiente de los tres menús principales. */
public final class EditorWindow<C> {
    public final C controller;
    public final VBox root;
    private final Stage stage = new Stage();

    public EditorWindow(Window owner, String archivo, String titulo) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(archivo));
        Parent formulario = loader.load();
        controller = loader.getController();
        Button cancelar = new Button("Cancelar");
        cancelar.setCancelButton(true);
        cancelar.setOnAction(e -> stage.close());
        HBox pie = new HBox(cancelar);
        pie.setAlignment(Pos.CENTER_RIGHT);
        pie.setPadding(new Insets(0, 24, 20, 24));
        root = new VBox(formulario, pie);
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(titulo);
        stage.setScene(new Scene(root, 640, -1));
        stage.setMinWidth(580);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> { if (root.isDisabled()) e.consume(); });
    }

    public void mostrar() { stage.show(); }
    public void cerrar() { stage.close(); }
}
