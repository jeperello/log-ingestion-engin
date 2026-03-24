package com.virtualthread.log_ingestion_engine.core.controller;

import com.virtualthread.log_ingestion_engine.core.repository.LogBuffer;
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
    public ResponseEntity<Map<String,String>> ingest(@RequestBody IngestionRequest request) {
        LogProducerI producer = factory.getProducer(request.engineType());

        // Lanzamos un Hilo Virtual "Orquestador" que se encarga de disparar la producción.
        // Esto libera al hilo de la petición HTTP INSTANTÁNEAMENTE.
        Thread.ofVirtual().start(() -> {
            producer.produce(request.count());
        });

        return ResponseEntity.ok(Map.of(
                "message", "Ingesta iniciada con éxito",
                "engine", producer.getEngineName(),
                "status", "ASYNCHRONOUS_PROCESSING"
        ));
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of(
                "pendingLogs", buffer.getPendingCount(),
                "activeThreads", Thread.activeCount() // Hilos de plataforma reales
        );
    }
}
