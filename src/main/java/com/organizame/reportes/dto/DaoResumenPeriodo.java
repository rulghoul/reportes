package com.organizame.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DaoResumenPeriodo {
    private String modelo;
    private Integer cantidad;
    private String mesAnio;
    private String marca;
    private String fabricante;
    private String segmento;
}
