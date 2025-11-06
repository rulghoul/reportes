package com.organizame.reportes.utils.excel;


import com.organizame.reportes.exceptions.ExcelException;
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
public class ColorExcel {

    private final String nombre;
    private final XSSFColor normal;
    private final XSSFColor odd;
    private final Pattern rgb = Pattern
            .compile("(?i)\\#*(?<r>[0-9|a-f]{2})(?<g>[0-9|a-f]{2})(?<b>[0-9|a-f]{2})");

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

    private XSSFColor ConvierteRGB(String valor) throws ExcelException {
        Matcher match = rgb.matcher(valor);
        while (match.find()) {
            int r = Integer.parseInt(match.group("r"), 16);
            int g = Integer.parseInt(match.group("g"), 16);
            int b = Integer.parseInt(match.group("b"), 16);
            return new XSSFColor(new Color(r, g, b),null);
        }
        throw new ExcelException("No se pudo cargar el color");
    }

    private XSSFColor ConvierteRGB(List<Integer> valor) throws ExcelException {
        try {
            return new XSSFColor(new Color(valor.get(0), valor.get(1), valor.get(2)), null);
        } catch (Exception e) {
            throw new ExcelException("El color no es valido");
        }
    }

    public String getNombre() {
        return nombre;
    }

    public XSSFColor getNormal() {
        return normal;
    }

    public XSSFColor getOdd() {
        return odd;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.nombre);
        hash = 17 * hash + Objects.hashCode(this.normal);
        hash = 17 * hash + Objects.hashCode(this.odd);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ColorExcel other = (ColorExcel) obj;
        if (!Objects.equals(this.nombre, other.nombre)) {
            return false;
        }
        if (!Objects.equals(this.normal, other.normal)) {
            return false;
        }
        if (!Objects.equals(this.odd, other.odd)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ColorExcel{" + "nombre=" + nombre + ", normal=" + normal + ", odd=" + odd + '}';
    }

}
