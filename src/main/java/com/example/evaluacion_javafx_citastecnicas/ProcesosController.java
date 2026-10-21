package com.example.evaluacion_javafx_citastecnicas;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador para la demostración y monitorización de procesos concurrentes en JavaFX.
 * Cumple con los objetivos de la Unidad III: Programación concurrente:
 * - Creación de hilos con Thread y Runnable.
 * - Ejecución de tareas en segundo plano sin bloquear el JavaFX Application Thread.
 * - Actualización segura de controles con Platform.runLater().
 * - Detención controlada mediante interrupt() e isInterrupted().
 * - Reto final: Monitor empresarial con dos hilos independientes y diferentes velocidades.
 */
public class ProcesosController extends MenuController {

    private static final DateTimeFormatter HORA_FORMATO = DateTimeFormatter.ofPattern("HH:mm:ss");

    // --- Controles de Clientes (100 registros) ---
    @FXML private Label lblEstadoClientes;
    @FXML private Label lblPorcentajeClientes;
    @FXML private ProgressBar barraProgresoClientes;
    @FXML private Button btnIniciarClientes;
    @FXML private Button btnDetenerClientes;
    @FXML private Button btnReiniciarClientes;

    // --- Controles de Citas Técnicas (50 registros) ---
    @FXML private Label lblEstadoCitas;
    @FXML private Label lblPorcentajeCitas;
    @FXML private ProgressBar barraProgresoCitas;
    @FXML private Button btnIniciarCitas;
    @FXML private Button btnDetenerCitas;
    @FXML private Button btnReiniciarCitas;

    // --- Controles Globales y Registro ---
    @FXML private Button btnIniciarTodos;
    @FXML private Button btnDetenerTodos;
    @FXML private Button btnReiniciarTodos;
    @FXML private Label lblHilosActivos;
    @FXML private TextArea txtLog;

    // --- Hilos independientes ---
    private Thread hiloClientes;
    private Thread hiloCitas;

    // Compatibilidad directa con los identificadores exactos de la práctica guiada
    @FXML private Label lblEstado;
    @FXML private Label lblPorcentaje;
    @FXML private ProgressBar barraProgreso;
    @FXML private Button btnIniciar;
    @FXML private Button btnDetener;
    @FXML private Button btnReiniciar;

    @FXML
    private void initialize() {
        actualizarEstadoHilos();
        log("Sistema de concurrencia listo. Seleccione un proceso para iniciar.");
    }

    // =========================================================================
    // PROCESO 1: CLIENTES (100 registros, 100 ms por registro)
    // =========================================================================

    @FXML
    public void iniciarProcesoClientes() {
        if (hiloClientes != null && hiloClientes.isAlive()) {
            return;
        }

        if (barraProgresoClientes != null) barraProgresoClientes.setProgress(0);
        if (lblPorcentajeClientes != null) lblPorcentajeClientes.setText("0%");
        if (lblEstadoClientes != null) lblEstadoClientes.setText("Procesando registros de clientes...");

        if (btnIniciarClientes != null) btnIniciarClientes.setDisable(true);
        if (btnDetenerClientes != null) btnDetenerClientes.setDisable(false);
        if (btnReiniciarClientes != null) btnReiniciarClientes.setDisable(true);

        if (barraProgreso != null) barraProgreso.setProgress(0);
        if (lblPorcentaje != null) lblPorcentaje.setText("0%");
        if (lblEstado != null) lblEstado.setText("Procesando registros...");
        if (btnIniciar != null) btnIniciar.setDisable(true);
        if (btnDetener != null) btnDetener.setDisable(false);
        if (btnReiniciar != null) btnReiniciar.setDisable(true);

        log("[Hilo-Clientes] Iniciando procesamiento de 100 registros...");

        Runnable tarea = () -> {
            try {
                for (int i = 1; i <= 100; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    Thread.sleep(100);

                    final int avance = i;
                    Platform.runLater(() -> {
                        double progreso = avance / 100.0;
                        if (barraProgresoClientes != null) barraProgresoClientes.setProgress(progreso);
                        if (lblPorcentajeClientes != null) lblPorcentajeClientes.setText(avance + "%");

                        if (barraProgreso != null) barraProgreso.setProgress(progreso);
                        if (lblPorcentaje != null) lblPorcentaje.setText(avance + "%");

                        if (avance % 25 == 0 || avance == 100) {
                            log("[Hilo-Clientes] Progreso: " + avance + "/100 registros (" + avance + "%)");
                        }
                    });
                }

                Platform.runLater(() -> {
                    if (lblEstadoClientes != null) lblEstadoClientes.setText("Proceso finalizado");
                    if (lblEstado != null) lblEstado.setText("Proceso finalizado");
                    habilitarBotonesClientes();
                    actualizarEstadoHilos();
                    log("[Hilo-Clientes] ¡Proceso completado exitosamente al 100%!");
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    if (lblEstadoClientes != null) lblEstadoClientes.setText("Proceso detenido");
                    if (lblEstado != null) lblEstado.setText("Proceso detenido");
                    habilitarBotonesClientes();
                    actualizarEstadoHilos();
                    log("[Hilo-Clientes] Proceso detenido por solicitud del usuario.");
                });
            }
        };

        hiloClientes = new Thread(tarea);
        hiloClientes.setName("Hilo-Procesamiento-Clientes");
        hiloClientes.setDaemon(true);
        hiloClientes.start();
        actualizarEstadoHilos();
    }

    @FXML
    public void detenerProcesoClientes() {
        if (hiloClientes != null && hiloClientes.isAlive()) {
            if (btnDetenerClientes != null) btnDetenerClientes.setDisable(true);
            if (btnDetener != null) btnDetener.setDisable(true);
            if (lblEstadoClientes != null) lblEstadoClientes.setText("Solicitando detención...");
            if (lblEstado != null) lblEstado.setText("Solicitando detención...");
            log("[Hilo-Clientes] Enviando señal de interrupción...");
            hiloClientes.interrupt();
        }
    }

    @FXML
    public void reiniciarProcesoClientes() {
        if (hiloClientes != null && hiloClientes.isAlive()) {
            return;
        }

        if (barraProgresoClientes != null) barraProgresoClientes.setProgress(0);
        if (lblPorcentajeClientes != null) lblPorcentajeClientes.setText("0%");
        if (lblEstadoClientes != null) lblEstadoClientes.setText("Esperando inicio");

        if (barraProgreso != null) barraProgreso.setProgress(0);
        if (lblPorcentaje != null) lblPorcentaje.setText("0%");
        if (lblEstado != null) lblEstado.setText("Esperando inicio");

        habilitarBotonesClientes();
        log("[Hilo-Clientes] Estado reiniciado a 0%.");
    }

    private void habilitarBotonesClientes() {
        if (btnIniciarClientes != null) btnIniciarClientes.setDisable(false);
        if (btnDetenerClientes != null) btnDetenerClientes.setDisable(true);
        if (btnReiniciarClientes != null) btnReiniciarClientes.setDisable(false);

        if (btnIniciar != null) btnIniciar.setDisable(false);
        if (btnDetener != null) btnDetener.setDisable(true);
        if (btnReiniciar != null) btnReiniciar.setDisable(false);
    }

    // =========================================================================
    // PROCESO 2: CITAS TÉCNICAS (50 registros, 150 ms por registro)
    // =========================================================================

    @FXML
    public void iniciarProcesoCitas() {
        if (hiloCitas != null && hiloCitas.isAlive()) {
            return;
        }

        if (barraProgresoCitas != null) barraProgresoCitas.setProgress(0);
        if (lblPorcentajeCitas != null) lblPorcentajeCitas.setText("0%");
        if (lblEstadoCitas != null) lblEstadoCitas.setText("Procesando citas técnicas...");

        if (btnIniciarCitas != null) btnIniciarCitas.setDisable(true);
        if (btnDetenerCitas != null) btnDetenerCitas.setDisable(false);
        if (btnReiniciarCitas != null) btnReiniciarCitas.setDisable(true);

        log("[Hilo-Citas] Iniciando procesamiento de 50 citas técnicas...");

        Runnable tarea = () -> {
            try {
                for (int i = 1; i <= 50; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    Thread.sleep(150);

                    final int avance = i;
                    Platform.runLater(() -> {
                        double progreso = avance / 50.0;
                        int porcentaje = (int) Math.round(progreso * 100);
                        if (barraProgresoCitas != null) barraProgresoCitas.setProgress(progreso);
                        if (lblPorcentajeCitas != null) lblPorcentajeCitas.setText(porcentaje + "%");

                        if (avance % 10 == 0 || avance == 50) {
                            log("[Hilo-Citas] Progreso: " + avance + "/50 citas (" + porcentaje + "%)");
                        }
                    });
                }

                Platform.runLater(() -> {
                    if (lblEstadoCitas != null) lblEstadoCitas.setText("Proceso finalizado");
                    habilitarBotonesCitas();
                    actualizarEstadoHilos();
                    log("[Hilo-Citas] ¡Proceso de citas completado exitosamente!");
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    if (lblEstadoCitas != null) lblEstadoCitas.setText("Proceso detenido");
                    habilitarBotonesCitas();
                    actualizarEstadoHilos();
                    log("[Hilo-Citas] Proceso detenido por solicitud del usuario.");
                });
            }
        };

        hiloCitas = new Thread(tarea);
        hiloCitas.setName("Hilo-Procesamiento-Citas");
        hiloCitas.setDaemon(true);
        hiloCitas.start();
        actualizarEstadoHilos();
    }

    @FXML
    public void detenerProcesoCitas() {
        if (hiloCitas != null && hiloCitas.isAlive()) {
            if (btnDetenerCitas != null) btnDetenerCitas.setDisable(true);
            if (lblEstadoCitas != null) lblEstadoCitas.setText("Solicitando detención...");
            log("[Hilo-Citas] Enviando señal de interrupción...");
            hiloCitas.interrupt();
        }
    }

    @FXML
    public void reiniciarProcesoCitas() {
        if (hiloCitas != null && hiloCitas.isAlive()) {
            return;
        }

        if (barraProgresoCitas != null) barraProgresoCitas.setProgress(0);
        if (lblPorcentajeCitas != null) lblPorcentajeCitas.setText("0%");
        if (lblEstadoCitas != null) lblEstadoCitas.setText("Esperando inicio");

        habilitarBotonesCitas();
        log("[Hilo-Citas] Estado reiniciado a 0%.");
    }

    private void habilitarBotonesCitas() {
        if (btnIniciarCitas != null) btnIniciarCitas.setDisable(false);
        if (btnDetenerCitas != null) btnDetenerCitas.setDisable(true);
        if (btnReiniciarCitas != null) btnReiniciarCitas.setDisable(false);
    }

    // =========================================================================
    // CONTROL GLOBAL CONCURRENTE (Ambos hilos simultáneamente)
    // =========================================================================

    @FXML
    public void iniciarTodos() {
        log("[Control-Global] Iniciando ejecución simultánea de ambos hilos...");
        iniciarProcesoClientes();
        iniciarProcesoCitas();
    }

    @FXML
    public void detenerTodos() {
        log("[Control-Global] Solicitando detención simultánea de todos los hilos...");
        detenerProcesoClientes();
        detenerProcesoCitas();
    }

    @FXML
    public void reiniciarTodos() {
        log("[Control-Global] Reiniciando controles de todos los hilos...");
        reiniciarProcesoClientes();
        reiniciarProcesoCitas();
    }

    // =========================================================================
    // DEMOSTRACIÓN DEL PASO 6: Identificar el bloqueo de la interfaz
    // =========================================================================

    @FXML
    public void demostrarBloqueo() {
        Alert info = new Alert(Alert.AlertType.CONFIRMATION);
        info.setTitle("Demostración didáctica: Bloqueo de UI");
        info.setHeaderText("¿Por qué no ejecutar procesos prolongados en el hilo de JavaFX?");
        info.setContentText(
                "En JavaFX, el 'JavaFX Application Thread' se encarga de dibujar la interfaz y recibir eventos.\n\n" +
                "Si un botón ejecuta 'Thread.sleep(2000)' directamente en su evento sin un hilo secundario:\n" +
                "• La ventana se congela completamente durante ese tiempo.\n" +
                "• No responde a clics, no se puede redimensionar ni mover.\n\n" +
                "¿Deseas simular un bloqueo intencional de 2 segundos para comprobarlo?"
        );
        ButtonType btnBloquear = new ButtonType("Simular bloqueo breve (2s)");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        info.getButtonTypes().setAll(btnBloquear, btnCancelar);

        if (info.showAndWait().orElse(btnCancelar) == btnBloquear) {
            log("[Demostración] Congelando intencionalmente el JavaFX Thread por 2 segundos...");
            try {
                // Paso 6 de la práctica guiada
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log("[Demostración] El JavaFX Application Thread ha vuelto a responder.");
        }
    }

    // =========================================================================
    // UTILIDADES: LOG, ESTADO Y LIMPIEZA
    // =========================================================================

    private void log(String mensaje) {
        String hora = LocalTime.now().format(HORA_FORMATO);
        String linea = "[" + hora + "] " + mensaje + "\n";
        if (Platform.isFxApplicationThread()) {
            if (txtLog != null) txtLog.appendText(linea);
        } else {
            Platform.runLater(() -> {
                if (txtLog != null) txtLog.appendText(linea);
            });
        }
    }

    @FXML
    public void limpiarLog() {
        if (txtLog != null) txtLog.clear();
    }

    private void actualizarEstadoHilos() {
        int activos = 0;
        StringBuilder sb = new StringBuilder();
        if (hiloClientes != null && hiloClientes.isAlive()) {
            activos++;
            sb.append("Clientes");
        }
        if (hiloCitas != null && hiloCitas.isAlive()) {
            activos++;
            if (sb.length() > 0) sb.append(", ");
            sb.append("Citas");
        }

        final int totalActivos = activos;
        final String nombres = sb.length() > 0 ? " (" + sb + ")" : "";
        Platform.runLater(() -> {
            if (lblHilosActivos != null) {
                lblHilosActivos.setText("Hilos activos: " + totalActivos + nombres);
            }
            if (btnIniciarTodos != null) {
                btnIniciarTodos.setDisable(totalActivos == 2);
            }
            if (btnDetenerTodos != null) {
                btnDetenerTodos.setDisable(totalActivos == 0);
            }
        });
    }

    // Compatibilidad exacta con el método de la práctica guiada
    @FXML
    public void iniciarProceso() {
        iniciarProcesoClientes();
    }

    @FXML
    public void detenerProceso() {
        detenerProcesoClientes();
    }

    @FXML
    public void reiniciarProceso() {
        reiniciarProcesoClientes();
    }

    @Override
    public void detenerAlCerrar() {
        if (hiloClientes != null && hiloClientes.isAlive()) {
            hiloClientes.interrupt();
        }
        if (hiloCitas != null && hiloCitas.isAlive()) {
            hiloCitas.interrupt();
        }
    }
}
