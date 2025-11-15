package com.organizame.reportes.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestOrigen {

    @NotBlank(message = "El origen no puede estar vacío")
    @Size(max = 100, message = "El origen no puede tener más de 100 caracteres")
    private String origen;

    @NotNull(message = "La fecha final no puede ser nula")
    @Past(message = "La fecha final debe ser una fecha pasada")
    private LocalDate mesFinal;

    @NotNull(message = "El mes de reporte no puede ser nulo")
    @Min(value = 2, message = "Los meses del reporte deben ser al menos 2")
    @Max(value = 36, message = "Los meses del reporte no puede ser mas de 36") // Ajusta según tus reglas de negocio
    private Integer mesReporte;

}
