# Guía de Trabajo y Pull Requests: Sistema de Gestión de Citas Técnicas

* **Asignatura:** Programación de Aplicaciones de Escritorio — UAM
* **Docente:** José A. Durán García
* **Proyecto:** `Evaluacion_JavaFX_CitasTecnicas`
* **Tecnologías:** JavaFX, PostgreSQL, JDBC nativo, Git/GitHub

---

## Equipo y Distribución de Roles

| Integrante | Rol Principal | Área de Responsabilidad |
| :--- | :--- | :--- |
| **Integrante 1** | Líder DB & Core JDBC | Script SQL, dependencias Maven, conexión JDBC singleton (`DatabaseConnection`), `SELECT 1` con `Statement`. |
| **Integrante 2** | Backend Clientes | Modelo `Cliente`, `ClienteDAO`, búsquedas case-insensitive y validación de correo único. |
| **Integrante 3** | Backend Citas | Modelo `CitaTecnica`, `CitaTecnicaDAO`, consulta relacional con `INNER JOIN` y filtros de citas. |
| **Integrante 4** | Frontend JavaFX & UX | Vistas FXML (`TabPane`), `TableView`, sincronización en caliente, alertas y diálogos de confirmación. |

---

## Guía Cronológica de Pull Requests

### Sesión 1: Lunes 12 de Octubre

#### **PR #1: Infraestructura de Base de Datos y Conexión JDBC**
* **Rama:** `feature/db-connection`
* **Responsable:** **Integrante 1**
* **Objetivos:**
  * Configurar dependencia de PostgreSQL en `pom.xml` y habilitar `requires java.sql;` en `module-info.java`.
  * Crear script `gestion_citas.sql` con tablas `cliente` y `cita_tecnica` (FK con `ON DELETE RESTRICT`).
  * Implementar clase singleton `DatabaseConnection` con try-with-resources.
  * Implementar verificación de conexión usando `Statement` con `SELECT 1` (requisito obligatorio de rúbrica).
* **Commits recomendados:**
  1. `build: agregar driver postgresql en pom.xml y modulo java.sql`
  2. `feat(db): crear script gestion_citas.sql con tablas cliente y cita_tecnica`
  3. `feat(jdbc): implementar DatabaseConnection con Statement para test SELECT 1`

---

#### **PR #2: Modelo Cliente, DAO y Búsqueda**
* **Rama:** `feature/cliente-dao`
* **Responsable:** **Integrante 2**
* **Objetivos:**
  * Crear entidad `Cliente` (id, nombres, apellidos, telefono, correo, activo).
  * Implementar `ClienteDAO` con operaciones CRUD usando exclusivamente `PreparedStatement` (RF02).
  * Implementar método de búsqueda case-insensitive por nombres, apellidos o correo (RF05).
  * Implementar validación de existencia de correo para evitar duplicados (RF07).
* **Commits recomendados:**
  1. `feat(model): crear entidad Cliente con constructores y getters`
  2. `feat(dao): implementar ClienteDAO con operaciones CRUD y PreparedStatement`
  3. `feat(dao): agregar busqueda case-insensitive y validacion de correo duplicado`

---

### Sesión 2: Martes 13 de Octubre

#### **PR #3: Modelo Cita Técnica, DAO con Relación (JOIN) y Filtros**
* **Rama:** `feature/citas-dao`
* **Responsable:** **Integrante 3**
* **Objetivos:**
  * Crear entidad `CitaTecnica` con campo para nombre completo del cliente.
  * Implementar `CitaTecnicaDAO` con operaciones CRUD (RF03).
  * Implementar consulta con `INNER JOIN cliente` para mostrar el nombre del cliente en la cita (RF04).
  * Implementar método de filtrado parametrizado por estado y/o fecha con opción de limpiar filtros (RF06).
* **Commits recomendados:**
  1. `feat(model): crear entidad CitaTecnica y atributos de relacion`
  2. `feat(dao): implementar CitaTecnicaDAO con CRUD y consulta JOIN de cliente`
  3. `feat(dao): implementar filtros parametrizados por estado y fecha`

---

#### **PR #4: Interfaz Gráfica JavaFX, TableViews y Controladores**
* **Rama:** `feature/javafx-ui`
* **Responsable:** **Integrante 4**
* **Objetivos:**
  * Diseñar vista FXML con `TabPane` para separar Clientes y Citas Técnicas.
  * Configurar `TableView<Cliente>` y `TableView<CitaTecnica>` con sus columnas correspondientes.
  * Poblar `ComboBox<Cliente>` en el formulario de citas para asociar clientes existentes (RF03).
  * Implementar actualización reactiva de las tablas tras crear, editar o eliminar (RF09).
* **Commits recomendados:**
  1. `feat(ui): disenar vista principal FXML con pestanas para Clientes y Citas`
  2. `feat(ui): configurar TableViews y columnas con PropertyValueFactory`
  3. `feat(controller): vincular formularios y poblar ComboBox con clientes existentes`

---

### Sesión 3: Miércoles 14 - Jueves 15 de Octubre

#### **PR #5: Validaciones de Negocio, Confirmaciones y Manejo de Errores**
* **Rama:** `feature/validations-alerts`
* **Responsable:** **Integrante 2** (apoyo de **Integrante 4**)
* **Objetivos:**
  * Validar campos obligatorios, formato de correo con regex y estados permitidos (RF07).
  * Solicitar confirmación modal (`Alert.AlertType.CONFIRMATION`) antes de eliminar registros (RF08).
  * **Regla estricta:** Bloquear eliminación física de clientes con citas asociadas y mostrar alerta explicativa (RF08).
  * Capturar excepciones `SQLException` mostrando mensajes comprensibles sin caídas de la app.
* **Commits recomendados:**
  1. `feat(validation): agregar validacion de campos obligatorios y regex de correo`
  2. `feat(ux): incorporar dialogos modales de confirmacion antes de eliminar`
  3. `fix(integrity): bloquear eliminacion de cliente con citas asociadas y mostrar alerta`

---

#### **PR #6: Documentación README, Credenciales y Pruebas Finales**
* **Rama:** `docs/readme-setup`
* **Responsable:** **Integrante 1** (apoyo de **Integrante 3**)
* **Objetivos:**
  * Configurar lectura de credenciales mediante `database.properties` sin exponer passwords en Git.
  * Completar `README.md` con nombres, usuarios GitHub de los 4 integrantes, guía de PostgreSQL y capturas.
  * Probar y verificar las 11 pruebas de aceptación obligatorias antes de la defensa final.
* **Commits recomendados:**
  1. `docs: documentar configuracion local de base de datos y credenciales externas`
  2. `docs: completar README con tabla de integrantes, guia de ejecucion y capturas`
  3. `test: verificar flujo de las 11 pruebas de aceptacion antes de la defensa`

---

## Checklist de Pruebas de Aceptación (Defensa Docente)

- [ ] 1. Registrar cliente y comprobar aparición en `TableView`.
- [ ] 2. Intentar registrar correo duplicado y comprobar mensaje de rechazo.
- [ ] 3. Modificar teléfono del cliente y verificar que el cambio persiste en PostgreSQL.
- [ ] 4. Registrar cita asociada al cliente y verificar que su nombre completo aparece en la tabla.
- [ ] 5. Buscar cliente por coincidencia parcial de apellido (insensible a mayúsculas/minúsculas).
- [ ] 6. Filtrar citas por estado y fecha, y presionar limpiar filtros para recuperar registros.
- [ ] 7. Cambiar estado de una cita a `Finalizada`.
- [ ] 8. Intentar eliminar un cliente con citas asociadas (debe rechazarse explicando el motivo).
- [ ] 9. Eliminar la cita técnica y después el cliente (confirmando cada diálogo).
- [ ] 10. Cerrar y abrir la aplicación para verificar persistencia real en PostgreSQL.
- [ ] 11. Revisar historial de Git con commits sustantivos en las 3 fechas (12, 13 y 14 de octubre) por los 4 autores.