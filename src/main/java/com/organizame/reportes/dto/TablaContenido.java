package com.organizame.reportes.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.function.Predicate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TablaContenido {

    private String nombreTabla;
    private List<FilaTabla> datos;
    private Double total;
    private Double totalStellantis;

}