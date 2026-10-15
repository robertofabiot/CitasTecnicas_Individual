package com.example.evaluacion_javafx_citastecnicas;

import com.example.evaluacion_javafx_citastecnicas.dao.ClienteDAO;
import com.example.evaluacion_javafx_citastecnicas.model.Cliente;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionesTest {
    @Test void regexRechazaCorreosMalFormados() {
        ClienteDAO dao = new ClienteDAO();
        Cliente c = new Cliente("Ana", "Pérez", "8888", "ana@test.com", true);
        for (String correo : List.of("sin-arroba.com", "a@dominio", "a b@test.com", "a..b@test.com", ".a@test.com", "a@-dominio.com", "a@dominio..com")) {
            c.setCorreo(correo);
            assertThrows(IllegalArgumentException.class, () -> dao.validarCliente(c), correo);
        }
        c.setCorreo("nombre.apellido+etiqueta@sub.dominio.com");
        assertDoesNotThrow(() -> dao.validarCliente(c));
    }
}
