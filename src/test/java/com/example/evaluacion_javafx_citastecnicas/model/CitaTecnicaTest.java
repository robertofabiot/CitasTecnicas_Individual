package com.example.evaluacion_javafx_citastecnicas.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CitaTecnicaTest {

    @Test
    @DisplayName("Debe crear cita con constructor completo incluyendo nombre del cliente (RF04)")
    void testConstructorCompleto() {
        LocalDate fecha = LocalDate.of(2026, 10, 15);
        LocalTime hora = LocalTime.of(10, 30);
        CitaTecnica cita = new CitaTecnica(1, 2, "Carlos Mendoza", fecha, hora,
                "Mantenimiento Preventivo", "Limpieza de hardware", "Pendiente");

        assertEquals(1, cita.getId());
        assertEquals(2, cita.getClienteId());
        assertEquals("Carlos Mendoza", cita.getClienteNombreCompleto());
        assertEquals(fecha, cita.getFecha());
        assertEquals(hora, cita.getHora());
        assertEquals("Mantenimiento Preventivo", cita.getTipoServicio());
        assertEquals("Limpieza de hardware", cita.getDescripcion());
        assertEquals("Pendiente", cita.getEstado());
    }

    @Test
    @DisplayName("Debe crear cita con constructor sin ID (para nuevas inserciones)")
    void testConstructorSinId() {
        LocalDate fecha = LocalDate.of(2026, 10, 16);
        LocalTime hora = LocalTime.of(14, 0);
        CitaTecnica cita = new CitaTecnica(3, fecha, hora,
                "Reparacion", "Falla de encendido", "En proceso");

        assertEquals(0, cita.getId());
        assertEquals(3, cita.getClienteId());
        assertEquals("", cita.getClienteNombreCompleto());
        assertEquals(fecha, cita.getFecha());
        assertEquals(hora, cita.getHora());
        assertEquals("Reparacion", cita.getTipoServicio());
        assertEquals("Falla de encendido", cita.getDescripcion());
        assertEquals("En proceso", cita.getEstado());
    }

    @Test
    @DisplayName("Debe formatear fecha y hora correctamente para la interfaz grafica")
    void testFormatoFechaYHora() {
        CitaTecnica cita = new CitaTecnica();
        cita.setFecha(LocalDate.of(2026, 10, 20));
        cita.setHora(LocalTime.of(9, 15));

        assertEquals("20/10/2026", cita.getFechaFormateada());
        assertEquals("09:15", cita.getHoraFormateada());

        // Manejo seguro de nulos
        cita.setFecha(null);
        cita.setHora(null);
        assertEquals("", cita.getFechaFormateada());
        assertEquals("", cita.getHoraFormateada());
    }

    @Test
    @DisplayName("Debe verificar igualdad por ID y por clave de negocio")
    void testEqualsAndHashCode() {
        LocalDate fecha = LocalDate.of(2026, 10, 15);
        LocalTime hora = LocalTime.of(10, 0);

        // Por ID
        CitaTecnica c1 = new CitaTecnica(5, 1, fecha, hora, "Diagnostico", "Revision", "Pendiente");
        CitaTecnica c2 = new CitaTecnica(5, 2, fecha, hora, "Otro", "Revision 2", "Cancelada");
        CitaTecnica c3 = new CitaTecnica(6, 1, fecha, hora, "Diagnostico", "Revision", "Pendiente");

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertEquals(c1.hashCode(), c2.hashCode());

        // Sin ID (por clienteId, fecha y hora)
        CitaTecnica c4 = new CitaTecnica(1, fecha, hora, "A", "Desc", "Pendiente");
        CitaTecnica c5 = new CitaTecnica(1, fecha, hora, "B", "Desc 2", "Finalizada");
        CitaTecnica c6 = new CitaTecnica(2, fecha, hora, "A", "Desc", "Pendiente");

        assertEquals(c4, c5);
        assertNotEquals(c4, c6);
    }

    @Test
    @DisplayName("Debe contener la lista oficial de estados validos de la base de datos")
    void testEstadosValidos() {
        assertTrue(CitaTecnica.ESTADOS_VALIDOS.contains("Pendiente"));
        assertTrue(CitaTecnica.ESTADOS_VALIDOS.contains("En proceso"));
        assertTrue(CitaTecnica.ESTADOS_VALIDOS.contains("Finalizada"));
        assertTrue(CitaTecnica.ESTADOS_VALIDOS.contains("Cancelada"));
        assertEquals(4, CitaTecnica.ESTADOS_VALIDOS.size());
    }

    @Test
    @DisplayName("Debe generar toString descriptivo con fecha, hora y cliente")
    void testToStringFormato() {
        CitaTecnica cita = new CitaTecnica(10, 1, "Maria Elena Robleto",
                LocalDate.of(2026, 10, 18), LocalTime.of(16, 0),
                "Instalacion de Software", "Antivirus y Office", "Pendiente");

        String str = cita.toString();
        assertTrue(str.contains("Cita #10"));
        assertTrue(str.contains("Maria Elena Robleto"));
        assertTrue(str.contains("18/10/2026"));
        assertTrue(str.contains("16:00"));
        assertTrue(str.contains("Instalacion de Software"));
    }
}
