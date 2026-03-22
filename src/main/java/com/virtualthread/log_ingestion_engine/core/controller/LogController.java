package com.virtualthread.log_ingestion_engine.core.controller;

import com.virtualthread.log_ingestion_engine.core.LogBuffer;
import com.virtualthread.log_ingestion_engine.core.dto.request.IngestionRequest;
import com.virtualthread.log_ingestion_engine.core.factory.LogProducerFactory;
import com.virtualthread.log_ingestion_engine.core.service.LogProducerI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin // Para que Angular pueda conectar sin problemas de CORS
public class LogController {

    private final LogProducerFactory factory;
    private final LogBuffer buffer;

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestBody IngestionRequest request) {
        LogProducerI producer = factory.getProducer(request.engineType());

        // Ejecutamos la producción (esto lanza los hilos)
        producer.produce(request.count());

        return ResponseEntity.ok("Iniciada ingesta con: " + producer.getEngineName());
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of(
                "pendingLogs", buffer.getPendingCount(),
                "activeThreads", Thread.activeCount() // Hilos de plataforma reales
        );
    }
}
