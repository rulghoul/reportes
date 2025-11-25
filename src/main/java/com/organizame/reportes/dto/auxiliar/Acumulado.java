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

    public FilaTabla getFilaTabla(DecimalFormat formatoEnteros, DecimalFormat formatoDecimales ){
        var estilo = fabricante.equalsIgnoreCase("Stellantis") ? "Stellantis" : "Estandar";
        estilo = fabricante.equalsIgnoreCase("TOTAL") ? "TOTAL" : estilo;
        return new FilaTabla(estilo, List.of(fabricante, lineas, formatoEnteros.format(volumen)
                , formatoDecimales.format(peso)+"%", formatoDecimales.format(porcentajeIndustria)+"%"));
    }
}
