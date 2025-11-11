package com.organizame.reportes.utils.excel;

import com.organizame.reportes.dto.TablaContenido;
import com.organizame.reportes.utils.SpringContext;
import com.organizame.reportes.utils.excel.dto.Posicion;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


public class Tabla {

    private final XSSFWorkbook wb;

    private final List<EstiloCeldaExcel> estilos;

    private final XSSFCellStyle encabezado;

    private final XSSFSheet hoja;

    private  List<List<Object>> datos;

    private  TablaContenido tabla;

    private  Integer columna;

    private Integer columnaEnd;

    private  Integer rownum;


    public Tabla(XSSFWorkbook wb, List<EstiloCeldaExcel> estilos, XSSFCellStyle encabezado, XSSFSheet hoja, List<List<Object>> datos, Integer columna, Integer fila){
        this.wb = wb;
        this.estilos = estilos;
        this.encabezado = encabezado;
        this.hoja = hoja;
        this.datos = datos;
        this.columna = columna;
        this.rownum = fila;

        Environment env = SpringContext.getContext().getEnvironment();
        Integer initCol = env.getProperty("excel.table.init.col", Integer.class);
        Integer initRow = env.getProperty("excel.table.init.row", Integer.class);

        this.columna =  columna < initCol ? initCol: columna;
        this.rownum = rownum < initRow ? initRow : rownum;
    }

    public Tabla(XSSFWorkbook wb, List<EstiloCeldaExcel> estilos, XSSFCellStyle encabezado, XSSFSheet hoja, TablaContenido tabla, Integer columna, Integer fila){
        this.wb = wb;
        this.estilos = estilos;
        this.encabezado = encabezado;
        this.hoja = hoja;
        this.tabla = tabla;
        this.columna = columna;
        this.rownum = fila;

        Environment env = SpringContext.getContext().getEnvironment();
        Integer initCol = env.getProperty("excel.table.init.col", Integer.class);
        Integer initRow = env.getProperty("excel.table.init.row", Integer.class);

        this.columna =  columna < initCol ? initCol: columna;
        this.rownum = rownum < initRow ? initRow : rownum;
    }



    public Posicion procesaTabla(){
        for(int i = 0; this.datos.size() > i ; i++){
            this.escribeFilaObject(hoja,this.datos.get(i), i);
        }
        return new Posicion(columnaEnd,rownum);
    }

    public Posicion procesaTablaEstilo() {

        for(int i = 0; this.tabla.getDatos().size() > i ; i++){
            this.escribeFilaObjectconEstilo(hoja,this.tabla.getDatos().get(i).getFila(), i, this.tabla.getDatos().get(i).getNombreEstilo());
        }
        return new Posicion(columnaEnd,rownum);
    }



    public void escribeFila(XSSFSheet sh, List<String> fila, int elemento) {
        escribeFila(sh, fila, "Estandar", elemento);
    }

    public void escribeFila(XSSFSheet sh, List<String> fila, String color, int elemento ) {
        List<Object> newFila = fila.stream().map(cel -> (Object) cel).collect(Collectors.toList());
        escribeFilaObject(sh, newFila, color, elemento);
    }

    //Este metodo obtiene una lista de Objetos de un dbf
    public void escribeFilaObject(XSSFSheet sh, List<Object> fila, int elemento) {
        escribeFilaObject(sh, fila, "Estandar", elemento);
    }

    public void escribeFilaObjectconEstilo(XSSFSheet sh, List<Object> fila, int elemento, String estilo) {
        if (
                this.estilos.stream().filter(e -> e.getNombre()
                        .equalsIgnoreCase(estilo)).findFirst().isPresent()){
            escribeFilaObject(sh, fila, estilo, elemento);
        }else{
            escribeFilaObject(sh, fila, "Estandar", elemento);
        }
    }

    //Este metodo obtiene una lista de Objetos de un dbf
    public void escribeFilaObject(XSSFSheet sh, List<Object> fila, String color, int elemento) {
        Row row = sh.createRow(rownum);
        int cellnum = 0 + columna;
        Optional<EstiloCeldaExcel> temp = this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(color)).findFirst();
        EstiloCeldaExcel estilo = temp.isPresent() ? temp.get() : this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase("Estandar")).findFirst().get();
        for (Object celda : fila) {
            Cell cell = row.createCell(cellnum);
            if (celda != null) {
                if (elemento == 0) {
                    cell.setCellStyle(encabezado);
                } else {
                    if ((elemento % 3) != 0) {
                        cell.setCellStyle(estilo.getOdd());
                    } else {
                        cell.setCellStyle(estilo.getNormal());
                    }
                }
            }
            this.trasnforma(cell, celda, ((elemento % 3) != 0), estilo);
            cellnum++;
        }
        columnaEnd = cellnum;
        rownum++;
    }

    private void trasnforma(Cell cell, Object valor, boolean par, EstiloCeldaExcel estilo) {
        try {
            switch (valor) {
                case String s -> cell.setCellValue(s);
                case Double d -> cell.setCellValue(d);
                case Date d -> {
                    cell.setCellValue(d);
                    cell.setCellStyle(par ? estilo.getOddDate() : estilo.getNormalDate());
                }
                case BigDecimal bd -> cell.setCellValue(bd.doubleValue());
                case Integer i -> cell.setCellValue(i.doubleValue());
                case Boolean b -> cell.setCellValue(b ? "VERDADERO" : "FALSO");
                case List<?> temp -> manejarArrayList(cell, temp);
                case null, default -> {
                    System.out.println("No ESTIPULADO: " +
                            (valor != null ? valor.getClass().getTypeName() : "null"));
                }
            }
        } catch (Exception e) {
            cell.setCellValue("");
        }
    }

    private void manejarArrayList(Cell cell, List<?> temp) {
        if (temp.size() >= 2 && temp.get(0) instanceof String && temp.get(1) instanceof String) {
            cell.setCellValue((String) temp.get(0));
            Hyperlink href = this.wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
            href.setAddress((String) temp.get(1));
            cell.setHyperlink(href);
        }
    }


}
