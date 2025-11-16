package com.organizame.reportes.utils.excel;

import com.organizame.reportes.exceptions.ColorExcepcion;
import com.organizame.reportes.exceptions.ExcelException;
import com.organizame.reportes.utils.Utilidades;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.*;

/**
 *
 * @author raulperez
 */
@Slf4j
@Getter
@EqualsAndHashCode
public class ColorExcel {

    private final String nombre;
    private final XSSFColor normal;
    private final XSSFColor odd;

    public ColorExcel(String nombre, String normal, String odd) throws ExcelException {
        this.nombre = nombre;
        this.normal = ConvierteRGB(normal);
        this.odd = ConvierteRGB(odd);
    }

    public XSSFColor ConvierteRGB(String valor) throws ExcelException {
        try {
            return new XSSFColor(Utilidades.convierteRGB(valor), null);
        }catch (ColorExcepcion e){
            throw new ExcelException(e.getMessage());
        }
    }


    @Override
    public String toString() {
        return "ColorExcel{" + "nombre=" + nombre + ", normal=" + normal + ", odd=" + odd + '}';
    }

}
