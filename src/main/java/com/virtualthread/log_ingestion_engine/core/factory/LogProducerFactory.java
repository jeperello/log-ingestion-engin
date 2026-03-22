package com.virtualthread.log_ingestion_engine.core.factory;

import com.virtualthread.log_ingestion_engine.core.service.LogProducerI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LogProducerFactory {

    // Spring inyecta automáticamente TODAS las implementaciones de LogProducer en esta lista
    private final List<LogProducerI> producers;

    public LogProducerI getProducer(String type) {
        return producers.stream()
                .filter(p -> p.getEngineName().toLowerCase().contains(type.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Motor no soportado: " + type));
    }
}