package com.organizame.reportes.utils.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
public class Posicion {
    private int col;
    private int row;

    public void addRows(int rows){
        this.row = this.row + rows;
    }

    public void addCols(int cols){
        this.col = this.col + cols;
    }
}
