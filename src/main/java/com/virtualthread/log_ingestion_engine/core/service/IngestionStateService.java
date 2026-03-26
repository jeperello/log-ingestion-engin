package com.virtualthread.log_ingestion_engine.core.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IngestionStateService {
    // false = libre, true = ocupado
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public boolean tryStart() {
        return isProcessing.compareAndSet(false, true);
    }

    public void finish() {
        isProcessing.set(false);
    }

    public boolean isBusy() {
        return isProcessing.get();
    }
}