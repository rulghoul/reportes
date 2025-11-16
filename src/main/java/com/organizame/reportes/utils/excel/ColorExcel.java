package com.organizame.reportes.utils.excel;


import com.organizame.reportes.exceptions.ColorExcepcion;
import com.organizame.reportes.exceptions.ExcelException;
import com.organizame.reportes.utils.Utilidades;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public ColorExcel(String nombre, List<Integer> normal, List<Integer> odd) throws ExcelException {
        this.nombre = nombre;
        this.normal = ConvierteRGB(normal);
        this.odd = ConvierteRGB(odd);
    }

    public ColorExcel(String nombre, XSSFColor normal, XSSFColor odd) {
        this.nombre = nombre;
        this.normal = normal;
        this.odd = odd;
    }

    public XSSFColor ConvierteRGB(String valor) throws ExcelException {
        try {
            return new XSSFColor(Utilidades.convierteRGB(valor), null);
        }catch (ColorExcepcion e){
            throw new ExcelException(e.getMessage());
        }
    }

    private XSSFColor ConvierteRGB(List<Integer> valor) throws ExcelException {
        try {
            return new XSSFColor(new Color(valor.get(0), valor.get(1), valor.get(2)), null);
        } catch (Exception e) {
            throw new ExcelException("El color no es valido");
        }
    }


    @Override
    public String toString() {
        return "ColorExcel{" + "nombre=" + nombre + ", normal=" + normal + ", odd=" + odd + '}';
    }

}
