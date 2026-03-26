package com.virtualthread.log_ingestion_engine.core.repository;

import com.virtualthread.log_ingestion_engine.core.dto.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class LogBuffer {
    // Capacidad limitada para demostrar Backpressure (Presión hacia atrás)
    private int capacity = 3000;
    // Si la cola se llena, el productor tiene que esperar.
    private final BlockingQueue<LogEntry> queue = new LinkedBlockingQueue<>(capacity);

    // Estado atómico para evitar la repetición del log en múltiples hilos
    private final AtomicBoolean backpressureLogged = new AtomicBoolean(false);

    /**
     * El Productor usa este método.
     * .put() es bloqueante: si no hay espacio, el hilo espera.
     */
    public void enqueue(LogEntry entry) throws InterruptedException {
        if (queue.remainingCapacity() == 0) {
            // compareAndSet intenta cambiar de false a true.
            // Solo el PRIMER hilo que lo logre entrará al if.
            if (backpressureLogged.compareAndSet(false, true)) {
                log.warn("⚠️ [BACKPRESSURE] La cola llegó a su límite ("+capacity+"). Hilos en espera...");
            }
        }
        queue.put(entry);
    }

    /**
     * El Consumidor usa este método para procesar en lotes (Batching).
     * drainTo es atómico y muy eficiente para sacar múltiples elementos.
     */
    public List<LogEntry> dequeueBatch(int batchSize) {
        List<LogEntry> batch = new ArrayList<>(batchSize);
        queue.drainTo(batch, batchSize);

        // Si sacamos elementos y la cola ya no está llena, reseteamos el aviso
        if (!batch.isEmpty() && queue.remainingCapacity() > 0) {
            backpressureLogged.set(false);
        }

        return batch;
    }

    public int getPendingCount() {
        return queue.size();
    }
}
