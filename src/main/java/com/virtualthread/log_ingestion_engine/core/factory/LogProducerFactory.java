package com.virtualthread.log_ingestion_engine.core.factory;

import com.virtualthread.log_ingestion_engine.core.service.producer.LogProducerI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LogProducerFactory {

    private final List<LogProducerI> producers;

    public LogProducerI getProducer(String type) {
        return producers.stream()
                .filter(p -> p.getEngineName().toLowerCase().contains(type.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Motor no soportado: " + type));
    }
}