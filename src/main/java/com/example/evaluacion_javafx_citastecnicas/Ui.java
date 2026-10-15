package com.example.evaluacion_javafx_citastecnicas;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Window;

/** Utilidades compartidas: teclado, diálogos y JDBC fuera del hilo de JavaFX. */
public final class Ui {
    private Ui() { }
    public static Window ventana(Node node) {
        return node.getScene() == null ? null : node.getScene().getWindow();
    }

    private static Alert alerta(Node node, Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        if (ventana(node) != null) alert.initOwner(ventana(node));
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.getDialogPane().setMinWidth(460);
        return alert;
    }

    public static void error(Node node, String mensaje) {
        alerta(node, Alert.AlertType.ERROR, "No se pudo completar la operación", mensaje).showAndWait();
    }

    public static void informar(Node node, String mensaje) {
        alerta(node, Alert.AlertType.INFORMATION, "Operación completada", mensaje).showAndWait();
    }

    public static boolean confirmar(Node node, String mensaje) {
        Alert alert = alerta(node, Alert.AlertType.CONFIRMATION, "Confirmar operación", mensaje);
        ButtonType aceptar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(aceptar, cancelar);
        ((Button) alert.getDialogPane().lookupButton(aceptar)).setDefaultButton(false);
        ((Button) alert.getDialogPane().lookupButton(cancelar)).setDefaultButton(true);
        return alert.showAndWait().orElse(cancelar) == aceptar;
    }

    public static void detalles(Node node, String titulo, String contenido) {
        Alert alert = alerta(node, Alert.AlertType.INFORMATION, titulo, "");
        TextArea texto = new TextArea(contenido);
        texto.setEditable(false);
        texto.setWrapText(true);
        texto.setPrefColumnCount(48);
        texto.setPrefRowCount(14);
        alert.getDialogPane().setContent(texto);
        alert.setResizable(true);
        alert.showAndWait();
    }

}
