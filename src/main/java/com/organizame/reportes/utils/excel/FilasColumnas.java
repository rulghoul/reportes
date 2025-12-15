package com.organizame.reportes.utils.excel;

import com.organizame.reportes.utils.Utilidades;
import com.organizame.reportes.utils.excel.dto.ColumnaFila;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.Objects;

//Dibujar filas y columnas
@Slf4j
public class FilasColumnas {

    private final XSSFWorkbook wb;

    private final XSSFSheet hoja;

    private final List<EstiloCeldaExcel> estilos;

    private final EstiloCeldaExcel normal;

    private final EstiloCeldaExcel rojo;

    public FilasColumnas(XSSFWorkbook wb, XSSFSheet hoja
            , List<EstiloCeldaExcel> estilos
            ,EstiloCeldaExcel normal, EstiloCeldaExcel rojo){
        this.wb = wb;
        this.hoja = hoja;
        this.estilos = estilos;
        this.normal = normal;
        this.rojo = rojo;
    }

    public void DibujaFila(ColumnaFila fila){
        if(!Objects.isNull(fila)){
            var posicion = fila.getPosicion();
            var filaGet = hoja.getRow(posicion.getRow());
            var row = Objects.isNull(filaGet)
                    ? hoja.createRow(posicion.getRow())
                    : filaGet;
            fila.getValores().forEach(celda -> {
                var cell = row.createCell(posicion.getCol());
                var estilo = getEstilo(celda.getEstilo());

                cell.setCellStyle(estilo.getNormal());

                this.trasnforma(cell, celda.getValor(), true, estilo);

                if(celda.getTamanio() > 1){
                    var rango = new CellRangeAddress(posicion.getRow(),posicion.getRow(),
                            posicion.getCol()
                            , (posicion.getCol() + celda.getTamanio() -1));
                    hoja.addMergedRegion(rango);
                }
                posicion.addCols(celda.getTamanio());
            });
        }

    }



    public void DibujaColumna(ColumnaFila fila){
        if(!Objects.isNull(fila)){
            var posicion = fila.getPosicion();
            fila.getValores().forEach(celda -> {
                var filaGet = hoja.getRow(posicion.getRow());
                var row = Objects.isNull(filaGet)
                        ? hoja.createRow(posicion.getRow())
                        : filaGet;
                var cell = row.createCell(posicion.getCol());
                var estilo = getEstilo(celda.getEstilo());

                cell.setCellStyle(estilo.getNormal());

                this.trasnforma(cell, celda.getValor(), true, estilo);

                if(celda.getTamanio() > 1){
                    var rango = new CellRangeAddress(posicion.getRow(),
                            (posicion.getRow() + celda.getTamanio() -1),
                            posicion.getCol() , posicion.getCol());
                    hoja.addMergedRegion(rango);
                }
                posicion.addRows(celda.getTamanio());
            });
        }

    }

    private EstiloCeldaExcel getEstilo(String color){
        return this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(color))
                .findFirst().orElse(this.normal);
    }

    private void trasnforma(Cell cell, Object celda, boolean ispar, EstiloCeldaExcel estilo) {
        Utilidades.trasnforma(this.wb, cell, celda, ispar, estilo, this.rojo);
    }
}
