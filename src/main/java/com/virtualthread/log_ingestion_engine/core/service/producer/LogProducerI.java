package com.virtualthread.log_ingestion_engine.core.service.producer;

public interface LogProducerI {
    void produce(int count);
    String getEngineName();
}
