package com.virtualthread.log_ingestion_engine.core.service;

import com.virtualthread.log_ingestion_engine.core.dto.LogEntry;
import com.virtualthread.log_ingestion_engine.core.repository.LogBuffer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogConsumer {

    private final LogBuffer logBuffer;
    private static final int BATCH_SIZE = 100;

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
        // Simulamos la latencia de guardar en una base de datos (I/O Bound)
        // Esto es lo que realmente probará la potencia de los Virtual Threads
        long delay = 150;
        Thread.sleep(delay);

        log.info("💾 [BATCH] Guardados {} logs en la DB simulada (Latencia: {}ms). Pendientes en cola: {}",
                logs.size(), delay, logBuffer.getPendingCount());
    }
}
