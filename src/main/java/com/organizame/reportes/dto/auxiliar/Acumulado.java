package com.organizame.reportes.dto.auxiliar;

import com.organizame.reportes.dto.FilaTabla;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.*;

@Slf4j
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
        estilo = fabricante.equalsIgnoreCase("TOTAL") ? "TOTAL" : estilo;
        return new FilaTabla(estilo, List.of(fabricante, lineas, volumen, peso, porcentajeIndustria));
    }

    public FilaTabla getFilaTabla(DecimalFormat formato){
        var estilo = fabricante.equalsIgnoreCase("Stellantis") ? "Stellantis" : "Estandar";
        estilo = fabricante.equalsIgnoreCase("TOTAL") ? "TOTAL" : estilo;
        return new FilaTabla(estilo, List.of(fabricante, lineas, volumen
                , formato.format(peso)+"%", formato.format(porcentajeIndustria)+"%"));
    }

    @Override
    public boolean equals(Object obj){
        log.info("Se compara {} contra {}", obj, this);
        if(obj instanceof Acumulado){
            if( ((Acumulado) obj).getFabricante().equalsIgnoreCase(this.fabricante)){
                return true;
            }
            return false;
        }
        return false;
    }
}
