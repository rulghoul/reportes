package com.organizame.reportes.dto.auxiliar;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Acumulado {
    private String fabricante;
    private Integer lineas;
    private Integer volumen;
    private double peso;
    private double porcentajeIndustria;
}
