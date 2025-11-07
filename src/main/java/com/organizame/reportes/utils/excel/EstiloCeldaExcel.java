package com.organizame.reportes.utils.excel;


import com.organizame.reportes.utils.SpringContext;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 *
 * @author raulperez
 */
@Slf4j
@Getter
@Setter
public class EstiloCeldaExcel {

    private String fuenteNombre;
    private Integer fuenteSize;
    private String borderType;

    private final String nombre;
    private final XSSFCellStyle normal;
    private final XSSFCellStyle odd;
    private final XSSFCellStyle normalDate;
    private final XSSFCellStyle oddDate;


    public EstiloCeldaExcel(ColorExcel color, XSSFWorkbook libro) {
        Environment env = SpringContext.getContext().getEnvironment();
        this.fuenteNombre = env.getProperty("excel.font.name");
        this.fuenteSize = env.getProperty("excel.font.size", Integer.class);
        this.borderType = env.getProperty("excel.border.type");
        this.nombre = color.getNombre();
        this.normal = CreaEstilo(libro, color, false, false);
        this.odd = CreaEstilo(libro, color, true, false);
        this.normalDate = CreaEstilo(libro, color, false, true);
        this.oddDate = CreaEstilo(libro, color, true, true);
    }


    private XSSFCellStyle CreaEstilo(XSSFWorkbook libro, ColorExcel color, boolean odd, boolean fecha){
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

        XSSFCellStyle temp = (XSSFCellStyle) libro.createCellStyle();
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
        if(fecha){
            temp.setDataFormat(libro.getCreationHelper()
                    .createDataFormat().getFormat("dd/mm/yyyy"));
        }
        temp.setFont(fuente);
        return temp;
    }

}