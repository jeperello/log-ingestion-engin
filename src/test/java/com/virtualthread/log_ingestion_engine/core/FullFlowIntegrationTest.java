package com.virtualthread.log_ingestion_engine.core;

import com.virtualthread.log_ingestion_engine.core.factory.LogProducerFactory;
import com.virtualthread.log_ingestion_engine.core.repository.LogBuffer;
import com.virtualthread.log_ingestion_engine.core.service.LogProducerI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class FullFlowIntegrationTest {

    @Autowired
    private LogProducerFactory producerFactory;

    @Autowired
    private LogBuffer logBuffer;

    @Test
    @DisplayName("Flujo Completo: Los productores llenan la cola y el consumidor la vacía eventualmente")
    void fullIngestionFlowTest() {
        // 1. Arrange: Elegimos el motor de Virtual Threads y definimos carga
        LogProducerI producer = producerFactory.getProducer("virtual");
        int totalLogs = 500;

        // 2. Act: Iniciamos la producción masiva
        producer.produce(totalLogs);

        // 3. Assert: Verificamos que la cola NO está vacía inmediatamente (productores trabajando)
        assertThat(logBuffer.getPendingCount()).isGreaterThan(0);

        // 4. Await: Esperamos a que el Consumidor haga su trabajo (Máximo 10 segundos)
        // Esto es mucho mejor que un Thread.sleep porque termina apenas se cumple la condición
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> logBuffer.getPendingCount() == 0);

        // 5. Final Assert
        assertThat(logBuffer.getPendingCount()).isZero();
    }
}