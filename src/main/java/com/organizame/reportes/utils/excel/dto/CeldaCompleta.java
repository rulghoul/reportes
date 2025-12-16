package com.organizame.reportes.utils.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CeldaCompleta extends Celda{
    private Posicion posicion;
}
