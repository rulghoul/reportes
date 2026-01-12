package com.organizame.reportes.service;

import com.organizame.reportes.dto.Margen;
import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.repository.service.PeriodoService;
import com.organizame.reportes.utils.excel.ColorExcel;
import com.organizame.reportes.utils.excel.CrearExcel;
import com.organizame.reportes.utils.excel.EstiloCeldaExcel;
import com.organizame.reportes.utils.excel.dto.Celda;
import com.organizame.reportes.utils.excel.dto.ColumnaFila;
import com.organizame.reportes.utils.excel.dto.Posicion;
import jakarta.validation.OverridesAttribute;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class ReporteInventarioService {

    private PeriodoService service;

    private String nombreArchivo;

    @Autowired
    public ReporteInventarioService(PeriodoService service){
        this.service = service;
    }

    public ByteArrayInputStream getReporte(RequestRanking peticion) throws IOException {

        var fechAnt = LocalDate.of(peticion.getAnio(), peticion.getMes(), 1).minusMonths(1);

        var actual = service.getRegistrosMes(peticion.getAnio(), peticion.getMes());
        var anterior = service.getRegistrosMes(fechAnt.getYear(), fechAnt.getMonth().getValue());

        var consolidado = service.getMargenes(anterior, actual);

        CrearExcel excel = new CrearExcel();

        var hoja = excel.CrearHoja("margen");

        this.creaEstilos(excel);

        this.encabezados(excel, hoja, peticion, fechAnt);

        this.cuerpo(excel, hoja, consolidado);

        //Se ajustan los ancos de columnas
        var anchos = List.of(60,1,35,35,1,35);
        for(var i = 0; anchos.size() > i; i++ ){
            hoja.setColumnWidth(i, anchos.get(i)*150);
        }

        //Se congelan secciones

        hoja.createFreezePane(1, 5);

        String mesCadena = Month.of(peticion.getMes()).getDisplayName(TextStyle.FULL, Locale.of("es", "MX"));


        this.nombreArchivo = "Estado Inventario " + mesCadena + " " + peticion.getAnio();
        return  excel.guardaExcel();
    }


    private void encabezados(CrearExcel excel, XSSFSheet hoja, RequestRanking peticion, LocalDate fechaAnterior) {
        String mesCadena = Month.of(peticion.getMes()).getDisplayName(TextStyle.FULL, Locale.of("es", "MX")).toUpperCase();
        String mesAnterior = Month.of(fechaAnterior.getMonthValue()).getDisplayName(TextStyle.FULL, Locale.of("es", "MX")).toUpperCase();

        var posicion = new Posicion(0,3);
        var columnaA = new ColumnaFila(posicion, List.of(new Celda("Modelos", "GrisEncabezado", 2)));
        excel.creaColumna(hoja, columnaA);

        posicion.setCol(2);
        posicion.setRow(1);
        var columnaC = new ColumnaFila(posicion, List.of(
                new Celda("Inventario Red de Distribuidores", "BlancoEncabezado", 1)
                ,new Celda("", "BlancoEncabezado", 1)
                ,new Celda("Al cierre de " + mesAnterior + " " + fechaAnterior.getYear(), "GrisEncabezado", 2)));
        excel.creaColumna(hoja, columnaC);

        hoja.addMergedRegion(new CellRangeAddress(1, 2,
                2, 3));

        posicion.setCol(3);
        posicion.setRow(3);
        var columnaD = new ColumnaFila(posicion, List.of(
                new Celda("Al cierre de " + mesCadena + " " + peticion.getAnio(), "GrisEncabezado", 2)));
        excel.creaColumna(hoja, columnaD);

        posicion.setCol(5);
        posicion.setRow(1);
        var columnaF = new ColumnaFila(posicion, List.of(
                new Celda("Días Inventario Menudeo", "BlancoEncabezado", 2)
                ,new Celda("Al cierre de " + mesCadena + " " + peticion.getAnio(), "GrisEncabezado", 2)));
        excel.creaColumna(hoja, columnaF);


    }

    private void cuerpo(CrearExcel excel, XSSFSheet hoja, List<Margen> consolidado) {
        var agrupado = service.groupByMarca(consolidado);

        List<List<Celda>> tabla = new ArrayList<>();
        agrupado.entrySet()
                .stream()
                .forEach( marca -> {
                    marca.getValue()
                            .forEach(modelo -> tabla.add(modelo.toCeldas("Blanco")));
                    tabla.add(marca.getValue().stream().reduce(Margen::sumarConMarca).get().toCeldas("Gris"));

                });

        for(int i = 0; i < tabla.size(); i++){
            excel.creaFila(hoja, new ColumnaFila(new Posicion(0,5+i), tabla.get(i)));
        }

        var stellantis = consolidado.stream().reduce(Margen::sumarConMarca).get();
        stellantis.setModelo("Stellantis");
        excel.creaFila( hoja, new ColumnaFila(new Posicion(0, tabla.size()+6), stellantis.toCeldas("Gris")));
    }


    private void creaEstilos(CrearExcel excel) {
        var limpioColor = new ColorExcel("Limpio","#FFFFFF", "#FFFFFF");
        var limpio = new EstiloCeldaExcel(limpioColor, excel.getWb(),12
                , Optional.of(HorizontalAlignment.CENTER),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(), BorderStyle.NONE, Optional.empty(),"0.0%",
                false, Optional.of("#FFFFFF"), Optional.of("#FFFFFF") , false);
        excel.getEstilos().add(limpio);

        var blancoColor = new ColorExcel("Blanco","#FFFFFF", "#FFFFFF");
        var blanco = new EstiloCeldaExcel(blancoColor, excel.getWb(),12
                , Optional.of(HorizontalAlignment.CENTER),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(), BorderStyle.THIN, Optional.of("lr"),"0.0%",
                false, Optional.of("#000000"), Optional.of("#FF0000") , false);
        excel.getEstilos().add(blanco);

        var blancoEColor = new ColorExcel("BlancoEncabezado","#FFFFFF", "#FFFFFF");
        var blancoE = new EstiloCeldaExcel(blancoEColor, excel.getWb(),12
                , Optional.of(HorizontalAlignment.CENTER),Optional.of(VerticalAlignment.CENTER),
                Optional.empty(), BorderStyle.THIN, Optional.empty(),"0.0%",
                true, Optional.of("#000000"), Optional.of("#FF0000"), true );
        excel.getEstilos().add(blancoE);

        var grisColor = new ColorExcel("Gris","#BFBFBF", "#BFBFBF");
        var gris = new EstiloCeldaExcel(grisColor, excel.getWb(),12
                , Optional.of(HorizontalAlignment.CENTER),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(), BorderStyle.THIN, Optional.of("bt"),"0.0%",
                true, Optional.of("#000000"), Optional.of("#FF0000") , false);
        excel.getEstilos().add(gris);

        var grisEColor = new ColorExcel("GrisEncabezado","#BFBFBF", "#BFBFBF");
        var grisE = new EstiloCeldaExcel(grisEColor, excel.getWb(),12
                , Optional.of(HorizontalAlignment.CENTER),Optional.of(VerticalAlignment.CENTER),
                Optional.empty(), BorderStyle.THIN, Optional.of("b"),"0.0%",
                true, Optional.of("#000000"), Optional.of("#FF0000") , true);
        excel.getEstilos().add(grisE);
    }




    public String getNombreArchivo() {
        return nombreArchivo;
    }
}
