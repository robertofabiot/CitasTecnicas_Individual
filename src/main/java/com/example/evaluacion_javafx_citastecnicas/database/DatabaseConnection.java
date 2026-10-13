package com.example.evaluacion_javafx_citastecnicas.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Clase centralizada de conexion a PostgreSQL / NeonDB.
 * Cumple con los requerimientos tecnicos de la evaluacion:
 * - Conexion centralizada gestionada con JDBC nativo.
 * - Uso demostrable de Connection, Statement y ResultSet.
 * - Ejecucion de sentencia SQL fija 'SELECT 1' con Statement para verificar el enlace.
 * - Cierre seguro de recursos mediante bloques try-with-resources.
 */
public class DatabaseConnection {

    private static String url;
    private static String user;
    private static String password;
    private static boolean driverLoaded = false;

    static {
        loadDriver();
        loadConfiguration();
    }

    private static void loadDriver() {
        if (!driverLoaded) {
            try {
                Class.forName("org.postgresql.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                System.err.println("❌ No se encontro el driver de PostgreSQL en el classpath: " + e.getMessage());
            }
        }
    }

    /**
     * Carga las credenciales desde el archivo database.properties o database.properties.example.
     */
    public static synchronized void loadConfiguration() {
        Properties props = new Properties();
        boolean loaded = false;

        // 1. Priorizar lectura directa de archivo fisico para reflejar cambios inmediatos
        File[] posiblesArchivos = new File[]{
                new File("src/main/resources/database.properties"),
                new File("database.properties"),
                new File("src/main/resources/database.properties.example"),
                new File("database.properties.example")
        };

        for (File archivo : posiblesArchivos) {
            if (archivo.exists() && archivo.canRead()) {
                try (FileInputStream fis = new FileInputStream(archivo)) {
                    props.load(fis);
                    loaded = true;
                    break;
                } catch (Exception ignored) {
                }
            }
        }

        // 2. Si no se encontro en disco, cargar desde classpath
        if (!loaded) {
            try (InputStream in = DatabaseConnection.class.getResourceAsStream("/database.properties")) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (Exception ignored) {
            }
        }

        if (loaded) {
            url = props.getProperty("db.url", "").trim();
            user = props.getProperty("db.user", "").trim();
            password = props.getProperty("db.password", "").trim();

            // Auto-parsear si el usuario pego la URI directa completa de Neon:
            // postgresql://usuario:contrasena@host/bd?params
            if (url.startsWith("postgresql://") || url.startsWith("jdbc:postgresql://")) {
                String cleanUrl = url.startsWith("jdbc:") ? url.substring(5) : url;
                if (cleanUrl.contains("@")) {
                    try {
                        java.net.URI uri = new java.net.URI(cleanUrl);
                        String userInfo = uri.getUserInfo();
                        if (userInfo != null && userInfo.contains(":")) {
                            String[] userParts = userInfo.split(":", 2);
                            if (user.isEmpty() || user.contains("[PEGA_AQUI")) {
                                user = userParts[0];
                            }
                            if (password.isEmpty() || password.contains("[PEGA_AQUI")) {
                                password = userParts[1];
                            }
                        }
                        String host = uri.getHost();
                        int port = uri.getPort();
                        String path = uri.getPath();
                        String query = uri.getQuery();

                        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(host);
                        if (port > 0) {
                            jdbc.append(":").append(port);
                        }
                        if (path != null && !path.isEmpty()) {
                            jdbc.append(path);
                        }
                        if (query != null && !query.isEmpty()) {
                            jdbc.append("?").append(query);
                        }
                        url = jdbc.toString();
                    } catch (Exception ignored) {
                    }
                } else if (!url.startsWith("jdbc:")) {
                    url = "jdbc:" + url;
                }
            }
        } else {
            System.err.println("⚠️ No se pudo cargar database.properties. Revisa src/main/resources/database.properties");
        }
    }

    /**
     * Obtiene una conexion activa a la base de datos PostgreSQL/NeonDB.
     *
     * @return Connection lista para usarse.
     * @throws SQLException si las credenciales son invalidas o no hay enlace a la red.
     */
    public static Connection getConnection() throws SQLException {
        if (url == null || url.isEmpty() || url.contains("[PEGA_AQUI")) {
            throw new SQLException(
                    "Cadena de conexion no configurada. Por favor coloca tu link de NeonDB en 'src/main/resources/database.properties'."
            );
        }

        if (user != null && !user.isEmpty() && !user.contains("[PEGA_AQUI")) {
            return DriverManager.getConnection(url, user, password);
        } else {
            // NeonDB suele incluir user y password directamente en la cadena jdbc URL
            return DriverManager.getConnection(url);
        }
    }

    /**
     * Prueba la conexion con la base de datos ejecutando una sentencia SQL fija sin entradas de usuario.
     * Requisito oficial: "Utilizar Statement al menos una vez para una sentencia SQL fija sin entradas del usuario (SELECT 1)".
     *
     * @return true si la consulta SELECT 1 responde exitosamente; false en caso contrario.
     */
    public static boolean testConnection() {
        String sql = "SELECT 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int valor = rs.getInt(1);
                System.out.println("✅ Conexión exitosa a PostgreSQL/NeonDB. Test 'SELECT 1' respondio: " + valor);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Fallo en testConnection(): " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica que tablas de la evaluacion existen en el esquema public.
     */
    public static boolean checkTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN ('cliente', 'cita_tecnica')")) {
            int count = 0;
            while (rs.next()) {
                System.out.println("   - Tabla detectada: " + rs.getString("table_name"));
                count++;
            }
            return count >= 2;
        } catch (SQLException e) {
            System.err.println("Error al verificar tablas: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ejecuta el script gestion_citas.sql para crear las tablas en NeonDB si aun no existen.
     */
    public static void executeInitScript() {
        File script = new File("gestion_citas.sql");
        if (!script.exists()) {
            System.out.println("No se encontro el archivo gestion_citas.sql en la raiz del proyecto.");
            return;
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(script))) {

            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                String trimmed = linea.trim();
                if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                    continue;
                }
                sb.append(linea).append("\n");
                if (trimmed.endsWith(";")) {
                    stmt.execute(sb.toString());
                    sb.setLength(0);
                }
            }
            System.out.println("✅ Script gestion_citas.sql ejecutado correctamente en NeonDB.");
        } catch (Exception e) {
            System.err.println("❌ Error al ejecutar gestion_citas.sql: " + e.getMessage());
        }
    }

    /**
     * Metodo main para probar rapidamente la conexion desde consola o IntelliJ IDEA.
     */
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Diagnostico de Conexion: TecnoAsistencia / Neon ");
        System.out.println("=================================================");
        loadConfiguration();
        boolean ok = testConnection();
        if (ok) {
            System.out.println(">> Verificando estado de tablas en NeonDB:");
            boolean tablasListas = checkTables();
            if (!tablasListas) {
                System.out.println(">> Creando tablas y datos iniciales desde gestion_citas.sql...");
                executeInitScript();
                checkTables();
            } else {
                System.out.println(">> Las tablas cliente y cita_tecnica ya estan listas.");
            }
            System.out.println(">> Todo listo para que los demas integrantes desarrollen los DAOs.");
        } else {
            System.out.println(">> Recuerda colocar tu connection string en src/main/resources/database.properties");
        }
    }
}
