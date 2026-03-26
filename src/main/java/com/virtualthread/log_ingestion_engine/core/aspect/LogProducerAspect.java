package com.virtualthread.log_ingestion_engine.core.aspect;

import com.virtualthread.log_ingestion_engine.core.service.producer.LogProducerI;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class LogProducerAspect {
/*
    @Before("execution(* com.virtualthread.log_ingestion_engine.core.service..*.produce(int)) && args(count)")
    public void logBeforeProduction(JoinPoint joinPoint, int count) {
        Object target = joinPoint.getTarget();
        if (target instanceof LogProducerI producer) {
            log.info("🚀 [AOP] Iniciando producción de: {} logs", count);
            log.info("⚙️  Usando motor: {}", producer.getEngineName());
        }
    }
*/
    // "Around" nos permite ejecutar código antes Y después del método
    @Around("execution(* com.virtualthread.log_ingestion_engine.core.service..*.produce(int)) && args(count)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, int count) throws Throwable {
        LogProducerI producer = (LogProducerI) joinPoint.getTarget();

        log.info("🚀 [AOP] Iniciando producción de {} logs con: {}", count, producer.getEngineName());

        // StopWatch es una utilidad de Spring para medir tiempos de forma limpia
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // AQUÍ se ejecuta el método real (produce)
        Object result = joinPoint.proceed();

        stopWatch.stop();

        log.info("✅ [AOP] Finalizado en {} ms usando hilos de tipo: {}",
                stopWatch.getTotalTimeMillis(),
                producer.getEngineName());

        return result;
    }
}
