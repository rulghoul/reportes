package com.organizame.reportes.utils.excel;


import com.organizame.reportes.utils.SpringContext;
import com.organizame.reportes.utils.Utilidades;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
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
    private BorderStyle borderType;
    private final Optional<String> borderPosition;
    private String formatoDecimales;

    private final String nombre;

    private XSSFCellStyle normal;
    private XSSFCellStyle odd;


    private final XSSFCellStyle normalDate;
    private final XSSFCellStyle oddDate;

    private final XSSFCellStyle normalEntero;
    private final XSSFCellStyle oddEntero;
    private final XSSFCellStyle normalEnteroRojo;
    private final XSSFCellStyle oddEnteroRojo;


    private final XSSFCellStyle normalPorciento;
    private final XSSFCellStyle oddPorciento;
    private final XSSFCellStyle normalPorcientoRojo;
    private final XSSFCellStyle oddPorcientoRojo;


    public EstiloCeldaExcel(ColorExcel color, XSSFWorkbook libro) {
        Environment env = SpringContext.getContext().getEnvironment();
        this.fuenteNombre = env.getProperty("excel.font.name");
        this.fuenteSize = env.getProperty("excel.font.size", Integer.class);
        try {
            this.borderType = BorderStyle.valueOf(env.getProperty("excel.border.type").toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Nombre de estilo de borde desconocido: '{}'. Usando valor por defecto: {}", borderType, BorderStyle.MEDIUM);
            this.borderType = BorderStyle.MEDIUM;
        }
        this.borderPosition = Optional.empty();
        this.formatoDecimales = "0.00%";
        this.nombre = color.getNombre();
        this.normal = creaEstilo(libro, color,false, TipoDato.TEXTO, false);
        this.odd = creaEstilo(libro, color, true, TipoDato.TEXTO, false);
        this.normalDate = creaEstilo(libro, color, false, TipoDato.FECHA, false);
        this.oddDate = creaEstilo(libro, color, true, TipoDato.FECHA, false);

        this.normalEntero = creaEstilo(libro, color, false, TipoDato.ENTERO, false);
        this.oddEntero = creaEstilo(libro, color, true, TipoDato.ENTERO, false);
        this.normalEnteroRojo = creaEstilo(libro, color, false, TipoDato.ENTERO, true);
        this.oddEnteroRojo = creaEstilo(libro, color, true, TipoDato.ENTERO, true);

        this.normalPorciento = creaEstilo(libro, color, false, TipoDato.PORCENTAJE, false);
        this.oddPorciento = creaEstilo(libro, color, true, TipoDato.PORCENTAJE,false);
        this.normalPorcientoRojo = creaEstilo(libro, color, false, TipoDato.PORCENTAJE, true);
        this.oddPorcientoRojo = creaEstilo(libro, color, true, TipoDato.PORCENTAJE, true);
    }


    public EstiloCeldaExcel(ColorExcel color, XSSFWorkbook libro, Integer fontSize,
                            Optional<HorizontalAlignment> horizontal, Optional<VerticalAlignment> verticalAlignment,
                            Optional<Short> rotacion, BorderStyle borderType, Optional<String> borderPosition,
                            String formatoDecimal, boolean isBold,
                            Optional<String> colorFuente, Optional<String> colorNegativos, boolean isWarpText) {
        Environment env = SpringContext.getContext().getEnvironment();
        this.fuenteNombre = env.getProperty("excel.font.name");
        this.fuenteSize = fontSize;
        this.borderType = borderType;
        this.borderPosition = borderPosition;
        this.formatoDecimales = formatoDecimal;
        this.nombre = color.getNombre();
        this.normal = this.creaEstilo(libro, color, false, TipoDato.TEXTO, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold, colorFuente, isWarpText);
        this.normalDate = this.creaEstilo(libro, color, false, TipoDato.FECHA, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold, colorFuente, isWarpText);
        this.oddDate = this.creaEstilo(libro, color, true, TipoDato.FECHA, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold, colorFuente, isWarpText);

        this.normalEntero = this.creaEstilo(libro, color, false, TipoDato.ENTERO, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold, colorFuente, isWarpText);
        this.oddEntero = this.creaEstilo(libro, color, true, TipoDato.ENTERO, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold, colorFuente, isWarpText);
        this.normalEnteroRojo = this.creaEstilo(libro, color, false, TipoDato.ENTERO, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold,  colorNegativos, isWarpText);
        this.oddEnteroRojo = this.creaEstilo(libro, color, true, TipoDato.ENTERO, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold,  colorNegativos, isWarpText);

        this.normalPorciento = this.creaEstilo(libro, color, false, TipoDato.PORCENTAJE, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold, colorFuente, isWarpText);
        this.oddPorciento = this.creaEstilo(libro, color, true, TipoDato.PORCENTAJE, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold, colorFuente, isWarpText);
        this.normalPorcientoRojo = this.creaEstilo(libro, color, false, TipoDato.PORCENTAJE, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold,  colorNegativos, isWarpText);
        this.oddPorcientoRojo = this.creaEstilo(libro, color, true, TipoDato.PORCENTAJE, horizontal, verticalAlignment, rotacion, formatoDecimal, isBold,  colorNegativos, isWarpText);


    }

    private XSSFCellStyle creaEstilo(XSSFWorkbook libro, ColorExcel color,  boolean odd, TipoDato tipo, boolean resaltado){
        Optional<String> fuente = resaltado ?  Optional.of("#FF0000") : Optional.empty();
        return this.creaEstilo(libro, color, odd, tipo, Optional.empty(), Optional.empty(), Optional.empty(),
                this.formatoDecimales, false,  fuente, false);
    }

    private XSSFCellStyle creaEstilo(XSSFWorkbook libro, ColorExcel color, boolean odd, TipoDato tipo
    ,Optional<HorizontalAlignment> horizontal, Optional<VerticalAlignment> verticalAlignment
            , Optional<Short> rotacion
    ,String formatoDecimal, Boolean isBold
            , Optional<String> colorFuente, boolean isWarpText){

        var fuente = libro.createFont();
        fuente.setFontName(fuenteNombre);
        fuente.setFontHeightInPoints(fuenteSize.shortValue());
        fuente.setBold(isBold);

        if(colorFuente.isPresent()){
            fuente.setColor(new XSSFColor(Utilidades.convierteComponentesRGB(colorFuente.get()), null));
        }


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
        if(this.borderPosition.isEmpty()) {
            temp.setBorderTop(this.borderType);
            temp.setBorderBottom(this.borderType);
            temp.setBorderLeft(this.borderType);
            temp.setBorderRight(this.borderType);
        }else{
            this.ajustaBordes(temp);
        }
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
        if(isWarpText){
            temp.setWrapText(isWarpText);
        }
        return temp;
    }

    private void ajustaBordes(XSSFCellStyle temp) {
        if(this.borderPosition.get().isBlank()){
            return;
        }
        for (char c : this.borderPosition.get().toLowerCase().toCharArray()) {
            switch (c) {
                case 't' -> temp.setBorderTop(this.borderType);
                case 'b' -> temp.setBorderBottom(this.borderType);
                case 'l' -> temp.setBorderLeft(this.borderType);
                case 'r' -> temp.setBorderRight(this.borderType);
            }
        }
    }


}