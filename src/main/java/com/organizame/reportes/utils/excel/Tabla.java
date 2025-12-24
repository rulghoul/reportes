package com.organizame.reportes.utils.excel;

import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.utils.SpringContext;
import com.organizame.reportes.utils.Utilidades;
import com.organizame.reportes.utils.excel.dto.Posicion;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Tabla {

    private final XSSFWorkbook wb;

    private final List<EstiloCeldaExcel> estilos;

    private final EstiloCeldaExcel rojo;

    private final XSSFCellStyle encabezado;

    private final XSSFSheet hoja;

    private  List<List<Object>> datos;

    private  List<FilaTabla>  tabla;

    private  Integer columna;

    private Integer columnaEnd;

    private  Integer rownum;


    public Tabla(XSSFWorkbook wb, List<EstiloCeldaExcel> estilos, XSSFCellStyle encabezado, XSSFSheet hoja){
        this.wb = wb;
        this.estilos = estilos;
        this.encabezado = encabezado;
        this.hoja = hoja;
        this.rojo = estilos.stream().filter(est -> est.getNombre().equalsIgnoreCase("Rojo"))

                .findFirst().orElse(estilos.get(0));
    }


    public void tablaFromList(List<List<Object>> datos, Integer columna, int fila) {
        this.datos = datos;
        Environment env = SpringContext.getContext().getEnvironment();
        Integer initCol = env.getProperty("excel.table.init.col", Integer.class);
        Integer initRow = env.getProperty("excel.table.init.row", Integer.class);

        this.columna =  columna < initCol ? initCol: columna;
        this.rownum = initRow < fila ? fila : initRow;
    }


    public void tablaFromFila(List<FilaTabla> tabla, Integer columna, int fila) {
        this.tabla = tabla;
        Environment env = SpringContext.getContext().getEnvironment();
        Integer initCol = env.getProperty("excel.table.init.col", Integer.class);
        Integer initRow = env.getProperty("excel.table.init.row", Integer.class);

        this.columna =  columna < initCol ? initCol: columna;
        this.rownum = initRow < fila ? fila : initRow;
    }


    public Posicion procesaTabla(){
        var primero = this.datos.getFirst();
        var ultimo = this.datos.getLast();
        AtomicInteger contador = new AtomicInteger(0);
        this.datos.forEach(dato -> {
            var isPrimero = primero.equals(dato);
            var isUltimo = ultimo.equals(dato);
            this.escribeFilaObject(hoja,dato, isPrimero, isUltimo,contador.getAndIncrement());
        });
        return new Posicion(columnaEnd,rownum);
    }

    public Posicion procesaTablaEstilo() {
        var primero = this.tabla.getFirst();
        var ultimo = this.tabla.getLast();
        AtomicInteger contador = new AtomicInteger(0);
        this.tabla.forEach(fila -> {
            boolean isPrimero = primero.equals(fila);
            boolean isUltimo = ultimo.equals(fila);
            this.escribeFilaObjectconEstilo(hoja,fila.getFila(), fila.getNombreEstilo(), isPrimero, isUltimo, contador.getAndIncrement());
        });

        return new Posicion(columnaEnd,rownum);
    }

    //Crear el celda unida con ancho y alto y luego solo invocar con alto 0;
    public Posicion dibujaCeldaUnida(String valor, String color, Posicion posicion, int ancho){
        CellRangeAddress region = new CellRangeAddress(posicion.getRow(), posicion.getRow(), posicion.getCol(), posicion.getCol() + ancho);
        Row row = hoja.createRow(posicion.getRow());
        Cell cell = row.createCell(posicion.getCol());
        cell.setCellValue(valor);
        Optional<EstiloCeldaExcel> temp = this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(color)).findFirst();
        EstiloCeldaExcel estilo = temp.orElseGet(() -> this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase("Estandar")).findFirst().get());
        var estiloCentrado = estilo.getNormal();
        estiloCentrado.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(estiloCentrado);
        this.hoja.addMergedRegion(region);
        posicion.setRow(posicion.getRow() + 1);
        return posicion;
    }

    public Posicion dibujaFila(FilaTabla fila, Posicion posicion){
        this.escribeFilaObject(this.hoja, fila.getFila(), fila.getNombreEstilo(), false, false, 1);
        posicion.setRow(posicion.getRow()+ 1);
        return posicion;
    }

    public Posicion dibujaLista(List<Object> fila, Posicion posicion, String estilo){
        this.escribeFilaObject(this.hoja, fila, estilo, false, false, 1);
        posicion.setRow(posicion.getRow()+ 1);
        return posicion;
    }


    //Este metodo obtiene una lista de Objetos de un dbf
    public void escribeFilaObject(XSSFSheet sh, List<Object> fila, boolean primero, boolean ultimo, int elemento) {
        escribeFilaObject(sh, fila, "Estandar", primero, ultimo, elemento);
    }

    public void escribeFilaObjectconEstilo(XSSFSheet sh, List<Object> fila, String estilo, boolean primero, boolean ultimo, int elemento) {
            escribeFilaObject(sh, fila, estilo, primero, ultimo, elemento);
    }

    //Este metodo obtiene una lista de Objetos de un dbf
    public void escribeFilaObject(XSSFSheet sh, List<Object> fila, String color, boolean primero, boolean ultimo, int elemento) {
        log.debug("Se procesa fila {} con el estilo {}", fila.getFirst(), color);
        log.debug("Los estilos disponibles son {}", this.estilos.stream().map(EstiloCeldaExcel::getNombre).toList());
        Row row = sh.createRow(rownum);
        int cellnum = columna;
        Optional<EstiloCeldaExcel> temp = this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(color)).findFirst();
        EstiloCeldaExcel estilo = temp.orElseGet(() -> this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase("Estandar")).findFirst().get());
        log.debug("Se recupero el estilo **{}** para la peticion de estilo {}", estilo.getNombre(), color);
        for (Object celda : fila) {
                Cell cell = row.createCell(cellnum);
                if (celda != null) {
                    if (primero) {
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

        //aplicar el autosize
        if(ultimo) {
            for (int i = 0; row.getLastCellNum() + 2 > i; i++) {
                sh.autoSizeColumn(i);
            }
        }

        columnaEnd = cellnum;
        rownum++;
    }

    private void trasnforma(Cell cell, Object celda, boolean ispar, EstiloCeldaExcel estilo) {
        Utilidades.trasnforma(this.wb, cell, celda, ispar, estilo);
    }

}
