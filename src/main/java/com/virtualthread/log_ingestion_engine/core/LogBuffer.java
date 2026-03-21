package com.virtualthread.log_ingestion_engine.core;

import com.virtualthread.log_ingestion_engine.core.model.LogEntry;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class LogBuffer {

    // Capacidad limitada para demostrar Backpressure (Presión hacia atrás)
    // Si la cola se llena, el productor tiene que esperar.
    private final BlockingQueue<LogEntry> queue = new LinkedBlockingQueue<>(5000);

    /**
     * El Productor usa este método.
     * .put() es bloqueante: si no hay espacio, el hilo espera.
     */
    public void enqueue(LogEntry entry) throws InterruptedException {
        queue.put(entry);
    }

    /**
     * El Consumidor usa este método para procesar en lotes (Batching).
     * drainTo es atómico y muy eficiente para sacar múltiples elementos.
     */
    public List<LogEntry> dequeueBatch(int batchSize) {
        List<LogEntry> batch = new ArrayList<>(batchSize);
        queue.drainTo(batch, batchSize);
        return batch;
    }

    public int getPendingCount() {
        return queue.size();
    }
}
