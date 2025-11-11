package com.organizame.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DaoPeriodo {

    private String modelo;
    private double total;
    private double porcentaje;

    public List<Object> toListObject(){
        return List.of(modelo, total, porcentaje);
    }
}
