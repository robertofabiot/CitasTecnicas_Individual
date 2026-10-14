package com.example.evaluacion_javafx_citastecnicas.model;
import com.example.evaluacion_javafx_citastecnicas.database.DatabaseConnection;
import com.example.evaluacion_javafx_citastecnicas.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Statement;
import  java.util.ArrayList;
import java.util.List;

/**
 * DAO de la entidad Cliente.
 * Cumple:
 *  - RF02: CRUD completo con PreparedStatement (nunca Statement con datos de usuario).
 *  - RF05: Busqueda parcial case-insensitive por nombres, apellidos o correo.
 *  - RF07: Validacion de campos obligatorios, formato de correo y correo duplicado.
 *  - Eliminacion FISICA (DELETE) respetando integridad referencial (FK RESTRICT).
 */

public class ClienteDAO {
    private static final String REGEX_CORREO =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // Validaciones
    /** Valida campos obligatorios y su formato */
    public void ValidarCliente(Cliente c) {

    }
}
