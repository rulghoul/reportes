package com.organizame.reportes.utils.excel;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author raulperez
 */
@Slf4j
@Getter
@Setter
public class EstiloCeldaExcel {

    @Value("${excel.font.name}")
    private String fuenteNombre;
    @Value("${excel.font.size}")
    private String fuenteSize;
    @Value("${excel.border.type}")
    private String borderType;

    private final String nombre;
    private final XSSFCellStyle normal;
    private final XSSFCellStyle odd;
    private final XSSFCellStyle normalDate;
    private final XSSFCellStyle oddDate;

    public EstiloCeldaExcel(ColorExcel color, SXSSFWorkbook libro) {
        this.nombre = color.getNombre();
        this.normal = CreaEstilo(libro, color, false, false);
        this.odd = CreaEstilo(libro, color, true, false);
        this.normalDate = CreaEstilo(libro, color, false, true);
        this.oddDate = CreaEstilo(libro, color, true, true);
    }

    private XSSFCellStyle CreaEstilo(SXSSFWorkbook libro, ColorExcel color, boolean odd, boolean fecha){
        BorderStyle borde;
        try {
            borde = BorderStyle.valueOf(borderType.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Nombre de estilo de borde desconocido: '{}'. Usando valor por defecto: {}", borderType, BorderStyle.MEDIUM);
            borde = BorderStyle.MEDIUM;
        }
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

        return temp;
    }

}