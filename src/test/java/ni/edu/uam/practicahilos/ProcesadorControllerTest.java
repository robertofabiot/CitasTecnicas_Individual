package ni.edu.uam.practicahilos;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la práctica guiada de la Universidad Americana (UAM):
 * ni.edu.uam.practicahilos.ProcesadorController
 */
public class ProcesadorControllerTest {

    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    @DisplayName("ProcesadorController: estructura y ciclo de vida de los hilos")
    void testProcesadorControllerCicloDeVida() throws Exception {
        ProcesadorController controller = new ProcesadorController();

        // Verificar existencia de métodos requeridos por la práctica
        assertNotNull(ProcesadorController.class.getDeclaredMethod("iniciarProceso"));
        assertNotNull(ProcesadorController.class.getDeclaredMethod("detenerProceso"));
        assertNotNull(ProcesadorController.class.getDeclaredMethod("reiniciarProceso"));
        assertNotNull(ProcesadorController.class.getDeclaredMethod("detenerAlCerrar"));

        // Verificar campos de hilos
        Field hiloProcesoField = ProcesadorController.class.getDeclaredField("hiloProceso");
        hiloProcesoField.setAccessible(true);

        // detenerAlCerrar sin hilo activo no debe arrojar excepción
        assertDoesNotThrow(controller::detenerAlCerrar);
    }
}
