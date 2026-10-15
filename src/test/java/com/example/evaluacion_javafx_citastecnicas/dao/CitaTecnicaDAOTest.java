package com.example.evaluacion_javafx_citastecnicas.dao;

import com.example.evaluacion_javafx_citastecnicas.database.DatabaseConnection;
import com.example.evaluacion_javafx_citastecnicas.model.CitaTecnica;
import com.example.evaluacion_javafx_citastecnicas.model.Cliente;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CitaTecnicaDAOTest {

    private CitaTecnicaDAO citaDAO;
    private ClienteDAO clienteDAO;
    private static boolean dbAvailable = false;
    private static int testClienteId = 0;
    private static int insertedCitaId = 0;
    private static final String CLIENTE_TEST_EMAIL = "rommel.citas.test@correo.com";
    private static final LocalDate TEST_FECHA = LocalDate.now().plusDays(2);
    private static final LocalTime TEST_HORA = LocalTime.of(15, 30);

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
        citaDAO = new CitaTecnicaDAO();
        clienteDAO = new ClienteDAO();
    }

    // =========================================================================
    // 1. PRUEBAS DE VALIDACION (RF07) EN MEMORIA
    // =========================================================================

    @Test
    @Order(1)
    @DisplayName("validarCita debe rechazar cita nula")
    void testValidarCitaNula() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(null));
        assertTrue(ex.getMessage().contains("nula"));
    }

    @Test
    @Order(2)
    @DisplayName("validarCita debe rechazar clienteId invalido (menor o igual a 0)")
    void testValidarClienteIdInvalido() {
        CitaTecnica c = new CitaTecnica(0, LocalDate.now(), LocalTime.of(10, 0), "Servicio", "Desc", "Pendiente");
        assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(c));

        c.setClienteId(-5);
        assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(c));
    }

    @Test
    @Order(3)
    @DisplayName("validarCita debe rechazar fecha u hora nulas")
    void testValidarFechaYHoraObligatorias() {
        CitaTecnica c = new CitaTecnica(1, null, LocalTime.of(10, 0), "Servicio", "Desc", "Pendiente");
        assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(c));

        c.setFecha(LocalDate.now());
        c.setHora(null);
        assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(c));
    }

    @Test
    @Order(4)
    @DisplayName("validarCita debe rechazar tipoServicio vacio o que supere 80 caracteres")
    void testValidarTipoServicio() {
        CitaTecnica c = new CitaTecnica(1, LocalDate.now(), LocalTime.of(10, 0), "   ", "Desc", "Pendiente");
        assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(c));

        c.setTipoServicio("A".repeat(81));
        assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(c));
    }

    @Test
    @Order(5)
    @DisplayName("validarCita debe rechazar descripcion vacia o que supere 255 caracteres")
    void testValidarDescripcion() {
        CitaTecnica c = new CitaTecnica(1, LocalDate.now(), LocalTime.of(10, 0), "Servicio", "   ", "Pendiente");
        assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(c));

        c.setDescripcion("A".repeat(256));
        assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(c));
    }

    @Test
    @Order(6)
    @DisplayName("validarCita debe rechazar estados no permitidos por la base de datos (RF07)")
    void testValidarEstadoInvalido() {
        CitaTecnica c = new CitaTecnica(1, LocalDate.now(), LocalTime.of(10, 0), "Servicio", "Desc", "InvalidoEstado");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> citaDAO.validarCita(c));
        assertTrue(ex.getMessage().contains("no valido"));
    }

    // =========================================================================
    // 2. PRUEBAS CRUD Y PERSISTENCIA CONTRA BASE DE DATOS (RF03, RF04, RF06)
    // =========================================================================

    @Test
    @Order(7)
    @DisplayName("RF03: Debe insertar cita tecnica con PreparedStatement y autogenerar ID")
    void testInsertarCita() throws SQLException {
        Assumptions.assumeTrue(dbAvailable, "Base de datos no disponible");

        asegurarClienteDePrueba();
        assertTrue(testClienteId > 0, "Debe existir un cliente de prueba valido");

        CitaTecnica nueva = new CitaTecnica(
                testClienteId,
                TEST_FECHA,
                TEST_HORA,
                "Diagnostico de Red",
                "Falla en switch de laboratorio y configuracion VLAN",
                "Pendiente"
        );

        boolean insertado = citaDAO.insertar(nueva);
        assertTrue(insertado);
        assertTrue(nueva.getId() > 0, "El ID generado debe ser mayor a 0");
        insertedCitaId = nueva.getId();
    }

    @Test
    @Order(8)
    @DisplayName("RF04: Debe obtener cita por ID con INNER JOIN y nombre completo de cliente proyectado")
    void testObtenerPorIdConInnerJoin() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && insertedCitaId > 0, "DB no disponible o ID de cita invalido");

        CitaTecnica recuperada = citaDAO.obtenerPorId(insertedCitaId);
        assertNotNull(recuperada);
        assertEquals(insertedCitaId, recuperada.getId());
        assertEquals(testClienteId, recuperada.getClienteId());
        assertEquals(TEST_FECHA, recuperada.getFecha());
        assertEquals(TEST_HORA, recuperada.getHora());
        assertEquals("Diagnostico de Red", recuperada.getTipoServicio());
        assertEquals("Pendiente", recuperada.getEstado());

        // RF04: Verificacion estricta de INNER JOIN
        assertNotNull(recuperada.getClienteNombreCompleto(), "El nombre del cliente no debe ser nulo");
        assertFalse(recuperada.getClienteNombreCompleto().isBlank(), "El nombre del cliente debe provenir del INNER JOIN");
        assertTrue(recuperada.getClienteNombreCompleto().contains("Rommel Test"),
                "El nombre completo del cliente debe coincidir con el cliente asociado");
    }

    @Test
    @Order(9)
    @DisplayName("RF04: Listar todas las citas debe retornar registros con el nombre completo del cliente")
    void testListarTodasConInnerJoin() throws SQLException {
        Assumptions.assumeTrue(dbAvailable, "Base de datos no disponible");

        List<CitaTecnica> lista = citaDAO.listarTodas();
        assertFalse(lista.isEmpty(), "La lista de citas no deberia estar vacia");

        boolean encontroInsertada = false;
        for (CitaTecnica cita : lista) {
            assertNotNull(cita.getClienteNombreCompleto());
            assertFalse(cita.getClienteNombreCompleto().isBlank());
            if (cita.getId() == insertedCitaId) {
                encontroInsertada = true;
            }
        }
        assertTrue(encontroInsertada, "La cita insertada debe estar en la lista global");
    }

    @Test
    @Order(10)
    @DisplayName("RF03/RF04: Listar citas por cliente especifico debe filtrar correctamente")
    void testListarPorCliente() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && testClienteId > 0, "DB no disponible o ID cliente invalido");

        List<CitaTecnica> citasCliente = citaDAO.listarPorCliente(testClienteId);
        assertFalse(citasCliente.isEmpty());
        for (CitaTecnica c : citasCliente) {
            assertEquals(testClienteId, c.getClienteId());
        }
    }

    @Test
    @Order(11)
    @DisplayName("RF03: Debe actualizar estado y datos de la cita tecnica con PreparedStatement")
    void testActualizarCita() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && insertedCitaId > 0, "DB no disponible o ID invalido");

        CitaTecnica cita = citaDAO.obtenerPorId(insertedCitaId);
        assertNotNull(cita);

        cita.setEstado("En proceso");
        cita.setTipoServicio("Mantenimiento Correctivo");
        cita.setDescripcion("Cambio de puerto Gigabit y cable UTP Cat6");

        boolean actualizado = citaDAO.actualizar(cita);
        assertTrue(actualizado);

        CitaTecnica verificada = citaDAO.obtenerPorId(insertedCitaId);
        assertEquals("En proceso", verificada.getEstado());
        assertEquals("Mantenimiento Correctivo", verificada.getTipoServicio());
        assertEquals("Cambio de puerto Gigabit y cable UTP Cat6", verificada.getDescripcion());
    }

    @Test
    @Order(12)
    @DisplayName("RF06: Debe filtrar citas por estado de forma parametrizada")
    void testFiltrarPorEstado() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && insertedCitaId > 0, "DB no disponible o ID invalido");

        List<CitaTecnica> enProceso = citaDAO.filtrar("En proceso", null);
        assertFalse(enProceso.isEmpty());
        assertTrue(enProceso.stream().anyMatch(c -> c.getId() == insertedCitaId));
        assertTrue(enProceso.stream().allMatch(c -> "En proceso".equalsIgnoreCase(c.getEstado())));
    }

    @Test
    @Order(13)
    @DisplayName("RF06: Debe filtrar citas por fecha de forma parametrizada")
    void testFiltrarPorFecha() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && insertedCitaId > 0, "DB no disponible o ID invalido");

        List<CitaTecnica> porFecha = citaDAO.filtrar(null, TEST_FECHA);
        assertFalse(porFecha.isEmpty());
        assertTrue(porFecha.stream().anyMatch(c -> c.getId() == insertedCitaId));
        assertTrue(porFecha.stream().allMatch(c -> TEST_FECHA.equals(c.getFecha())));
    }

    @Test
    @Order(14)
    @DisplayName("RF06: Debe filtrar citas combinando estado y fecha")
    void testFiltrarPorEstadoYFecha() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && insertedCitaId > 0, "DB no disponible o ID invalido");

        List<CitaTecnica> combinadas = citaDAO.filtrar("En proceso", TEST_FECHA);
        assertFalse(combinadas.isEmpty());
        assertTrue(combinadas.stream().anyMatch(c -> c.getId() == insertedCitaId));
        assertTrue(combinadas.stream().allMatch(c -> "En proceso".equalsIgnoreCase(c.getEstado()) && TEST_FECHA.equals(c.getFecha())));
    }

    @Test
    @Order(15)
    @DisplayName("RF06: Limpiar filtros ('Todos' o vacio) debe devolver todas las citas")
    void testLimpiarFiltros() throws SQLException {
        Assumptions.assumeTrue(dbAvailable, "Base de datos no disponible");

        List<CitaTecnica> todas = citaDAO.listarTodas();
        List<CitaTecnica> filtroLimpioTodos = citaDAO.filtrar("Todos", null);
        List<CitaTecnica> filtroLimpioVacio = citaDAO.filtrar("", null);
        List<CitaTecnica> filtroNulo = citaDAO.filtrar(null, null);

        assertEquals(todas.size(), filtroLimpioTodos.size());
        assertEquals(todas.size(), filtroLimpioVacio.size());
        assertEquals(todas.size(), filtroNulo.size());
    }

    @Test
    @Order(16)
    @DisplayName("RF03/RF07: Rechazar insercion con cliente inexistente (violacion de FK)")
    void testRechazarClienteInexistente() {
        Assumptions.assumeTrue(dbAvailable, "Base de datos no disponible");

        CitaTecnica conClienteInexistente = new CitaTecnica(
                99999999,
                LocalDate.now(),
                LocalTime.of(12, 0),
                "Soporte Remoto",
                "Prueba FK",
                "Pendiente"
        );

        Exception ex = assertThrows(IllegalArgumentException.class, () -> citaDAO.insertar(conClienteInexistente));
        assertTrue(ex.getMessage().contains("no existe en el sistema"));
    }

    @Test
    @Order(17)
    @DisplayName("RF03: Debe eliminar fisicamente la cita tecnica por su ID")
    void testEliminarCita() throws SQLException {
        Assumptions.assumeTrue(dbAvailable && insertedCitaId > 0, "DB no disponible o ID invalido");

        boolean eliminado = citaDAO.eliminar(insertedCitaId);
        assertTrue(eliminado);

        CitaTecnica borrada = citaDAO.obtenerPorId(insertedCitaId);
        assertNull(borrada, "La cita eliminada no debe existir en la base de datos");

        // Limpieza del cliente de prueba
        limpiarClienteDePrueba();
    }

    // =========================================================================
    // Helpers de configuracion y limpieza
    // =========================================================================

    private void asegurarClienteDePrueba() throws SQLException {
        String sql = "SELECT id FROM cliente WHERE LOWER(correo) = LOWER(?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, CLIENTE_TEST_EMAIL);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    testClienteId = rs.getInt("id");
                    return;
                }
            }
        }

        Cliente cliente = new Cliente("Rommel Test", "Citas DAO", "8899-7711", CLIENTE_TEST_EMAIL, true);
        clienteDAO.insertar(cliente);
        testClienteId = cliente.getId();
    }

    private void limpiarClienteDePrueba() {
        if (testClienteId > 0) {
            try {
                clienteDAO.eliminar(testClienteId);
            } catch (Exception ignored) {
            }
        }
    }
}
