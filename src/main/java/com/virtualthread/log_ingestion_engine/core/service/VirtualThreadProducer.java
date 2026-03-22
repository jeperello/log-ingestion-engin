package com.virtualthread.log_ingestion_engine.core.service;
import com.virtualthread.log_ingestion_engine.core.LogBuffer;
import com.virtualthread.log_ingestion_engine.core.model.LogEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class VirtualThreadProducer implements LogProducerI {

    private final LogBuffer buffer;

    @Override
    public void produce(int count) {
        // Executor que crea un hilo virtual por tarea. Es ultra liviano.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                executor.submit(() -> {
                    try {
                        LogEntry entry = new LogEntry(UUID.randomUUID().toString(),
                                "Log Virtual", "INFO", LocalDateTime.now());
                        buffer.enqueue(entry);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
    }

    @Override
    public String getEngineName() {
        return "Virtual Threads";
    }
}