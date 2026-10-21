package com.example.evaluacion_javafx_citastecnicas.concurrencia;

import com.example.evaluacion_javafx_citastecnicas.ProcesosController;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para verificar los requisitos de concurrencia de la Unidad III:
 * - Creación de hilos mediante Thread y Runnable.
 * - Comprobación de hilos daemon y nomenclatura.
 * - Detención controlada con interrupt() e isInterrupted().
 * - Ejecución concurrente e independiente de múltiples hilos con diferentes tiempos.
 */
public class HilosConcurrenciaTest {

    @BeforeAll
    static void iniciarFx() {
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException ignored) {
            // JavaFX ya estaba iniciado
        }
    }

    @Test
    @DisplayName("IL3.1: Creación y ejecución de un hilo mediante Thread y Runnable")
    void testCreacionYEjecucionDeHiloConRunnable() throws InterruptedException {
        AtomicBoolean ejecutado = new AtomicBoolean(false);
        AtomicBoolean esDaemon = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable tarea = () -> {
            ejecutado.set(true);
            esDaemon.set(Thread.currentThread().isDaemon());
            latch.countDown();
        };

        Thread hilo = new Thread(tarea);
        hilo.setName("Hilo-Prueba-Unitaria");
        hilo.setDaemon(true);
        hilo.start();

        assertTrue(latch.await(3, TimeUnit.SECONDS), "El hilo debió completar la ejecución.");
        assertTrue(ejecutado.get(), "La tarea Runnable debió haberse ejecutado.");
        assertTrue(esDaemon.get(), "El hilo debió haberse marcado como Daemon.");
        assertEquals("Hilo-Prueba-Unitaria", hilo.getName(), "El nombre del hilo debe coincidir.");
    }

    @Test
    @DisplayName("IL3.3: Gestión del ciclo de vida y detención controlada con interrupt()")
    void testDetencionControladaConInterrupt() throws InterruptedException {
        CountDownLatch iniciado = new CountDownLatch(1);
        AtomicBoolean interrumpidoDetectado = new AtomicBoolean(false);
        AtomicInteger iteraciones = new AtomicInteger(0);

        Runnable tarea = () -> {
            iniciado.countDown();
            try {
                for (int i = 1; i <= 100; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    Thread.sleep(100);
                    iteraciones.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                interrumpidoDetectado.set(true);
            }
        };

        Thread hilo = new Thread(tarea, "Hilo-Detencion-Prueba");
        hilo.setDaemon(true);
        hilo.start();

        assertTrue(iniciado.await(2, TimeUnit.SECONDS));
        Thread.sleep(150);

        hilo.interrupt();
        hilo.join(2000);

        assertFalse(hilo.isAlive(), "El hilo debió haber finalizado tras la interrupción.");
        assertTrue(interrumpidoDetectado.get(), "El hilo debió haber capturado InterruptedException y marcado interrupción.");
        assertTrue(iteraciones.get() < 100, "El proceso debió haberse detenido antes de completar los 100 pasos.");
    }

    @Test
    @DisplayName("Reto final: Dos hilos concurrentes e independientes con diferentes velocidades")
    void testHilosConcurrentesIndependientes() throws InterruptedException {
        CountDownLatch clientesLatch = new CountDownLatch(10);
        CountDownLatch citasLatch = new CountDownLatch(5);
        AtomicInteger contadorClientes = new AtomicInteger(0);
        AtomicInteger contadorCitas = new AtomicInteger(0);

        // Hilo 1: 10 iteraciones a 20ms
        Runnable tareaClientes = () -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                    Thread.sleep(20);
                    contadorClientes.incrementAndGet();
                    clientesLatch.countDown();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // Hilo 2: 5 iteraciones a 35ms
        Runnable tareaCitas = () -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                    Thread.sleep(35);
                    contadorCitas.incrementAndGet();
                    citasLatch.countDown();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread hilo1 = new Thread(tareaClientes, "Hilo-Clientes-Test");
        Thread hilo2 = new Thread(tareaCitas, "Hilo-Citas-Test");
        hilo1.setDaemon(true);
        hilo2.setDaemon(true);

        hilo1.start();
        hilo2.start();

        assertTrue(clientesLatch.await(3, TimeUnit.SECONDS), "El hilo de clientes debió completar.");
        assertTrue(citasLatch.await(3, TimeUnit.SECONDS), "El hilo de citas debió completar.");

        assertEquals(10, contadorClientes.get());
        assertEquals(5, contadorCitas.get());
    }

    @Test
    @DisplayName("ProcesosController: detenerAlCerrar detiene los hilos activos")
    void testProcesosControllerDetenerAlCerrar() throws Exception {
        ProcesosController controller = new ProcesosController();
        assertDoesNotThrow(controller::detenerAlCerrar);

        // Inyectar un hilo activo de prueba
        CountDownLatch runningLatch = new CountDownLatch(1);
        Thread mockHilo = new Thread(() -> {
            runningLatch.countDown();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Mock-Hilo");
        mockHilo.setDaemon(true);
        mockHilo.start();

        assertTrue(runningLatch.await(2, TimeUnit.SECONDS));

        Field f1 = ProcesosController.class.getDeclaredField("hiloClientes");
        f1.setAccessible(true);
        f1.set(controller, mockHilo);

        controller.detenerAlCerrar();
        mockHilo.join(1000);
        assertFalse(mockHilo.isAlive(), "El hilo debió haber sido interrumpido y terminado.");
    }
}
