package com.example.evaluacion_javafx_citastecnicas.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    @DisplayName("Debe crear cliente con constructor completo y verificar getters")
    void testConstructorCompleto() {
        Cliente c = new Cliente(1, "Mario", "Bros", "8888-1234", "mario@nintendo.com", true);

        assertEquals(1, c.getId());
        assertEquals("Mario", c.getNombres());
        assertEquals("Bros", c.getApellidos());
        assertEquals("8888-1234", c.getTelefono());
        assertEquals("mario@nintendo.com", c.getCorreo());
        assertTrue(c.isActivo());
    }

    @Test
    @DisplayName("Debe crear cliente con constructor sin ID (para nuevas inserciones)")
    void testConstructorSinId() {
        Cliente c = new Cliente("Luigi", "Bros", "8888-5678", "luigi@nintendo.com", false);

        assertEquals(0, c.getId());
        assertEquals("Luigi", c.getNombres());
        assertEquals("Luigi Bros", c.getNombreCompleto());
        assertFalse(c.isActivo());
    }

    @Test
    @DisplayName("Debe concatenar nombre completo correctamente")
    void testNombreCompleto() {
        Cliente c = new Cliente();
        c.setNombres("  Ana Sofia  ");
        c.setApellidos("  Perez Lopez  ");

        assertEquals("Ana Sofia Perez Lopez", c.getNombreCompleto());
    }

    @Test
    @DisplayName("Debe generar toString legible para ComboBox JavaFX")
    void testToStringFormato() {
        Cliente c = new Cliente(10, "Carlos", "Mendoza", "8888-9999", "carlos@mail.com", true);

        assertEquals("Carlos Mendoza (carlos@mail.com)", c.toString());
    }

    @Test
    @DisplayName("Debe verificar igualdad por ID cuando este asignado")
    void testEqualsAndHashCode() {
        Cliente c1 = new Cliente(5, "Roberto", "Gomez", "8888-1111", "r@mail.com", true);
        Cliente c2 = new Cliente(5, "Roberto", "Gomez", "8888-1111", "r@mail.com", true);
        Cliente c3 = new Cliente(6, "Roberto", "Gomez", "8888-1111", "otro@mail.com", true);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
