package com.organizame.reportes.dto.auxiliar;

import com.organizame.reportes.dto.FilaTabla;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.DecimalFormat;
import java.util.*;

@Data
@AllArgsConstructor
public class Acumulado {
    private String fabricante;
    private Integer lineas;
    private Integer volumen;
    private Double peso;
    private Double porcentajeIndustria;

    public FilaTabla getFilaTabla(){
        var estilo = fabricante.equalsIgnoreCase("Stellantis") ? "Stellantis" : "Estandar";
        return new FilaTabla(estilo, List.of(fabricante, lineas, volumen, peso, porcentajeIndustria));
    }

    public FilaTabla getFilaTabla(DecimalFormat formato){
        var estilo = fabricante.equalsIgnoreCase("Stellantis") ? "Stellantis" : "Estandar";
        estilo = fabricante.equalsIgnoreCase("TOTAL") ? "TOTAL" : estilo;
        return new FilaTabla(estilo, List.of(fabricante, lineas, volumen
                , formato.format(peso)+"%", formato.format(porcentajeIndustria)+"%"));
    }
}
