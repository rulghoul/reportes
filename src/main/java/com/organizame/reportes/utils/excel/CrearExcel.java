package com.organizame.reportes.utils.excel;


import com.organizame.reportes.exceptions.ExcelException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author raul.perez
 */
@Slf4j
@Getter
@Setter
public class CrearExcel {


    @Value("${excel.table.init.col}")
    private String initCol;

    @Value("${excel.table.init.row}")
    private String initRow;

    private final Path salida;
    private final SXSSFWorkbook wb;
    private final boolean cerrado;
    private Integer rownum = 0;
    private final List<EstiloCeldaExcel> estilos;
    private XSSFCellStyle encabezado;

    public CrearExcel(Path archivo) {
        wb = new SXSSFWorkbook(100);
        cerrado = false;
        salida = archivo;
        estilos = new ArrayList<>();
    }

    public SXSSFSheet CrearHoja(String hoja){
        if(cerrado){
            throw new ExcelException("El archivo ya se guardo, no se pueden agregar mas Hojas");
        }else{
            return wb.createSheet(hoja);
        }
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
        Optional<EstiloCeldaExcel> temp = this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(color.getNombre()))
                .findFirst();
        if(temp.isPresent()){
            throw new ExcelException("El color ya existe");
        }else{
            this.estilos.add(new EstiloCeldaExcel(color, wb));
        }
    }

    public void escribeFila(SXSSFSheet sh, List<String> fila) {
        escribeFila(sh, fila, "Estandar");
    }

    public void escribeFila(SXSSFSheet sh, List<String> fila, String color) {
        List<Object> newFila = fila.stream().map(cel -> (Object) cel).collect(Collectors.toList());
        escribeFilaObject(sh, newFila, color);
    }

    //Este metodo obtiene una lista de Objetos de un dbf
    public void escribeFilaObject(SXSSFSheet sh, List<Object> fila) {
        escribeFilaObject(sh, fila, "Estandar");
    }

    //Este metodo obtiene una lista de Objetos de un dbf
    public void escribeFilaObject(SXSSFSheet sh, List<Object> fila, String color) {
        Row row = sh.createRow(rownum);
        int cellnum = 0;
        Optional<EstiloCeldaExcel> temp = this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(color)).findFirst();
        EstiloCeldaExcel estilo = temp.isPresent() ? temp.get() : this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase("Estandar")).findFirst().get();
        for (Object celda : fila) {
            Cell cell = row.createCell(cellnum);
            if (celda != null) {
                if (rownum == 0) {
                    cell.setCellStyle(encabezado);
                } else {
                    if ((rownum % 3) != 0) {
                        cell.setCellStyle(estilo.getOdd());
                    } else {
                        cell.setCellStyle(estilo.getNormal());
                    }
                }
            }
            this.trasnforma(cell, celda, ((rownum % 3) != 0), estilo);
            cellnum++;
        }
        rownum++;
    }

    private void trasnforma(Cell cell, Object valor, boolean par, EstiloCeldaExcel estilo) {

        try {
            String clase = valor.getClass().getTypeName();
            switch (clase) {
                case "java.lang.String":
                    cell.setCellValue((String) valor);
                    break;
                case "java.lang.Double":
                    cell.setCellValue((Double) valor);
                    break;
                case "java.util.Date":
                    cell.setCellValue((Date) valor);
                    if (par) {
                        cell.setCellStyle(estilo.getOddDate());
                    } else {
                        cell.setCellStyle(estilo.getNormalDate());
                    }
                    break;
                case "java.math.BigDecimal":
                    cell.setCellValue(Double.parseDouble(valor.toString()));
                    break;
                case "java.lang.Integer":
                    cell.setCellValue(Double.parseDouble(valor.toString()));
                    break;
                case "java.lang.Boolean":
                    if (valor.equals(false)) {
                        cell.setCellValue("FALSO");
                    } else {
                        cell.setCellValue("VERDADERO");
                    }
                    break;
                //Para escribir links
                case "java.util.Arrays$ArrayList":
                    List<String> temp = (List<String>) valor;
                    cell.setCellValue(temp.get(0));
                    Hyperlink href = this.wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
                    href.setAddress(temp.get(1));
                    cell.setHyperlink(href);
                    break;
                default:
                    System.out.println("No ESTIPULADO");
                    break;
            }
        } catch (Exception e) {
            //si falla simplemente agrega una celda vacia
            cell.setCellValue("");
        }

    }

    public void guardaExcel() throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream(salida.toFile());
        wb.write(out);
        wb.dispose();
    }

}
