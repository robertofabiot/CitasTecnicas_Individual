package com.example.evaluacion_javafx_citastecnicas.dao;

import com.example.evaluacion_javafx_citastecnicas.database.DatabaseConnection;
import com.example.evaluacion_javafx_citastecnicas.model.Cliente;
import com.example.evaluacion_javafx_citastecnicas.model.CitaTecnica;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class IntegridadClienteCitaTest {
    @Test void protegeClienteConCitasYPermiteEliminarloDespuesDeEliminarLaCita() throws Exception {
        Assumptions.assumeTrue(DatabaseConnection.testConnection(), "PostgreSQL no está disponible");
        ClienteDAO clientes = new ClienteDAO();
        CitaTecnicaDAO citas = new CitaTecnicaDAO();
        Cliente cliente = new Cliente("Prueba integridad", "Cliente con cita", "8888-1111",
                "integridad." + UUID.randomUUID() + "@example.com", true);
        CitaTecnica cita = null;
        try {
            assertTrue(clientes.insertar(cliente));
            cita = new CitaTecnica(cliente.getId(), LocalDate.now().plusDays(1),
                    LocalTime.NOON, "Soporte Remoto", "Prueba de restricción de eliminación", "Pendiente");
            assertTrue(citas.insertar(cita));

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> clientes.eliminar(cliente.getId()));
            assertTrue(error.getMessage().contains("tiene citas tecnicas asociadas"));
            assertNotNull(clientes.obtenerPorId(cliente.getId()), "El rechazo debe conservar al cliente");
            assertNotNull(citas.obtenerPorId(cita.getId()), "El rechazo debe conservar la cita");

            assertTrue(citas.eliminar(cita.getId()));
            assertNull(citas.obtenerPorId(cita.getId()));
            assertTrue(clientes.eliminar(cliente.getId()));
            assertNull(clientes.obtenerPorId(cliente.getId()));
        } finally {
            // Solo se limpian registros creados por esta ejecución, incluso si falla una aserción.
            try {
                if (cita != null && cita.getId() > 0) citas.eliminar(cita.getId());
            } finally {
                if (cliente.getId() > 0) clientes.eliminar(cliente.getId());
            }
        }
    }
}
