package com.virtualthread.log_ingestion_engine.core.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record IngestionRequest(
        @Min(value = 1, message = "Debe enviar al menos 1 log")
       @Max(value = 5000, message = "El máximo permitido por ráfaga es 5000 para proteger el sistema")
       int count,

       @NotBlank(message = "El tipo de motor es obligatorio")
       String engineType) {}
