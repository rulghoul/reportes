package com.organizame.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DaoPeriodo {

    private String fabicante;
    private String modelo;
    private Integer total;
    private double porcentaje;
    private String estilo;

    public DaoPeriodo(String fabricante, String modelo, Integer total, double porcentaje){
        this.estilo = "Estandar";
        this.modelo = modelo;
        this.total = total;
        this.porcentaje = porcentaje;
    }

    public List<Object> toListObject(){
        return List.of(modelo, total, porcentaje);
    }

    public FilaTabla toFilas(){
        return new FilaTabla(this.estilo, this.toListObject());
    }
}
