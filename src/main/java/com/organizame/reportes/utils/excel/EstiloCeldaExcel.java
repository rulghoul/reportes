package com.organizame.reportes.utils.excel;


import com.organizame.reportes.utils.SpringContext;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author raulperez
 */
@Slf4j
@Setter
@Getter
public class EstiloCeldaExcel {

    public enum TipoDato {
        TEXTO,
        ENTERO,
        PORCENTAJE,
        FECHA,
        BOOLEANO
    }


    private String fuenteNombre;
    private Integer fuenteSize;
    private String borderType;
    private String formatoDecimales;

    private final String nombre;

    private XSSFCellStyle normal;
    private XSSFCellStyle odd;

    private final XSSFCellStyle normalEntero;
    private final XSSFCellStyle oddEntero;

    private final XSSFCellStyle normalDate;
    private final XSSFCellStyle oddDate;

    private final XSSFCellStyle normalPorciento;
    private final XSSFCellStyle oddPorciento;


    public EstiloCeldaExcel(ColorExcel color, XSSFWorkbook libro) {
        Environment env = SpringContext.getContext().getEnvironment();
        this.fuenteNombre = env.getProperty("excel.font.name");
        this.fuenteSize = env.getProperty("excel.font.size", Integer.class);
        this.borderType = env.getProperty("excel.border.type");
        this.formatoDecimales = "0.00%";
        this.nombre = color.getNombre();
        this.normal = creaEstilo(libro, color, false, TipoDato.TEXTO);
        this.odd = creaEstilo(libro, color, true, TipoDato.TEXTO);
        this.normalDate = creaEstilo(libro, color, false, TipoDato.FECHA);
        this.oddDate = creaEstilo(libro, color, true, TipoDato.FECHA);
        this.normalPorciento = creaEstilo(libro, color, false, TipoDato.PORCENTAJE);
        this.oddPorciento = creaEstilo(libro, color, true, TipoDato.PORCENTAJE);
        this.normalEntero = creaEstilo(libro, color, false, TipoDato.ENTERO);
        this.oddEntero = creaEstilo(libro, color, true, TipoDato.ENTERO);
    }


    public EstiloCeldaExcel(ColorExcel color, XSSFWorkbook libro, Integer fontSize,
                            Optional<HorizontalAlignment> horizontal, Optional<VerticalAlignment> verticalAlignment,
                            Optional<Short> rotacion, String borderType,String formatoDecimal, boolean isBold) {
        Environment env = SpringContext.getContext().getEnvironment();
        this.fuenteNombre = env.getProperty("excel.font.name");
        this.fuenteSize = fontSize;
        this.borderType = borderType;
        this.formatoDecimales = formatoDecimal;
        this.nombre = color.getNombre();
        this.normal = this.creaEstilo(libro, color, false, TipoDato.TEXTO, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold);
        this.odd = this.creaEstilo(libro, color, true, TipoDato.TEXTO, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold);
        this.normalDate = this.creaEstilo(libro, color, false, TipoDato.FECHA, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold);
        this.oddDate = this.creaEstilo(libro, color, true, TipoDato.FECHA, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold);
        this.normalPorciento = this.creaEstilo(libro, color, false, TipoDato.PORCENTAJE, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold);
        this.oddPorciento = this.creaEstilo(libro, color, true, TipoDato.PORCENTAJE, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold);

        this.normalEntero = this.creaEstilo(libro, color, false, TipoDato.ENTERO, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold);
        this.oddEntero = this.creaEstilo(libro, color, true, TipoDato.ENTERO, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold);
    }

    private XSSFCellStyle creaEstilo(XSSFWorkbook libro, ColorExcel color, boolean odd, TipoDato tipo){
        return this.creaEstilo(libro, color,odd, tipo, Optional.empty(), Optional.empty(), Optional.empty(),  this.formatoDecimales, false);
    }

    private XSSFCellStyle creaEstilo(XSSFWorkbook libro, ColorExcel color, boolean odd, TipoDato tipo
    ,Optional<HorizontalAlignment> horizontal, Optional<VerticalAlignment> verticalAlignment, Optional<Short> rotacion
    ,String formatoDecimal, Boolean isBold){
        BorderStyle borde;
        try {
            borde = BorderStyle.valueOf(borderType.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Nombre de estilo de borde desconocido: '{}'. Usando valor por defecto: {}", borderType, BorderStyle.MEDIUM);
            borde = BorderStyle.MEDIUM;
        }

        Font fuente = libro.createFont();
        fuente.setFontName(fuenteNombre);
        fuente.setFontHeightInPoints(fuenteSize.shortValue());
        fuente.setBold(isBold);


        XSSFCellStyle temp = libro.createCellStyle();

        if(horizontal.isPresent()){
            temp.setAlignment(horizontal.get());
        }
        if(verticalAlignment.isPresent()){
            temp.setVerticalAlignment(verticalAlignment.get());
        }
        if(rotacion.isPresent()) {
            temp.setRotation(rotacion.get());
        }
        temp.setBorderTop(borde);
        temp.setBorderBottom(borde);
        temp.setBorderLeft(borde);
        temp.setBorderRight(borde);
        temp.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //Cambia el color
        if(odd){
            temp.setFillForegroundColor(color.getOdd());
        }else{
            temp.setFillForegroundColor(color.getNormal());
        }

        switch (tipo) {
            case FECHA -> {
                temp.setDataFormat(libro.getCreationHelper()
                    .createDataFormat().getFormat("dd/mm/yyyy"));
            }
            case ENTERO -> {
                temp.setDataFormat(libro.createDataFormat().getFormat("#,##0"));
            }
            case PORCENTAJE -> {
                temp.setDataFormat(libro.createDataFormat().getFormat(formatoDecimal));
            }
            case TEXTO, BOOLEANO -> {
            }
        }
        temp.setFont(fuente);
        return temp;
    }


}