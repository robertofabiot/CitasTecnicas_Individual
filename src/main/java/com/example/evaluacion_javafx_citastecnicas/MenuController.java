package com.example.evaluacion_javafx_citastecnicas;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class MenuController {
    @FXML protected BorderPane root;
    private static MenuController controladorActivo;

    @FXML protected void irInicio() { navegar("inicio-view.fxml"); }
    @FXML protected void irClientes() { navegar("clientes-view.fxml"); }
    @FXML protected void irCitas() { navegar("citas-view.fxml"); }
    @FXML protected void irProcesos() { navegar("procesos-view.fxml"); }

    private void navegar(String vista) {
        try {
            detenerAlCerrar();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(vista));
            Parent contenido = loader.load();
            root.getScene().setRoot(contenido);
            MenuController controller = loader.getController();
            controladorActivo = controller;
            controller.cargar();
        } catch (IOException e) {
            Ui.error(root, "No se pudo abrir la pantalla. Revise los archivos FXML.");
        }
    }

    public static void detenerControladorActivo() {
        if (controladorActivo != null) {
            controladorActivo.detenerAlCerrar();
        }
    }

    public void cargar() { }
    public void detenerAlCerrar() { }
}
