package com.organizame.reportes.utils.presentacion.dto;

import com.organizame.reportes.utils.Utilidades;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

@Data
@AllArgsConstructor
public class ColorPresentacion {
    private String nombre;
    private Color fillColor;
    private Color fontColor;
    private Double fontSize;
    private boolean isBold;

    public ColorPresentacion(String nombre, String fillColor, String fontColor, Number fontsize, boolean isBold){
        this.nombre = nombre;
        this.fillColor = Utilidades.convierteRGB(fillColor);
        this.fontColor = Utilidades.convierteRGB(fontColor);
        this.fontSize = fontsize.doubleValue();
        this.isBold = isBold;
    }
}
