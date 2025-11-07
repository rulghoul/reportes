package com.organizame.reportes.utils.excel;


import com.organizame.reportes.exceptions.ExcelException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.io.*;
import java.util.*;

/**
 *
 * @author raul.perez
 */
@Slf4j
@Getter
@Setter
public class CrearExcel {

    private final SXSSFWorkbook wb;
    private final boolean cerrado;


    private final List<EstiloCeldaExcel> estilos;

    private XSSFCellStyle encabezado;


    public CrearExcel() {
        wb = new SXSSFWorkbook(100);
        cerrado = false;
        estilos = new ArrayList<>();
        this.estiloEncabezado();
    }

    public SXSSFSheet CrearHoja(String hoja){
        if(cerrado){
            throw new ExcelException("El archivo ya se guardo, no se pueden agregar mas Hojas");
        }else{
            return wb.createSheet(hoja);
        }
    }


    public Map<String, Integer> creaTabla(SXSSFSheet hoja, List<List<Object>> datos, Integer columna, Integer  fila){
        Tabla tabla = new Tabla(wb, estilos, encabezado, hoja, datos, columna, fila);
        var resultado = tabla.procesaTabla();
        return resultado;
    }


    public void estiloEncabezado(){
        //colores
        XSSFColor azulObscuro = new XSSFColor(new java.awt.Color(3, 33, 81), null);
        XSSFColor grisclaro = new XSSFColor(new java.awt.Color(247, 246, 239), null);
        XSSFColor blanco = new XSSFColor(new java.awt.Color(255, 255, 255), null);

        CreationHelper createHelper = wb.getCreationHelper();

        encabezado = (XSSFCellStyle) wb.createCellStyle();
        Font resaltar = wb.createFont();
        resaltar.setBold(true);
        resaltar.setColor(IndexedColors.WHITE.getIndex());
        encabezado.setFont(resaltar);
        encabezado.setBorderTop(BorderStyle.MEDIUM);
        encabezado.setBorderBottom(BorderStyle.MEDIUM);
        encabezado.setBorderLeft(BorderStyle.MEDIUM);
        encabezado.setBorderRight(BorderStyle.MEDIUM);
        encabezado.setFillForegroundColor(azulObscuro);
        encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        ColorExcel estandar = new ColorExcel("Estandar", blanco, grisclaro);

        estilos.add(new EstiloCeldaExcel(estandar, wb));
    }

    public void agregaColor(ColorExcel color) throws ExcelException {
        var temp = this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(color.getNombre()))
                .findFirst();
        if(temp.isPresent()){
            throw new ExcelException("El color ya existe");
        }else{
            this.estilos.add(new EstiloCeldaExcel(color, wb));
        }
    }

    public ByteArrayInputStream guardaExcel() throws IOException {
        var out = new ByteArrayOutputStream();
        wb.write(out);
        wb.dispose();
        return new ByteArrayInputStream(out.toByteArray());
    }

}
