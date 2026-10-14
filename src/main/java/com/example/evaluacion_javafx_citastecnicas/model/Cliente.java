package com.example.evaluacion_javafx_citastecnicas.model;

public class Cliente {
    private int id;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;
    private boolean activo;


    // Constructor
    public Cliente() {
    }

    public Cliente(String nombres, String apellidos, String telefono, String correo, boolean activo) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correo = correo;
        this.activo = activo;
    }

    public Cliente(int id, String nombres, String apellidos, String telefono, String correo, boolean activo) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correo = correo;
        this.activo = activo;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /**
     * Retorna el nombre completo concatenado (nombres + apellidos).
     * Utilizado para TableViews y ComboBox en JavaFX (RF04).
     */
    public String getNombreCompleto() {
        String n = nombres != null ? nombres.trim() : "";
        String a = apellidos != null ? apellidos.trim() : "";
        return (n + " " + a).trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        if (id > 0 && cliente.id > 0) {
            return id == cliente.id;
        }
        return java.util.Objects.equals(correo, cliente.correo);
    }

    @Override
    public int hashCode() {
        return id > 0 ? java.util.Objects.hash(id) : java.util.Objects.hash(correo);
    }

    @Override
    public String toString() {
        String nombre = getNombreCompleto();
        if (nombre.isEmpty()) {
            return correo != null ? correo : "Cliente #" + id;
        }
        return nombre + (correo != null && !correo.isBlank() ? " (" + correo + ")" : "");
    }
}
