package com.example.evaluacion_javafx_citastecnicas.dao;

import com.example.evaluacion_javafx_citastecnicas.database.DatabaseConnection;
import com.example.evaluacion_javafx_citastecnicas.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la entidad Cliente.
 *
 * Cumple:
 *  - RF02: CRUD completo con PreparedStatement (nunca Statement con datos de usuario).
 *  - RF05: Busqueda parcial case-insensitive por nombres, apellidos o correo.
 *  - RF07: Validacion de campos obligatorios, formato de correo y correo duplicado.
 *  - Eliminacion FISICA (DELETE) respetando integridad referencial (FK RESTRICT).
 */
public class ClienteDAO {

    // Regex basico de correo: algo@algo.algo
    private static final String REGEX_CORREO =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // VALIDACIONES

    /** Valida campos obligatorios y formato. Lanza IllegalArgumentException si algo falla. */
    public void validarCliente(Cliente c) {
        if (c == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (c.getNombres() == null || c.getNombres().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'Nombres' es obligatorio.");
        }
        if (c.getNombres().trim().length() > 80) {
            throw new IllegalArgumentException("'Nombres' no puede superar 80 caracteres.");
        }
        if (c.getApellidos() == null || c.getApellidos().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'Apellidos' es obligatorio.");
        }
        if (c.getApellidos().trim().length() > 80) {
            throw new IllegalArgumentException("'Apellidos' no puede superar 80 caracteres.");
        }
        if (c.getTelefono() == null || c.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'Telefono' es obligatorio.");
        }
        if (c.getTelefono().trim().length() > 20) {
            throw new IllegalArgumentException("'Telefono' no puede superar 20 caracteres.");
        }
        if (c.getCorreo() == null || c.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'Correo' es obligatorio.");
        }
        if (!c.getCorreo().trim().matches(REGEX_CORREO)) {
            throw new IllegalArgumentException("El correo no tiene un formato valido (ejemplo@dominio.com).");
        }
        if (c.getCorreo().trim().length() > 120) {
            throw new IllegalArgumentException("'Correo' no puede superar 120 caracteres.");
        }
    }

    // CREATE
    public boolean insertar(Cliente cliente) throws SQLException {
        validarCliente(cliente);

        if (existeCorreo(cliente.getCorreo(), 0)) {
            throw new IllegalArgumentException(
                    "El correo '" + cliente.getCorreo() + "' ya esta registrado.");
        }

        String sql = "INSERT INTO cliente (nombres, apellidos, telefono, correo, activo) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNombres().trim());
            ps.setString(2, cliente.getApellidos().trim());
            ps.setString(3, cliente.getTelefono().trim());
            ps.setString(4, cliente.getCorreo().trim().toLowerCase());
            ps.setBoolean(5, cliente.isActivo());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) cliente.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    // READ por ID
    public Cliente obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id, nombres, apellidos, telefono, correo, activo " +
                "FROM cliente WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearCliente(rs);
            }
        }
        return null;
    }

    // READ ALL
    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id, nombres, apellidos, telefono, correo, activo " +
                "FROM cliente ORDER BY id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapearCliente(rs));
        }
        return lista;
    }

    // UPDATE
    public boolean actualizar(Cliente cliente) throws SQLException {
        validarCliente(cliente);

        if (existeCorreo(cliente.getCorreo(), cliente.getId())) {
            throw new IllegalArgumentException(
                    "El correo '" + cliente.getCorreo() + "' ya esta registrado por otro cliente.");
        }

        String sql = "UPDATE cliente SET nombres=?, apellidos=?, telefono=?, " +
                "correo=?, activo=? WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombres().trim());
            ps.setString(2, cliente.getApellidos().trim());
            ps.setString(3, cliente.getTelefono().trim());
            ps.setString(4, cliente.getCorreo().trim().toLowerCase());
            ps.setBoolean(5, cliente.isActivo());
            ps.setInt(6, cliente.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // DELETE FISICO
    /**
     * Elimina FISICAMENTE al cliente.
     * Si tiene citas asociadas, PostgreSQL lanza SQLState 23503 y se
     * traduce a IllegalArgumentException con mensaje claro para la UI.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // 23503 = foreign_key_violation
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                        "No se puede eliminar el cliente porque tiene citas tecnicas asociadas. " +
                                "Primero elimine o reasigne sus citas.");
            }
            throw e;
        }
    }

    // BUSQUEDA CASE-INSENSITIVE
    public List<Cliente> buscar(String criterio) throws SQLException {
        List<Cliente> lista = new ArrayList<>();

        String sql = "SELECT id, nombres, apellidos, telefono, correo, activo " +
                "FROM cliente " +
                "WHERE LOWER(nombres)   LIKE LOWER(?) " +
                "   OR LOWER(apellidos) LIKE LOWER(?) " +
                "   OR LOWER(correo)    LIKE LOWER(?) " +
                "ORDER BY apellidos, nombres";

        String patron = "%" + (criterio == null ? "" : criterio.trim()) + "%";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCliente(rs));
            }
        }
        return lista;
    }

    // VALIDACION DE CORREO DUPLICADO
    public boolean existeCorreo(String correo, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cliente " +
                "WHERE LOWER(correo) = LOWER(?) AND id <> ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo == null ? "" : correo.trim());
            ps.setInt(2, idExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Helper
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getBoolean("activo")
        );
    }
}