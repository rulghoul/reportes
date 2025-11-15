package com.organizame.reportes.utils.excel.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;


@Data
public class PosicionGrafica extends Posicion {

    private int ancho;
    private int alto;

    public PosicionGrafica(Posicion posicion, int ancho, int alto){
        super(posicion.getCol(), posicion.getRow());
        this.ancho = ancho;
        this.alto = alto;
    }

    public PosicionGrafica(int col, int row, int ancho, int alto){
        super(col, row);
        this.ancho = ancho;
        this.alto = alto;
    }
}

