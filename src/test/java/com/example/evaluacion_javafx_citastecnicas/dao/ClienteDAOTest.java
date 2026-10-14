package com.example.evaluacion_javafx_citastecnicas.dao;

import com.example.evaluacion_javafx_citastecnicas.database.DatabaseConnection;
import com.example.evaluacion_javafx_citastecnicas.model.Cliente;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClienteDAOTest {

    private ClienteDAO clienteDAO;
    private static boolean dbAvailable = false;
    private static final String TEST_EMAIL = "junit.test.cliente@correo.com";
    private static final String DUPLICATE_TEST_EMAIL = "junit.test.dup@correo.com";
    private static int insertedClienteId = 0;

    @BeforeAll
    static void checkDatabase() {
        try {
            dbAvailable = DatabaseConnection.testConnection();
        } catch (Exception e) {
            dbAvailable = false;
        }
    }

    @BeforeEach
    void setUp() {
        clienteDAO = new ClienteDAO();
    }

    // --- PRUEBAS DE VALIDACION (RF07) EN MEMORIA ---

    @Test
    @Order(1)
    @DisplayName("validarCliente debe rechazar cliente nulo")
    void testValidarClienteNulo() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> clienteDAO.validarCliente(null));
        assertTrue(ex.getMessage().contains("nulo"));
    }

    @Test
    @Order(2)
    @DisplayName("validarCliente debe rechazar nombres vacios o nulos")
    void testValidarNombresObligatorios() {
        Cliente c = new Cliente("", "Perez", "8888-1111", "correo@test.com", true);
        assertThrows(IllegalArgumentException.class, () -> clienteDAO.validarCliente(c));

        c.setNombres(null);
        assertThrows(IllegalArgumentException.class, () -> clienteDAO.validarCliente(c));
    }

    @Test
    @Order(3)
    @DisplayName("validarCliente debe rechazar apellidos vacios o nulos")
    void testValidarApellidosObligatorios() {
        Cliente c = new Cliente("Juan", "   ", "8888-1111", "correo@test.com", true);
        assertThrows(IllegalArgumentException.class, () -> clienteDAO.validarCliente(c));
    }

    @Test
    @Order(4)
    @DisplayName("validarCliente debe rechazar telefono vacio o nulo")
    void testValidarTelefonoObligatorio() {
        Cliente c = new Cliente("Juan", "Perez", "", "correo@test.com", true);
        assertThrows(IllegalArgumentException.class, () -> clienteDAO.validarCliente(c));
    }

    @Test
    @Order(5)
    @DisplayName("validarCliente debe validar formato basico de correo (RF07)")
    void testValidarFormatoCorreo() {
        Cliente c = new Cliente("Juan", "Perez", "8888-1111", "correo-invalido", true);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> clienteDAO.validarCliente(c));
        assertTrue(ex.getMessage().contains("formato"));

        c.setCorreo("sin_arroba.com");
        assertThrows(IllegalArgumentException.class, () -> clienteDAO.validarCliente(c));

        c.setCorreo("valido@dominio.com");
        assertDoesNotThrow(() -> clienteDAO.validarCliente(c));
    }

    // --- PRUEBAS CRUD Y PERSISTENCIA CONTRA BASE DE DATOS (RF02, RF05, RF07, RF08) ---

    @Test
    @Order(6)
    @DisplayName("RF02: Debe insertar cliente exitosamente y autogenerar ID con PreparedStatement")
    void testInsertarCliente() throws SQLException {
        Assumptions.assumeTrue(dbAvailable, "Base de datos no disponible, omitiendo test con persistencia");

        // Limpiar si existia previamente
        limpiarClientePorCorreo(TEST_EMAIL);

        Cliente c = new Cliente("Prueba JUnit", "Jarquin Test", "8899-0011", TEST_EMAIL, true);
        boolean insertado = clienteDAO.insertar(c);

        assertTrue(insertado);
        assertTrue(c.getId() > 0, "El ID autogenerado debe ser mayor a 0");
        insertedClienteId = c.getId();

        Cliente recuperado = clienteDAO.obtenerPorId(insertedClienteId);
        assertNotNull(recuperado);
        assertEquals("Prueba JUnit", recuperado.getNombres());
        assertEquals("Jarquin Test", recuperado.getApellidos());
        assertEquals(TEST_EMAIL.toLowerCase(), recuperado.getCorreo());
    }

    @Test
    @Order(7)
    @DisplayName("RF07: Debe evitar insercion de correo duplicado")
    void testEvitarCorreoDuplicado() {
        Assumptions.assumeTrue(dbAvailable && insertedClienteId > 0, "DB no disponible o ID previo invalido");

        Cliente duplicado = new Cliente("Otro", "Usuario", "8888-9999", TEST_EMAIL.toUpperCase(), true);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> clienteDAO.insertar(duplicado));
        assertTrue(ex.getMessage().contains("ya esta registrado"));
    }

    @Test
    @Order(8)
    @DisplayName("RF02: Debe actualizar datos del cliente con PreparedStatement")
    void testActualizarCliente() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && insertedClienteId > 0, "DB no disponible o ID previo invalido");

        Cliente c = clienteDAO.obtenerPorId(insertedClienteId);
        assertNotNull(c);
        c.setTelefono("8777-9999");
        c.setNombres("Prueba Modificada");

        boolean actualizado = clienteDAO.actualizar(c);
        assertTrue(actualizado);

        Cliente verificado = clienteDAO.obtenerPorId(insertedClienteId);
        assertEquals("8777-9999", verificado.getTelefono());
        assertEquals("Prueba Modificada", verificado.getNombres());
    }

    @Test
    @Order(9)
    @DisplayName("RF05: Debe buscar clientes por coincidencia parcial case-insensitive")
    void testBuscarCaseInsensitive() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && insertedClienteId > 0, "DB no disponible o ID previo invalido");

        // Busqueda por parte de nombres en minusculas
        List<Cliente> porNombre = clienteDAO.buscar("prueba mod");
        assertFalse(porNombre.isEmpty());
        assertTrue(porNombre.stream().anyMatch(cl -> cl.getId() == insertedClienteId));

        // Busqueda por parte de apellidos en mayusculas
        List<Cliente> porApellido = clienteDAO.buscar("JARQUIN");
        assertFalse(porApellido.isEmpty());
        assertTrue(porApellido.stream().anyMatch(cl -> cl.getId() == insertedClienteId));

        // Busqueda por correo
        List<Cliente> porCorreo = clienteDAO.buscar("junit.test");
        assertFalse(porCorreo.isEmpty());
        assertTrue(porCorreo.stream().anyMatch(cl -> cl.getId() == insertedClienteId));
    }

    @Test
    @Order(10)
    @DisplayName("RF08: Debe bloquear eliminacion de cliente si tiene citas tecnicas asociadas")
    void testBloquearEliminacionConCitas() throws SQLException {
        Assumptions.assumeTrue(dbAvailable, "Base de datos no disponible");

        // Encontrar o verificar un cliente que tenga citas (ej. cliente id=1 o datos iniciales)
        int clienteConCitaId = obtenerClienteConCita();
        Assumptions.assumeTrue(clienteConCitaId > 0, "No se encontro cliente con citas asociadas");

        assertTrue(clienteDAO.tieneCitasAsociadas(clienteConCitaId));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> clienteDAO.eliminar(clienteConCitaId));
        assertTrue(ex.getMessage().contains("tiene citas tecnicas asociadas"));
    }

    @Test
    @Order(11)
    @DisplayName("RF02: Debe eliminar fisicamente un cliente sin citas asociadas")
    void testEliminarClienteSinCitas() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && insertedClienteId > 0, "DB no disponible o ID previo invalido");

        boolean eliminado = clienteDAO.eliminar(insertedClienteId);
        assertTrue(eliminado);

        Cliente borrado = clienteDAO.obtenerPorId(insertedClienteId);
        assertNull(borrado, "El cliente eliminado no debe existir en la base de datos");
    }

    // --- Helpers de prueba ---

    private void limpiarClientePorCorreo(String correo) {
        String sql = "DELETE FROM cliente WHERE LOWER(correo) = LOWER(?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private int obtenerClienteConCita() {
        String sql = "SELECT cliente_id FROM cita_tecnica LIMIT 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("cliente_id");
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }
}
