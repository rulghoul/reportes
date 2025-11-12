package com.organizame.reportes.dto.auxiliar;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PortadaTotales {
    private String mes;
    private Integer volumen;
    private Integer totales;
    private double porcentaje;
}
