package com.virtualthread.log_ingestion_engine.core.service;

public interface LogProducerI {
    void produce(int count);
    String getEngineName();
}
