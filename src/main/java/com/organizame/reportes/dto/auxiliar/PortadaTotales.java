package com.organizame.reportes.dto.auxiliar;

import com.organizame.reportes.dto.FilaTabla;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class PortadaTotales {
    private String mes;
    private Integer volumen;
    private Integer totales;
    private double porcentaje;

    public FilaTabla getFilaTabla(){
        return new FilaTabla("Estandar", List.of(mes, volumen, totales, porcentaje));
    }
}
