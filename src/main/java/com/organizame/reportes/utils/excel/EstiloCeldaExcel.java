package com.organizame.reportes.utils.excel;


import com.organizame.reportes.utils.SpringContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;

/**
 *
 * @author raulperez
 */
@Slf4j
@Data
public class EstiloCeldaExcel {

    public enum TipoDato {
        TEXTO,
        PORCENTAJE,
        FECHA,
        BOOLEANO
    }


    private String fuenteNombre;
    private Integer fuenteSize;
    private String borderType;

    private final String nombre;
    private XSSFCellStyle normal;
    private final XSSFCellStyle odd;
    private final XSSFCellStyle normalDate;
    private final XSSFCellStyle oddDate;

    private final XSSFCellStyle normalPorciento;
    private final XSSFCellStyle oddPorciento;


    public EstiloCeldaExcel(ColorExcel color, XSSFWorkbook libro) {
        Environment env = SpringContext.getContext().getEnvironment();
        this.fuenteNombre = env.getProperty("excel.font.name");
        this.fuenteSize = env.getProperty("excel.font.size", Integer.class);
        this.borderType = env.getProperty("excel.border.type");
        this.nombre = color.getNombre();
        this.normal = creaEstilo(libro, color, false, TipoDato.TEXTO);
        this.odd = creaEstilo(libro, color, true, TipoDato.TEXTO);
        this.normalDate = creaEstilo(libro, color, false, TipoDato.FECHA);
        this.oddDate = creaEstilo(libro, color, true, TipoDato.FECHA);
        this.normalPorciento = creaEstilo(libro, color, false, TipoDato.PORCENTAJE);
        this.oddPorciento = creaEstilo(libro, color, true, TipoDato.PORCENTAJE);
    }


    private XSSFCellStyle creaEstilo(XSSFWorkbook libro, ColorExcel color, boolean odd, TipoDato tipo){
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

        XSSFCellStyle temp = libro.createCellStyle();
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
            case FECHA -> temp.setDataFormat(libro.getCreationHelper()
                    .createDataFormat().getFormat("dd/mm/yyyy"));
            case PORCENTAJE -> temp.setDataFormat(libro.createDataFormat().getFormat("0.00%"));
            case TEXTO, BOOLEANO -> {
                // No se aplica formato especial
            }
        }
        temp.setFont(fuente);
        return temp;
    }

}