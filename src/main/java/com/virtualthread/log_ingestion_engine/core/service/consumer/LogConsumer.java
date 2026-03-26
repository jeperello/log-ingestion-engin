package com.virtualthread.log_ingestion_engine.core.service.consumer;

import com.virtualthread.log_ingestion_engine.core.dto.LogEntry;
import com.virtualthread.log_ingestion_engine.core.repository.LogBuffer;
import com.virtualthread.log_ingestion_engine.core.service.IngestionStateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogConsumer implements DisposableBean {
    private final IngestionStateService stateService;
    private final LogBuffer logBuffer;
    private static final int BATCH_SIZE = 100;
    private volatile boolean running = true; // Flag para control de apagado
    private long startTime = 0;
    private int totalProcessedInSession = 0;
    /**
     * PostConstruct asegura que el consumidor arranque apenas Spring
     * termine de inicializar el bean.
     */
    @PostConstruct
    public void startConsuming() {
        // Lanzamos un Virtual Thread dedicado para este worker
        Thread.ofVirtual().name("log-consumer-worker").start(() -> {
            log.info("👷 Consumidor iniciado en un Virtual Thread. Esperando logs...");

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Intentamos sacar un lote de 100
                    List<LogEntry> batch = logBuffer.dequeueBatch(BATCH_SIZE);

                    if (!batch.isEmpty()) {
                        processLogs(batch);
                    } else {
                        // Si no hay nada, dormimos un poco para no quemar CPU (Busy-waiting)
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    log.error("❌ Consumidor interrumpido");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void processLogs(List<LogEntry> logs) throws InterruptedException {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
            totalProcessedInSession = 0;
        }
        // Simulo la latencia de una base de datos o sistema externo.
        long delay = 250;
        Thread.sleep(delay);

        totalProcessedInSession += logs.size();
        log.info("💾 [BATCH] Guardados {} logs en la DB simulada (Latencia: {}ms). Pendientes en cola: {}",
                logs.size(), delay, logBuffer.getPendingCount());
        // Contamos cuántos hay de cada uno en este lote de 100
        long virtualCount = logs.stream()
                .filter(l -> l.message().contains("Virtual"))
                .count();
        long platformCount = logs.stream()
                .filter(l -> l.message().contains("Plataform"))
                .count();
        log.info("💾 [BATCH] Procesados {} logs. (Virtual: {} | Platform: {}). Pendientes: {}",
                logs.size(), virtualCount, platformCount, logBuffer.getPendingCount());
        // Si después de procesar este lote la cola quedó en 0, terminó el proceso
        if (logBuffer.getPendingCount() == 0) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            stateService.finish();
            log.info("🏁 [LATENCY REPORT] Ingesta Completa de {} logs finalizada en {} ms.",
                    totalProcessedInSession, duration);

            // Reseteamos para la próxima ráfaga
            startTime = 0;
        }
    }

    @Override
    public void destroy() {
        log.warn("⚠️ [SHUTDOWN] Iniciando cierre elegante...");
        this.running = false;

        // Damos un tiempo de gracia para vaciar la cola
        int attempts = 0;
        while (logBuffer.getPendingCount() > 0 && attempts < 10) {
            log.info("⏳ [SHUTDOWN] Vaciando buffer: {} logs restantes...", logBuffer.getPendingCount());
            try {
                Thread.sleep(500); // Esperamos a que el bucle del worker procese
                attempts++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (logBuffer.getPendingCount() == 0) {
            log.info("✅ [SHUTDOWN] Buffer vaciado con éxito. Apagando.");
        } else {
            log.error("❌ [SHUTDOWN] Tiempo de gracia agotado. Se perdieron {} logs.", logBuffer.getPendingCount());
        }
    }
}
