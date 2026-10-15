package com.example.evaluacion_javafx_citastecnicas;

import com.example.evaluacion_javafx_citastecnicas.model.CitaTecnica;
import com.example.evaluacion_javafx_citastecnicas.model.Cliente;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

public class CitaFormController {
    public static final List<String> SERVICIOS = List.of("Mantenimiento Preventivo", "Mantenimiento Correctivo",
            "Reparacion de Hardware", "Instalacion de Software", "Diagnostico de Red", "Soporte Remoto");
    @FXML private VBox formulario;
    @FXML private ComboBox<Cliente> cliente;
    @FXML private DatePicker fecha;
    @FXML private TextField hora;
    @FXML private ComboBox<String> servicio, estado;
    @FXML private TextArea descripcion;
    @FXML private Button guardar;
    @FXML private Label titulo;
    private int id;
    private LocalDate fechaOriginal;
    private Consumer<CitaTecnica> alGuardar = cita -> { };

    @FXML private void initialize() {
        servicio.getItems().setAll(SERVICIOS);
        estado.getItems().setAll(CitaTecnica.ESTADOS_VALIDOS);
        estado.setValue("Pendiente");
        Ui.configurarFecha(fecha);
        fecha.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate dia, boolean vacio) {
                super.updateItem(dia, vacio);
                setDisable(vacio || (dia != null && dia.isBefore(LocalDate.now()) && !dia.equals(fechaOriginal)));
            }
        });
        Ui.flujo(cliente, fecha, hora, servicio, descripcion, estado, guardar);
    }

    public void clientes(List<Cliente> clientes) {
        Cliente seleccionado = cliente.getValue();
        cliente.getItems().setAll(clientes);
        cliente.setValue(clientes.stream().filter(c -> c.equals(seleccionado)).findFirst().orElse(null));
        cliente.setPromptText(clientes.isEmpty() ? "Primero registre un cliente" : "Seleccione un cliente");
        guardar.setDisable(clientes.isEmpty());
    }

    public void configurar(CitaTecnica cita, Consumer<CitaTecnica> accion) {
        alGuardar = accion;
        if (cita != null) {
            id = cita.getId();
            fechaOriginal = cita.getFecha();
            titulo.setText("Editar cita #" + id);
            guardar.setText("Actualizar cita");
            cliente.setValue(cliente.getItems().stream().filter(c -> c.getId() == cita.getClienteId()).findFirst().orElse(null));
            fecha.setValue(cita.getFecha());
            hora.setText(cita.getHoraFormateada());
            // Permite conservar servicios históricos sin transformarlos al editar otros campos.
            if (!servicio.getItems().contains(cita.getTipoServicio())) servicio.getItems().add(cita.getTipoServicio());
            servicio.setValue(cita.getTipoServicio());
            descripcion.setText(cita.getDescripcion());
            estado.setValue(cita.getEstado());
        }
    }

    CitaTecnica leer() {
        if (cliente.getValue() == null) throw new IllegalArgumentException("Seleccione un cliente existente.");
        LocalDate dia = Ui.leerFecha(fecha);
        if (dia == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        if (dia.isBefore(LocalDate.now()) && !dia.equals(fechaOriginal))
            throw new IllegalArgumentException("No se pueden agendar citas para antes del día actual.");
        String textoHora = hora.getText().trim();
        if (!textoHora.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]"))
            throw new IllegalArgumentException("Ingrese una hora válida de 00:00 a 23:59 (HH:mm).");
        if (servicio.getValue() == null || !servicio.getItems().contains(servicio.getValue()))
            throw new IllegalArgumentException("Seleccione un tipo de servicio de la lista.");
        if (descripcion.getText().trim().isEmpty() || descripcion.getText().trim().length() > 255)
            throw new IllegalArgumentException("La descripción es obligatoria y admite hasta 255 caracteres.");
        if (!CitaTecnica.ESTADOS_VALIDOS.contains(estado.getValue()))
            throw new IllegalArgumentException("Seleccione un estado válido.");
        return new CitaTecnica(id, cliente.getValue().getId(), cliente.getValue().getNombreCompleto(),
                dia, LocalTime.parse(textoHora), servicio.getValue(), descripcion.getText().trim(), estado.getValue());
    }

    @FXML private void guardar() {
        try { alGuardar.accept(leer()); }
        catch (IllegalArgumentException e) { Ui.error(formulario, e.getMessage()); }
    }

    @FXML public void limpiar() {
        cliente.setValue(null); fecha.setValue(null); fecha.getEditor().clear(); hora.clear();
        servicio.setValue(null); descripcion.clear(); estado.setValue("Pendiente");
        enfocar();
    }

    public void enfocar() { Platform.runLater(cliente::requestFocus); }
}
