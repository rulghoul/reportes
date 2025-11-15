package com.organizame.reportes.dto.auxiliar;

import com.organizame.reportes.dto.FilaTabla;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.*;

@Data
@AllArgsConstructor
public class Acumulado {
    private String fabricante;
    private Integer lineas;
    private Integer volumen;
    private double peso;
    private double porcentajeIndustria;

    public FilaTabla getFilaTabla(){
        var estilo = fabricante.equalsIgnoreCase("Stellantis") ? "Stellantis" : "Estandar";
        return new FilaTabla(estilo, List.of(fabricante, lineas, volumen, peso, porcentajeIndustria));
    }
}
