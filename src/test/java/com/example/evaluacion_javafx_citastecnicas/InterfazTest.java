package com.example.evaluacion_javafx_citastecnicas;

import com.example.evaluacion_javafx_citastecnicas.model.CitaTecnica;
import com.example.evaluacion_javafx_citastecnicas.model.Cliente;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class InterfazTest {
    @BeforeAll static void iniciarJavaFx() throws Exception {
        FutureTask<Void> listo = new FutureTask<>(() -> { Platform.setImplicitExit(false); return null; });
        boolean linuxSinPantalla = System.getProperty("os.name").toLowerCase().contains("linux")
                && System.getenv("DISPLAY") == null;
        Assumptions.assumeFalse(linuxSinPantalla, "Las pruebas JavaFX requieren una pantalla o xvfb-run.");
        Platform.startup(listo);
        listo.get(15, TimeUnit.SECONDS);
    }

    private void enFx(Runnable accion) throws Exception {
        FutureTask<Void> tarea = new FutureTask<>(accion, null);
        Platform.runLater(tarea);
        tarea.get(15, TimeUnit.SECONDS);
    }

    private FXMLLoader cargar(String archivo) {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(archivo));
        try { loader.load(); } catch (Exception e) { throw new AssertionError(archivo, e); }
        return loader;
    }

    private Stage mostrar(Parent root) {
        Stage stage = new Stage(); stage.setScene(new Scene(root, 1000, 780)); stage.show();
        root.applyCss(); root.layout(); return stage;
    }

    private void enter(Node node) {
        Event.fireEvent(node, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
        Node destino = node.getScene().getFocusOwner();
        Event.fireEvent(destino, new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.ENTER, false, false, false, false));
    }

    @Test void cargaLasCincoVistasSinCssYDesactivaElMenuActual() throws Exception {
        enFx(() -> {
            for (String vista : List.of("inicio", "clientes", "citas")) {
                Parent root = cargar(vista + "-view.fxml").getRoot();
                Stage stage = mostrar(root);
                try {
                    assertTrue(root.getStylesheets().isEmpty());
                    ToolBar barra = (ToolBar) root.lookup(".tool-bar");
                    for (Node item : barra.getItems()) {
                        if (item instanceof Button boton)
                            assertEquals(boton.getText().equalsIgnoreCase(vista), boton.isDisabled());
                    }
                    captura(root, vista);
                } finally { stage.close(); }
            }
            assertNotNull(cargar("cliente-form.fxml").getController());
            assertNotNull(cargar("cita-form.fxml").getController());
        });
    }

    @Test void clienteNormalizaNombresYEnterRecorreTodosLosCamposSinGuardar() throws Exception {
        enFx(() -> {
            FXMLLoader loader = cargar("cliente-form.fxml");
            Parent root = loader.getRoot();
            ClienteFormController controller = loader.getController();
            controller.configurar(null, c -> fail("No debe guardar al avanzar entre campos"));
            Stage stage = mostrar(root);
            try {
                ((TextField) root.lookup("#nombres")).setText("  Ana María  ");
                ((TextField) root.lookup("#apellidos")).setText("  Pérez López  ");
                ((TextField) root.lookup("#telefono")).setText("8888-1111");
                ((TextField) root.lookup("#correo")).setText("  Ana.Perez+soporte@dominio.com  ");
                Cliente c = controller.leer();
                assertEquals("Ana María", c.getNombres());
                assertEquals("Pérez López", c.getApellidos());
                assertEquals("ana.perez+soporte@dominio.com", c.getCorreo());
                assertEquals("Ana María Pérez López", c.getNombreCompleto());
                verificarFlujo(root, "nombres", "apellidos", "telefono", "correo", "activo", "guardar");
                controller.limpiar();
                assertTrue(((TextField) root.lookup("#nombres")).getText().isEmpty());
            } finally { stage.close(); }
        });
    }

    @SuppressWarnings("unchecked")
    @Test void citaValidaFechasHorasYSelectoresYFlujoEnter() throws Exception {
        enFx(() -> {
            FXMLLoader loader = cargar("cita-form.fxml");
            Parent root = loader.getRoot();
            CitaFormController controller = loader.getController();
            Stage stage = mostrar(root);
            try {
                Cliente c = new Cliente(7, "Ana", "Pérez", "8888-1111", "ana@test.com", true);
                controller.clientes(List.of(c));
                ComboBox<Cliente> clientes = (ComboBox<Cliente>) root.lookup("#cliente");
                clientes.setValue(c);
                ComboBox<String> servicio = (ComboBox<String>) root.lookup("#servicio");
                assertFalse(servicio.isEditable());
                servicio.setValue(CitaFormController.SERVICIOS.getFirst());
                DatePicker fecha = (DatePicker) root.lookup("#fecha");
                TextField hora = (TextField) root.lookup("#hora");
                ((TextArea) root.lookup("#descripcion")).setText("  Revisar equipo  ");
                fecha.setValue(LocalDate.now()); hora.setText("14:30");
                assertEquals("Revisar equipo", controller.leer().getDescripcion());
                verificarFlujo(root, "cliente", "fecha", "hora", "servicio", "descripcion", "estado", "guardar");
                fecha.setValue(LocalDate.now().minusDays(1));
                assertThrows(IllegalArgumentException.class, controller::leer);
                fecha.setValue(LocalDate.now().plusDays(1)); hora.setText("24:00");
                assertThrows(IllegalArgumentException.class, controller::leer);
                hora.setText("09:30"); fecha.getEditor().setText("31/02/2027");
                assertThrows(IllegalArgumentException.class, controller::leer);
                fecha.setValue(LocalDate.now());
                servicio.setValue("Servicio inventado");
                assertThrows(IllegalArgumentException.class, controller::leer);
            } finally { stage.close(); }
        });
    }

    @SuppressWarnings("unchecked")
    @Test void menuContextualTieneEditarDetallesYEliminarYLaEdicionEsIndependiente() throws Exception {
        enFx(() -> {
            Parent root = cargar("clientes-view.fxml").getRoot();
            Stage stage = mostrar(root);
            try {
                TableView<Cliente> tabla = (TableView<Cliente>) root.lookup("#tabla");
                Cliente original = new Cliente(8, "Luis", "López", "7777", "luis@test.com", true);
                tabla.getItems().add(original);
                tabla.getSelectionModel().select(original);
                root.applyCss(); root.layout();
                assertFalse(root.lookup("#editar").isDisabled());
                TableRow<Cliente> fila = (TableRow<Cliente>) tabla.lookupAll(".table-row-cell").stream()
                        .filter(n -> n instanceof TableRow<?> row && !row.isEmpty()).findFirst().orElseThrow();
                assertEquals(java.util.Arrays.asList("Ver detalles", "Editar", null, "Eliminar"),
                        fila.getContextMenu().getItems().stream().map(MenuItem::getText).toList());
                fila.getContextMenu().getItems().get(1).fire();
                Stage popup = (Stage) javafx.stage.Window.getWindows().stream()
                        .filter(w -> w instanceof Stage s && s.getOwner() == stage).findFirst().orElseThrow();
                assertEquals(javafx.stage.Modality.WINDOW_MODAL, popup.getModality());
                assertEquals("Luis", ((TextField) popup.getScene().lookup("#nombres")).getText());
                ((TextField) popup.getScene().lookup("#nombres")).setText("Cambio sin guardar");
                assertEquals("Luis", original.getNombres());
                captura(popup.getScene().getRoot(), "editar-cliente");
                popup.close();
            } finally { stage.close(); }
        });
    }

    @Test void popupCitaConservaFechaHistoricaYServicioExistenteSinModificarElOriginal() throws Exception {
        enFx(() -> {
            Stage principal = mostrar(cargar("inicio-view.fxml").getRoot());
            try {
                EditorWindow<CitaFormController> editor = new EditorWindow<>(principal, "cita-form.fxml", "Actualizar cita");
                Cliente cliente = new Cliente(3, "Ana", "Pérez", "8888", "ana@test.com", true);
                CitaTecnica original = new CitaTecnica(9, 3, "Ana Pérez", LocalDate.now().minusDays(3),
                        LocalTime.of(10, 30), "Servicio histórico", "Equipo", "Pendiente");
                editor.controller.clientes(List.of(cliente));
                editor.controller.configurar(original, c -> { });
                editor.mostrar();
                try {
                    assertEquals(original.getFecha(), editor.controller.leer().getFecha());
                    assertEquals("Servicio histórico", editor.controller.leer().getTipoServicio());
                    ((TextField) editor.root.lookup("#hora")).setText("11:30");
                    assertEquals(LocalTime.of(11, 30), editor.controller.leer().getHora());
                    assertEquals(LocalTime.of(10, 30), original.getHora());
                    ((DatePicker) editor.root.lookup("#fecha")).setValue(LocalDate.now().minusDays(1));
                    assertThrows(IllegalArgumentException.class, editor.controller::leer);
                    captura(editor.root, "editar-cita");
                } finally { editor.cerrar(); }
            } catch (java.io.IOException e) { throw new AssertionError(e); }
            finally { principal.close(); }
        });
    }

    @Test void enterConfirmaOpcionDelComboYAvanza() throws Exception {
        enFx(() -> {
            Parent root = cargar("cita-form.fxml").getRoot();
            Stage stage = mostrar(root);
            try {
                ComboBox<?> servicio = (ComboBox<?>) root.lookup("#servicio");
                servicio.requestFocus(); servicio.show();
                servicio.getSelectionModel().select(1);
                enter(servicio);
                assertFalse(servicio.isShowing());
                assertEquals(CitaFormController.SERVICIOS.get(1), servicio.getValue());
                assertSame(root.lookup("#descripcion"), root.getScene().getFocusOwner());
            } finally { stage.close(); }
        });
    }

    private void verificarFlujo(Parent root, String... ids) {
        for (int i = 0; i < ids.length - 1; i++) {
            Node actual = root.lookup("#" + ids[i]); actual.requestFocus(); enter(actual);
            assertSame(root.lookup("#" + ids[i + 1]), root.getScene().getFocusOwner(), ids[i]);
        }
    }

    private void captura(Parent root, String nombre) {
        try {
            root.applyCss(); root.layout();
            WritableImage imagen = root.snapshot(null, null);
            int ancho = (int) imagen.getWidth(), alto = (int) imagen.getHeight();
            Path dir = Path.of("target", "ui-capturas"); Files.createDirectories(dir);
            try (OutputStream out = Files.newOutputStream(dir.resolve(nombre + ".ppm"))) {
                out.write(("P6\n" + ancho + " " + alto + "\n255\n").getBytes());
                for (int y = 0; y < alto; y++) for (int x = 0; x < ancho; x++) {
                    int color = imagen.getPixelReader().getArgb(x, y);
                    out.write((color >> 16) & 255); out.write((color >> 8) & 255); out.write(color & 255);
                }
            }
        } catch (Exception e) { throw new AssertionError(e); }
    }
}
