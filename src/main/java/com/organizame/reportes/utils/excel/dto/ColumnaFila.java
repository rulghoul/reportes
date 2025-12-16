package com.organizame.reportes.utils.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColumnaFila {
    private Posicion posicion;
    private List<Celda> valores;
}
