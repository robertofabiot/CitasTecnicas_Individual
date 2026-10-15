package com.example.evaluacion_javafx_citastecnicas.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Entidad CitaTecnica que mapea la tabla 'cita_tecnica' en PostgreSQL.
 * Incluye el atributo relacional 'clienteNombreCompleto' para mostrar
 * el nombre del cliente asociado obtenido mediante INNER JOIN (RF04).
 */
public class CitaTecnica {

    public static final List<String> ESTADOS_VALIDOS = List.of(
            "Pendiente",
            "En proceso",
            "Finalizada",
            "Cancelada"
    );

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private int id;
    private int clienteId;
    private String clienteNombreCompleto;
    private LocalDate fecha;
    private LocalTime hora;
    private String tipoServicio;
    private String descripcion;
    private String estado;

    // Constructores
    public CitaTecnica() {
    }

    /**
     * Constructor para nuevas citas sin ID asignado aun.
     */
    public CitaTecnica(int clienteId, LocalDate fecha, LocalTime hora,
                       String tipoServicio, String descripcion, String estado) {
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoServicio = tipoServicio;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    /**
     * Constructor con ID para registros existentes de base de datos.
     */
    public CitaTecnica(int id, int clienteId, LocalDate fecha, LocalTime hora,
                       String tipoServicio, String descripcion, String estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoServicio = tipoServicio;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    /**
     * Constructor completo con nombre del cliente (utilizado para consultas con INNER JOIN - RF04).
     */
    public CitaTecnica(int id, int clienteId, String clienteNombreCompleto,
                       LocalDate fecha, LocalTime hora, String tipoServicio,
                       String descripcion, String estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.clienteNombreCompleto = clienteNombreCompleto;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoServicio = tipoServicio;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombreCompleto() {
        return clienteNombreCompleto != null ? clienteNombreCompleto : "";
    }

    public void setClienteNombreCompleto(String clienteNombreCompleto) {
        this.clienteNombreCompleto = clienteNombreCompleto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Helpers de formateo para la interfaz JavaFX
    public String getFechaFormateada() {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "";
    }

    public String getHoraFormateada() {
        return hora != null ? hora.format(FORMATO_HORA) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CitaTecnica that = (CitaTecnica) o;
        if (id > 0 && that.id > 0) {
            return id == that.id;
        }
        return clienteId == that.clienteId &&
                Objects.equals(fecha, that.fecha) &&
                Objects.equals(hora, that.hora);
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : Objects.hash(clienteId, fecha, hora);
    }

    @Override
    public String toString() {
        return "Cita #" + id + " - " +
                (clienteNombreCompleto != null && !clienteNombreCompleto.isBlank()
                        ? clienteNombreCompleto
                        : "Cliente #" + clienteId) +
                " [" + getFechaFormateada() + " " + getHoraFormateada() + "] - " +
                tipoServicio + " (" + estado + ")";
    }
}
