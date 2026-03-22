package com.virtualthread.log_ingestion_engine.core;

import com.virtualthread.log_ingestion_engine.core.dto.LogEntry;
import com.virtualthread.log_ingestion_engine.core.repository.LogBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LogBufferTest {

    private LogBuffer logBuffer;

    @BeforeEach
    void setUp() {
        logBuffer = new LogBuffer();
    }

    @Test
    @DisplayName("Debe permitir encolar un log y luego recuperarlo en un batch")
    void shouldEnqueueAndDequeueLog() throws InterruptedException {
        // Arrange (Preparar)
        LogEntry entry = new LogEntry(
                UUID.randomUUID().toString(),
                "Mensaje de prueba",
                "INFO",
                LocalDateTime.now()
        );

        // Act (Actuar)
        logBuffer.enqueue(entry);
        List<LogEntry> batch = logBuffer.dequeueBatch(10);

        // Assert (Afirmar)
        assertThat(batch)
                .hasSize(1)
                .containsExactly(entry);

        assertThat(logBuffer.getPendingCount()).isZero();
    }

    @Test
    @DisplayName("Debe vaciar la cola correctamente cuando se pide un batch mayor al tamaño actual")
    void shouldHandleBatchLargerThanQueue() throws InterruptedException {
        // Arrange
        logBuffer.enqueue(new LogEntry("1", "Log 1", "DEBUG", LocalDateTime.now()));
        logBuffer.enqueue(new LogEntry("2", "Log 2", "DEBUG", LocalDateTime.now()));

        // Act
        List<LogEntry> batch = logBuffer.dequeueBatch(100);

        // Assert
        assertThat(batch).hasSize(2);
        assertThat(logBuffer.getPendingCount()).isZero();
    }
}