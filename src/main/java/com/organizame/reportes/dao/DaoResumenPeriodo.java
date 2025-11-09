package com.organizame.reportes.dao;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

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
