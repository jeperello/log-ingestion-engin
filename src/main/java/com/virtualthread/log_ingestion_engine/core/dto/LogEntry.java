package com.virtualthread.log_ingestion_engine.core.dto;
import java.time.LocalDateTime;

/**
 * Usamos un Record porque es inmutable por naturaleza.
 * En sistemas concurrentes, esto garantiza que los hilos
 * no corrompan los datos al leerlos simultáneamente.
 */
public record LogEntry(
        String id,
        String message,
        String level,
        LocalDateTime timestamp
) {}