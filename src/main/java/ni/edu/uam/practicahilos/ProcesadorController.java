package ni.edu.uam.practicahilos;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * Práctica Guiada: Hilos en JavaFX
 * Programación de Aplicaciones de Escritorio - Universidad Americana (UAM)
 * Unidad III: Programación concurrente
 * Caso de estudio: NicaData Solutions S.A.
 */
public class ProcesadorController {

    @FXML
    private Label lblEstado;

    @FXML
    private Label lblPorcentaje;

    @FXML
    private ProgressBar barraProgreso;

    @FXML
    private Button btnIniciar;

    @FXML
    private Button btnDetener;

    @FXML
    private Button btnReiniciar;

    // Componentes adicionales para el Reto Final: Procesamiento simultáneo de clientes y productos
    @FXML
    private Label lblEstadoProductos;

    @FXML
    private Label lblPorcentajeProductos;

    @FXML
    private ProgressBar barraProgresoProductos;

    @FXML
    private Button btnIniciarProductos;

    @FXML
    private Button btnDetenerProductos;

    @FXML
    private Button btnReiniciarProductos;

    private Thread hiloProceso;
    private Thread hiloProductos;

    @FXML
    private void iniciarProceso() {

        if (hiloProceso != null &&
                hiloProceso.isAlive()) {
            return;
        }

        barraProgreso.setProgress(0);
        lblPorcentaje.setText("0%");
        lblEstado.setText("Procesando registros...");

        btnIniciar.setDisable(true);
        btnDetener.setDisable(false);
        btnReiniciar.setDisable(true);

        Runnable tarea = () -> {

            try {
                for (int i = 1; i <= 100; i++) {

                    if (Thread.currentThread()
                            .isInterrupted()) {
                        throw new InterruptedException();
                    }

                    Thread.sleep(100);

                    final int avance = i;

                    Platform.runLater(() -> {
                        barraProgreso.setProgress(
                            avance / 100.0
                        );

                        lblPorcentaje.setText(
                            avance + "%"
                        );
                    });
                }

                Platform.runLater(() -> {
                    lblEstado.setText(
                        "Proceso finalizado"
                    );
                    habilitarBotones();
                });

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                Platform.runLater(() -> {
                    lblEstado.setText(
                        "Proceso detenido"
                    );
                    habilitarBotones();
                });
            }
        };

        hiloProceso = new Thread(tarea);

        hiloProceso.setName("Hilo-Procesamiento");
        hiloProceso.setDaemon(true);
        hiloProceso.start();
    }

    @FXML
    private void detenerProceso() {

        if (hiloProceso != null &&
                hiloProceso.isAlive()) {

            btnDetener.setDisable(true);

            lblEstado.setText(
                "Solicitando detención..."
            );

            hiloProceso.interrupt();
        }
    }

    @FXML
    private void reiniciarProceso() {

        if (hiloProceso != null &&
                hiloProceso.isAlive()) {
            return;
        }

        barraProgreso.setProgress(0);
        lblPorcentaje.setText("0%");
        lblEstado.setText("Esperando inicio");

        habilitarBotones();
    }

    private void habilitarBotones() {
        btnIniciar.setDisable(false);
        btnDetener.setDisable(true);
        btnReiniciar.setDisable(false);
    }

    // =========================================================================
    // RETO FINAL: Procesamiento concurrente de Productos (50 registros a 150ms)
    // =========================================================================

    @FXML
    private void iniciarProductos() {
        if (barraProgresoProductos == null) return;
        if (hiloProductos != null && hiloProductos.isAlive()) return;

        barraProgresoProductos.setProgress(0);
        lblPorcentajeProductos.setText("0%");
        lblEstadoProductos.setText("Procesando productos...");

        if (btnIniciarProductos != null) btnIniciarProductos.setDisable(true);
        if (btnDetenerProductos != null) btnDetenerProductos.setDisable(false);
        if (btnReiniciarProductos != null) btnReiniciarProductos.setDisable(true);

        Runnable tarea = () -> {
            try {
                for (int i = 1; i <= 50; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    Thread.sleep(150);
                    final int avance = i;
                    Platform.runLater(() -> {
                        barraProgresoProductos.setProgress(avance / 50.0);
                        lblPorcentajeProductos.setText((avance * 2) + "%");
                    });
                }
                Platform.runLater(() -> {
                    lblEstadoProductos.setText("Productos procesados");
                    habilitarBotonesProductos();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    lblEstadoProductos.setText("Proceso detenido");
                    habilitarBotonesProductos();
                });
            }
        };

        hiloProductos = new Thread(tarea, "Hilo-Productos");
        hiloProductos.setDaemon(true);
        hiloProductos.start();
    }

    @FXML
    private void detenerProductos() {
        if (hiloProductos != null && hiloProductos.isAlive()) {
            if (btnDetenerProductos != null) btnDetenerProductos.setDisable(true);
            lblEstadoProductos.setText("Solicitando detención...");
            hiloProductos.interrupt();
        }
    }

    @FXML
    private void reiniciarProductos() {
        if (barraProgresoProductos == null) return;
        if (hiloProductos != null && hiloProductos.isAlive()) return;

        barraProgresoProductos.setProgress(0);
        lblPorcentajeProductos.setText("0%");
        lblEstadoProductos.setText("Esperando inicio");
        habilitarBotonesProductos();
    }

    private void habilitarBotonesProductos() {
        if (btnIniciarProductos != null) btnIniciarProductos.setDisable(false);
        if (btnDetenerProductos != null) btnDetenerProductos.setDisable(true);
        if (btnReiniciarProductos != null) btnReiniciarProductos.setDisable(false);
    }

    public void detenerAlCerrar() {

        if (hiloProceso != null &&
                hiloProceso.isAlive()) {

            hiloProceso.interrupt();
        }

        if (hiloProductos != null &&
                hiloProductos.isAlive()) {

            hiloProductos.interrupt();
        }
    }
}
