package com.organizame.reportes.utils.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
public class Posicion {
    private int col;
    private int row;
}
