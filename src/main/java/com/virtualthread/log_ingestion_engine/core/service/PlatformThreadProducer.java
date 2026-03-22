package com.virtualthread.log_ingestion_engine.core.service;

import com.virtualthread.log_ingestion_engine.core.repository.LogBuffer;
import com.virtualthread.log_ingestion_engine.core.dto.LogEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformThreadProducer implements LogProducerI {
    private final LogBuffer buffer;

    @Override
    public void produce(int count) {
        for (int i = 0; i < count; i++) {
            // Creamos un hilo de plataforma por cada log
            new Thread(() -> {
                try {
                    LogEntry entry = new LogEntry(UUID.randomUUID().toString(),
                            "Log de Plataforma", "INFO", LocalDateTime.now());
                    buffer.enqueue(entry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @Override
    public String getEngineName() {
        return "Platform Threads";
    }
}
