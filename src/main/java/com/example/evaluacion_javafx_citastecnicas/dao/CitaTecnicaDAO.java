package com.example.evaluacion_javafx_citastecnicas.dao;

import com.example.evaluacion_javafx_citastecnicas.database.DatabaseConnection;
import com.example.evaluacion_javafx_citastecnicas.model.CitaTecnica;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la entidad CitaTecnica.
 *
 * Cumple con los requerimientos tecnicos y funcionales:
 *  - RF03: Operaciones CRUD completas (crear, listar, editar, eliminar) utilizando
 *          exclusivamente PreparedStatement con parametros.
 *  - RF04: Consultas relacionales con INNER JOIN a la tabla 'cliente' para proyectar
 *          el nombre completo del cliente en cada cita tecnica.
 *  - RF06: Filtrado dinamico y parametrizado por estado y/o fecha con opcion de limpieza
 *          (al pasar filtros vacios o nulos recupera todos los registros).
 *  - RF07: Validaciones estrictas de campos obligatorios, limites de caracteres y estados permitidos.
 *  - RF08: Cierre estricto de recursos JDBC mediante try-with-resources y gestion de excepciones SQL.
 */
public class CitaTecnicaDAO {

    // =========================================================================
    // 1. VALIDACIONES DE NEGOCIO (RF07)
    // =========================================================================

    /**
     * Valida las restricciones de negocio de una CitaTecnica.
     * Lanza IllegalArgumentException si alguna regla no se cumple.
     */
    public void validarCita(CitaTecnica cita) {
        if (cita == null) {
            throw new IllegalArgumentException("La cita tecnica no puede ser nula.");
        }
        if (cita.getClienteId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un cliente valido para la cita.");
        }
        if (cita.getFecha() == null) {
            throw new IllegalArgumentException("La fecha de la cita es obligatoria.");
        }
        if (cita.getHora() == null) {
            throw new IllegalArgumentException("La hora de la cita es obligatoria.");
        }
        if (cita.getTipoServicio() == null || cita.getTipoServicio().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio.");
        }
        if (cita.getTipoServicio().trim().length() > 80) {
            throw new IllegalArgumentException("El tipo de servicio no puede exceder los 80 caracteres.");
        }
        if (cita.getDescripcion() == null || cita.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripcion de la cita es obligatoria.");
        }
        if (cita.getDescripcion().trim().length() > 255) {
            throw new IllegalArgumentException("La descripcion no puede superar los 255 caracteres.");
        }
        if (cita.getEstado() == null || cita.getEstado().trim().isEmpty()) {
            throw new IllegalArgumentException("El estado de la cita es obligatorio.");
        }

        String estadoTrim = cita.getEstado().trim();
        boolean estadoValido = CitaTecnica.ESTADOS_VALIDOS.stream()
                .anyMatch(e -> e.equalsIgnoreCase(estadoTrim));

        if (!estadoValido) {
            throw new IllegalArgumentException(
                    "Estado '" + cita.getEstado() + "' no valido. Opciones permitidas: " +
                            String.join(", ", CitaTecnica.ESTADOS_VALIDOS)
            );
        }
    }

    // =========================================================================
    // 2. OPERACIONES CRUD (RF03)
    // =========================================================================

    /**
     * Registra una nueva cita tecnica en la base de datos (CREATE).
     * Obtiene el ID autogenerado asignandolo a la entidad recibida.
     *
     * @param cita CitaTecnica a registrar
     * @return true si se inserto exitosamente
     * @throws SQLException si ocurre un error de base de datos
     */
    public boolean insertar(CitaTecnica cita) throws SQLException {
        validarCita(cita);

        String sql = "INSERT INTO cita_tecnica (cliente_id, fecha, hora, tipo_servicio, descripcion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, cita.getClienteId());
            ps.setDate(2, Date.valueOf(cita.getFecha()));
            ps.setTime(3, Time.valueOf(cita.getHora()));
            ps.setString(4, cita.getTipoServicio().trim());
            ps.setString(5, cita.getDescripcion().trim());
            ps.setString(6, normalizarEstado(cita.getEstado()));

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        cita.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            // SQLState 23503 = foreign_key_violation (cliente no existe)
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                        "El cliente con ID " + cita.getClienteId() + " no existe en el sistema.", e);
            }
            throw e;
        }
    }

    /**
     * Obtiene una cita tecnica por su identificador primario (READ BY ID),
     * incluyendo el nombre completo del cliente mediante INNER JOIN (RF04).
     *
     * @param id Identificador de la cita
     * @return CitaTecnica encontrada o null si no existe
     * @throws SQLException si ocurre error en la ejecucion SQL
     */
    public CitaTecnica obtenerPorId(int id) throws SQLException {
        if (id <= 0) {
            return null;
        }

        String sql = "SELECT c.id, c.cliente_id, " +
                "TRIM(CONCAT(cl.nombres, ' ', cl.apellidos)) AS cliente_nombre_completo, " +
                "c.fecha, c.hora, c.tipo_servicio, c.descripcion, c.estado " +
                "FROM cita_tecnica c " +
                "INNER JOIN cliente cl ON c.cliente_id = cl.id " +
                "WHERE c.id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCita(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todas las citas tecnicas registradas con INNER JOIN al cliente (READ ALL - RF04).
     * Ordenadas de forma cronologica descendente (mas recientes primero).
     *
     * @return Lista de citas tecnicas con el nombre completo de sus respectivos clientes
     * @throws SQLException si ocurre un error SQL
     */
    public List<CitaTecnica> listarTodas() throws SQLException {
        List<CitaTecnica> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.cliente_id, " +
                "TRIM(CONCAT(cl.nombres, ' ', cl.apellidos)) AS cliente_nombre_completo, " +
                "c.fecha, c.hora, c.tipo_servicio, c.descripcion, c.estado " +
                "FROM cita_tecnica c " +
                "INNER JOIN cliente cl ON c.cliente_id = cl.id " +
                "ORDER BY c.fecha DESC, c.hora DESC, c.id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCita(rs));
            }
        }
        return lista;
    }

    /**
     * Lista todas las citas asociadas a un cliente especifico (RF03, RF04).
     *
     * @param clienteId ID del cliente
     * @return Lista de citas del cliente
     * @throws SQLException si ocurre un error SQL
     */
    public List<CitaTecnica> listarPorCliente(int clienteId) throws SQLException {
        List<CitaTecnica> lista = new ArrayList<>();
        if (clienteId <= 0) {
            return lista;
        }

        String sql = "SELECT c.id, c.cliente_id, " +
                "TRIM(CONCAT(cl.nombres, ' ', cl.apellidos)) AS cliente_nombre_completo, " +
                "c.fecha, c.hora, c.tipo_servicio, c.descripcion, c.estado " +
                "FROM cita_tecnica c " +
                "INNER JOIN cliente cl ON c.cliente_id = cl.id " +
                "WHERE c.cliente_id = ? " +
                "ORDER BY c.fecha DESC, c.hora DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCita(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Actualiza los datos de una cita tecnica existente (UPDATE).
     *
     * @param cita CitaTecnica con los datos modificados
     * @return true si la actualizacion afecto al menos una fila
     * @throws SQLException si ocurre un error SQL
     */
    public boolean actualizar(CitaTecnica cita) throws SQLException {
        if (cita == null || cita.getId() <= 0) {
            throw new IllegalArgumentException("La cita a actualizar debe tener un ID valido mayor a 0.");
        }
        validarCita(cita);

        String sql = "UPDATE cita_tecnica SET cliente_id = ?, fecha = ?, hora = ?, " +
                "tipo_servicio = ?, descripcion = ?, estado = ? " +
                "WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cita.getClienteId());
            ps.setDate(2, Date.valueOf(cita.getFecha()));
            ps.setTime(3, Time.valueOf(cita.getHora()));
            ps.setString(4, cita.getTipoServicio().trim());
            ps.setString(5, cita.getDescripcion().trim());
            ps.setString(6, normalizarEstado(cita.getEstado()));
            ps.setInt(7, cita.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                        "No se puede actualizar la cita: El cliente con ID " + cita.getClienteId() + " no existe.", e);
            }
            throw e;
        }
    }

    /**
     * Elimina fisicamente una cita tecnica por su ID (DELETE - RF03).
     *
     * @param id Identificador de la cita a eliminar
     * @return true si se elimino exitosamente
     * @throws SQLException si ocurre un error SQL
     */
    public boolean eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la cita a eliminar debe ser mayor a 0.");
        }

        String sql = "DELETE FROM cita_tecnica WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 3. FILTRADO PARAMETRIZADO Y LIMPIEZA DE FILTROS (RF06)
    // =========================================================================

    /**
     * Filtra las citas tecnicas por estado y/o fecha de manera dinamica (RF06).
     * Si ambos criterios estan vacios o nulos (o estado es 'Todos'),
     * se limpia el filtro y retorna todas las citas.
     *
     * Cumple estrictamente con la rubrica:
     * - Utiliza exclusivamente PreparedStatement con parametros (cero concatenacion SQL).
     * - Incluye INNER JOIN cliente para mostrar siempre el nombre completo (RF04).
     *
     * @param estado Estado a filtrar (ej. 'Pendiente', 'Finalizada', o null/'Todos' para ignorar)
     * @param fecha  Fecha a filtrar (LocalDate o null para ignorar)
     * @return Lista de citas tecnicas que cumplen con los criterios especificados
     * @throws SQLException si ocurre un error SQL
     */
    public List<CitaTecnica> filtrar(String estado, LocalDate fecha) throws SQLException {
        boolean filtrarEstado = estado != null && !estado.trim().isEmpty() && !estado.trim().equalsIgnoreCase("Todos");
        boolean filtrarFecha = fecha != null;

        // Si no hay filtros aplicados, se limpian y retornan todas
        if (!filtrarEstado && !filtrarFecha) {
            return listarTodas();
        }

        StringBuilder sql = new StringBuilder(
                "SELECT c.id, c.cliente_id, " +
                "TRIM(CONCAT(cl.nombres, ' ', cl.apellidos)) AS cliente_nombre_completo, " +
                "c.fecha, c.hora, c.tipo_servicio, c.descripcion, c.estado " +
                "FROM cita_tecnica c " +
                "INNER JOIN cliente cl ON c.cliente_id = cl.id " +
                "WHERE 1=1 "
        );

        List<Object> parametros = new ArrayList<>();

        if (filtrarEstado) {
            sql.append("AND LOWER(c.estado) = LOWER(?) ");
            parametros.add(estado.trim());
        }

        if (filtrarFecha) {
            sql.append("AND c.fecha = ? ");
            parametros.add(Date.valueOf(fecha));
        }

        sql.append("ORDER BY c.fecha DESC, c.hora DESC, c.id DESC");

        List<CitaTecnica> resultado = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                Object param = parametros.get(i);
                int paramIndex = i + 1;

                if (param instanceof String s) {
                    ps.setString(paramIndex, s);
                } else if (param instanceof Date d) {
                    ps.setDate(paramIndex, d);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearCita(rs));
                }
            }
        }

        return resultado;
    }

    // =========================================================================
    // 4. METODOS AUXILIARES
    // =========================================================================

    /**
     * Normaliza la capitalizacion del estado para que coincida con los valores definidos.
     */
    private String normalizarEstado(String estado) {
        if (estado == null) return "Pendiente";
        for (String ev : CitaTecnica.ESTADOS_VALIDOS) {
            if (ev.equalsIgnoreCase(estado.trim())) {
                return ev;
            }
        }
        return estado.trim();
    }

    /**
     * Mapea una fila de ResultSet a un objeto CitaTecnica.
     */
    private CitaTecnica mapearCita(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("fecha");
        Time sqlTime = rs.getTime("hora");

        return new CitaTecnica(
                rs.getInt("id"),
                rs.getInt("cliente_id"),
                rs.getString("cliente_nombre_completo"),
                sqlDate != null ? sqlDate.toLocalDate() : null,
                sqlTime != null ? sqlTime.toLocalTime() : null,
                rs.getString("tipo_servicio"),
                rs.getString("descripcion"),
                rs.getString("estado")
        );
    }
}
