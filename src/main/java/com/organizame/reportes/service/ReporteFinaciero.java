package com.organizame.reportes.service;

import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.persistence.entities.BnkEstadofinanciero;
import com.organizame.reportes.repository.service.EstadoFinancieroService;
import com.organizame.reportes.utils.Utilidades;
import com.organizame.reportes.utils.excel.ColorExcel;
import com.organizame.reportes.utils.excel.CrearExcel;
import com.organizame.reportes.utils.excel.EstiloCeldaExcel;
import com.organizame.reportes.utils.excel.dto.Celda;
import com.organizame.reportes.utils.excel.dto.ColumnaFila;
import com.organizame.reportes.utils.excel.dto.Posicion;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

@Slf4j
@Service
public class ReporteFinaciero {


    private final DateTimeFormatter fechaSmall;

    private final EstadoFinancieroService service;

    private String nombreArchivo;

    @Autowired
    public ReporteFinaciero(EstadoFinancieroService service) {
        this.service = service;
        this.fechaSmall = DateTimeFormatter.ofPattern("MMMuu");
    }

    public ByteArrayInputStream CrearExcelFinanciero(RequestRanking request) throws IOException {
        //datos brutos todos los origenes por
        var eneroActual = LocalDate.of(request.getAnio(), 1, 1);
        var mesActual = LocalDate.of(request.getAnio(), request.getMes(), 1);

        var eneroAnterior = LocalDate.of(request.getAnio() - 1, 1, 1);
        var mesAnterior = LocalDate.of(request.getAnio() - 1, request.getMes(), 1);
        long inicio = System.nanoTime();

        List<BnkEstadofinanciero> resultadoActual = service.findEstadoFechas(eneroActual, mesActual);

        List<BnkEstadofinanciero> resultadoAnterior = service.findEstadoFechas(eneroAnterior, mesAnterior);

        long fin = System.nanoTime();

        long duracionNanos = fin - inicio;
        long duracionMillis = duracionNanos / 1_000_000;

        log.info("Recuperacion de datos fue: {} ms", duracionMillis);
        inicio = System.nanoTime();

        String mesCadena = Month.of(request.getMes()).getDisplayName(TextStyle.FULL, Locale.of("es", "MX"));
        String fecha = (Month.of(1).getDisplayName(TextStyle.FULL, Locale.of("es", "MX"))
                + " - " + mesCadena).toUpperCase();

        this.nombreArchivo = "Estado Financiero " + mesCadena + " " + request.getAnio();
        //Datos resumidos

        //Datos para hojas
        CrearExcel excel = new CrearExcel();

        this.modifiStyle(excel);




        this.estadofinanciero(resultadoActual, resultadoAnterior, excel, request, fecha, mesCadena);


        fin = System.nanoTime();

        duracionNanos = fin - inicio;
        duracionMillis = duracionNanos / 1_000_000;

        log.info("La creacion del excel fue: {} ms", duracionMillis);
        return excel.guardaExcel();
    }



    private void estadofinanciero(List<BnkEstadofinanciero> filtradoActual, List<BnkEstadofinanciero> filtradoAnterior,
                                  CrearExcel excel, RequestRanking request, String fecha, String mesCadena) {

        var hoja = excel.CrearHoja("Edos Finan "+ mesCadena.substring(0,2));

        this.encabezadosYtitiulos(excel, hoja);

        var mesActual = filtradoActual.getLast();
        var mesAnterior = filtradoAnterior.getLast();

        excel.creaColumna(hoja, new ColumnaFila(new Posicion(3,2), mesActual.toCeldas(request.getAnio().toString(), mesCadena.toUpperCase())));
        excel.creaColumna(hoja, new ColumnaFila(new Posicion(4,2), mesAnterior.toCeldas(String.valueOf(request.getAnio()-1), mesCadena.toUpperCase())));

        excel.creaColumna(hoja, new ColumnaFila(new Posicion(5,5), mesActual.variacionCon(mesAnterior)));
        //Divisor
        excel.creaColumna(hoja, new ColumnaFila(new Posicion(6,2), List.of(new Celda("", "separador", 67))));

        var consolidadoActual = filtradoActual.stream().reduce(new BnkEstadofinanciero(), BnkEstadofinanciero::sumarCon);
        consolidadoActual.setVentamuestra(mesActual.getVentamuestra());
        consolidadoActual.setUtilidaddealersreportaronutilidad(mesActual.getUtilidaddealersreportaronutilidad());
        consolidadoActual.setPeriodomes(mesActual.getPeriodomes());
        var consolidadoAnterior = filtradoAnterior.stream().reduce(new BnkEstadofinanciero(), BnkEstadofinanciero::sumarCon);
        consolidadoAnterior.setVentamuestra(mesAnterior.getVentamuestra());
        consolidadoAnterior.setUtilidaddealersreportaronutilidad(mesAnterior.getUtilidaddealersreportaronutilidad());
        consolidadoAnterior.setPeriodomes(mesAnterior.getPeriodomes());

        excel.creaColumna(hoja, new ColumnaFila(new Posicion(7,2), consolidadoActual.toCeldas(request.getAnio().toString(), fecha)));
        excel.creaColumna(hoja, new ColumnaFila(new Posicion(8,2), consolidadoAnterior.toCeldas(String.valueOf(request.getAnio()-1), fecha)));
        excel.creaColumna(hoja, new ColumnaFila(new Posicion(9,5), consolidadoActual.variacionCon(consolidadoAnterior)));

        //Ajustar el estilo de la variacion

        //Se ajustan los ancos de columnas
        var anchos = List.of(2,10,85,40,40,35,2,45,45,35);
        for(var i = 0; anchos.size() > i; i++ ){
            hoja.setColumnWidth(i, anchos.get(i)*150);
        }

        //Se congelan secciones

        hoja.createFreezePane(3, 5);
    }

    private void encabezadosYtitiulos(CrearExcel excel, XSSFSheet hoja){

        var posicion = new Posicion(0,0);

        List<Celda> columnaA1 = new ArrayList<>();
        columnaA1.add(new Celda("INFORMACION FINANCIERA DE  STELLANTIS", "normalEncabezado", 5));


        excel.creaFila(hoja, new ColumnaFila(posicion, columnaA1));

        posicion.setRow(1);
        posicion.setCol(0);
        List<Celda> columnaA2 = new ArrayList<>();
        columnaA2.add(new Celda("STELLANTIS", "EncabezadoIzquierda", 3));
        excel.creaFila(hoja, new ColumnaFila(posicion, columnaA2));

        posicion.setRow(5);
        posicion.setCol(1);

        List<Celda> columnaB = new ArrayList<>();

        columnaB.add(new Celda("VENTAS", "BlackRotate", 12));
        columnaB.add(new Celda("", "BlackRotate", 11));
        columnaB.add(new Celda("GASTOS", "BlackRotate", 9));

        excel.creaColumna(hoja, new ColumnaFila(posicion, columnaB));

        List<Celda> columnaC = new ArrayList<>();

        columnaC.add(new Celda("Muestra", "normalEncabezadoDerecha", 1));
        columnaC.add(new Celda("Autos nuevos MENUDEO", "normalDerecha", 1));
        columnaC.add(new Celda("Autos nuevos Flotillas", "normalDerecha", 1));
        columnaC.add(new Celda("Mercancías varias y otros", "normalDerecha", 1));
        columnaC.add(new Celda("TOTAL de autos nuevos", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Usados", "normalIzquierda", 1));
        columnaC.add(new Celda("Contratos de servicio nuevos y usados", "normalIzquierda", 1));
        columnaC.add(new Celda("Mecánica ", "normalIzquierda", 1));
        columnaC.add(new Celda("H&P", "normalIzquierda", 1));
        columnaC.add(new Celda("Refacciones", "normalIzquierda", 1));
        columnaC.add(new Celda("Ventas totales", "normalIzquierdaBold", 1));
        columnaC.add(new Celda("Venta totales AUTOS NUEVOS en UNIDADES", "normalDerechaBold", 1));
        columnaC.add(new Celda("Precio promedio x unidad (MEZCLA)", "normalDerecha", 1));

        columnaC.add(new Celda("Autos nuevos MENUDEO", "normalDerecha", 1));
        columnaC.add(new Celda("Autos nuevos Flotillas", "normalDerecha", 1));
        columnaC.add(new Celda("Bonos planta", "normalDerecha", 1));
        columnaC.add(new Celda("Transferencias/Bonos e Incentivos Financieras", "normalDerecha", 1));
        columnaC.add(new Celda("Autos Nuevos ", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Usados", "normalIzquierda", 1));
        columnaC.add(new Celda("Contratos de servicio nuevos y usados", "normalIzquierda", 1));
        columnaC.add(new Celda("Mecánica ", "normalIzquierda", 1));
        columnaC.add(new Celda("H&P", "normalIzquierda", 1));
        columnaC.add(new Celda("Refacciones", "normalIzquierda", 1));
        columnaC.add(new Celda("Utilidad Bruta Total ", "normalDerechaBoldBottom", 1));

        columnaC.add(new Celda("Variables Nuevos", "normalIzquierda", 1));
        columnaC.add(new Celda("Variables Usados", "normalIzquierda", 1));
        columnaC.add(new Celda("Plan Piso", "normalIzquierda", 1));
        columnaC.add(new Celda("De venta de Mecánica", "normalIzquierda", 1));
        columnaC.add(new Celda("De venta de H&P", "normalIzquierda", 1));
        columnaC.add(new Celda("De venta de refacciones", "normalIzquierda", 1));
        columnaC.add(new Celda("Fijos", "normalIzquierda", 1));
        columnaC.add(new Celda("Sueldos de propietarios y funcionarios", "normalIzquierda", 1));
        columnaC.add(new Celda("Gastos totales sin renta o equivalentes", "normalDerechaBoldBottom", 1));

        columnaC.add(new Celda("Utilidad de Operación sin rentas ni depreciación ", "normalIzquierdaBold", 1));
        columnaC.add(new Celda("Bienes Inmuebles - renta y equivalentes", "normalIzquierda", 1));
        columnaC.add(new Celda("Utilidad de la operación", "normalIzquierdaBold", 1));
        columnaC.add(new Celda("Otros ingresos y deducciones", "normalIzquierda", 1));
        columnaC.add(new Celda("Utilidad neta reportada", "normalIzquierdaBold", 1));

        columnaC.add(new Celda("", "normalIzquierda", 1));

        columnaC.add(new Celda("Dealer que reportaron utilidad", "azulEncabezado", 1));
        columnaC.add(new Celda("% de dealers con utilidad ", "normalDerechaBold", 1));
        columnaC.add(new Celda("Utilidad Neta / Ventas Totales (ROS)", "normalDerechaBold", 1));

        columnaC.add(new Celda("", "normalIzquierda", 1));
        columnaC.add(new Celda("", "normalIzquierda", 1));
        columnaC.add(new Celda("", "normalIzquierda", 1));

        columnaC.add(new Celda("EBITDA / Ventas Totales", "normalIzquierda", 1));
        columnaC.add(new Celda("Absorción de Servicio", "normalIzquierda", 1));
        columnaC.add(new Celda("ROI %", "normalIzquierda", 1));
        columnaC.add(new Celda("ROI  Operativo%", "normalIzquierda", 1));

        columnaC.add(new Celda("", "normalIzquierda", 1));

        columnaC.add(new Celda("Margen Bruto", "negro", 1));
        columnaC.add(new Celda("Plan piso (Utilidad bruta)", "negro", 1));

        columnaC.add(new Celda("Autos Nuevos  MENUDEO", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Nuevos  Flotillas", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Nuevos  Bonos planta", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Nuevos  TOTAL SIN FLOTILLAS", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Usados", "normalIzquierda", 1));
        columnaC.add(new Celda("Contratos de Servicio ", "normalIzquierda", 1));
        columnaC.add(new Celda("Mecánica", "normalIzquierda", 1));
        columnaC.add(new Celda("Hojalatería y Pintura", "normalIzquierda", 1));
        columnaC.add(new Celda("Refacciones", "normalIzquierda", 1));

        columnaC.add(new Celda("Total", "negro", 1));

        columnaC.add(new Celda("* NA  =  No Aplica", "normalIzquierda", 1));
        columnaC.add(new Celda("", "normalDerecha", 1));
        columnaC.add(new Celda("No. De meses", "grisOscuro", 1));

        posicion.setCol(2);
        posicion.setRow(4);
        excel.creaColumna(hoja, new ColumnaFila(posicion, columnaC));

        var variacion = List.of(new Celda("Variación", "grisEncabezado", 2 ),new Celda("", "EncabezadoIzquierda", 1 ));

        excel.creaColumna(hoja, new ColumnaFila(new Posicion(5,2), variacion));
        excel.creaColumna(hoja, new ColumnaFila(new Posicion(9,2), variacion));

    }



    private void modifiStyle(CrearExcel excel){
        //Modifica los encabezados para este reporte
        var encabezado = excel.getEncabezado();
        encabezado.setWrapText(true);
        encabezado.setAlignment(HorizontalAlignment.CENTER);
        encabezado.setVerticalAlignment(VerticalAlignment.CENTER);
        excel.setEncabezado(encabezado);

        excel.agregaColor(new ColorExcel("Black","#000000", "#000000"));
        var BlackRotateColor = new ColorExcel("BlackRotate","#FFFFFF", "#FFFFFF");
        var BlackRotate = new EstiloCeldaExcel(BlackRotateColor, excel.getWb(),16
                , Optional.of(HorizontalAlignment.CENTER), Optional.of(VerticalAlignment.CENTER),
                Optional.of(new Short("90")),BorderStyle.THIN, Optional.of("tb"),
                "0.0%", true, Optional.empty());
        excel.getEstilos().add(BlackRotate);
        excel.agregaColor(new ColorExcel("tile","#DEEBF7", "#DEEBF7"));


        var azulColor = new ColorExcel("Azul","#B4C7E7", "#B4C7E7");
        var azul = new EstiloCeldaExcel(azulColor, excel.getWb(),12
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%",
                false, Optional.empty());
        excel.getEstilos().add(azul);

        var azulEncabezadoColor = new ColorExcel("azulEncabezado","#B4C7E7", "#B4C7E7");
        var azulEncabezado = new EstiloCeldaExcel(azulEncabezadoColor,excel.getWb(),16
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%", true, Optional.empty());
        excel.getEstilos().add(azulEncabezado);


        var separadorColor = new ColorExcel("separador", "#7F9AC7", "#7F9AC7");
        var separador = new EstiloCeldaExcel(separadorColor,excel.getWb(),16
                , Optional.of(HorizontalAlignment.LEFT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%", true, Optional.empty());
        excel.getEstilos().add(separador);

        var EncabezadoIzquierdaColor = new ColorExcel("EncabezadoIzquierda", "#002B7F", "#002B7F");
        var EncabezadoIzquierda = new EstiloCeldaExcel(EncabezadoIzquierdaColor,excel.getWb(),16
                , Optional.of(HorizontalAlignment.LEFT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%", true, Optional.empty());
        excel.getEstilos().add(EncabezadoIzquierda);


        var EncabezadoCentroColor = new ColorExcel("EncabezadoCentro", "#002B7F", "#002B7F");
        var EncabezadoCentro = new EstiloCeldaExcel(EncabezadoCentroColor,excel.getWb(),14
                , Optional.of(HorizontalAlignment.CENTER),Optional.of(VerticalAlignment.CENTER),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%", true, Optional.empty());
        excel.getEstilos().add(EncabezadoCentro);

        var grisEncabezadoColor = new ColorExcel("grisEncabezado", "#F2F2F2", "#F2F2F2");
        var grisEncabezado = new EstiloCeldaExcel(grisEncabezadoColor,excel.getWb(),16
                , Optional.of(HorizontalAlignment.CENTER),Optional.of(VerticalAlignment.CENTER),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%", true, Optional.empty());
        excel.getEstilos().add(grisEncabezado);

        var normalEncabezadoColor = new ColorExcel("normalEncabezado", "#FFFFFF", "#FFFFFF");
        var normalEncabezado = new EstiloCeldaExcel(normalEncabezadoColor,excel.getWb(),20
                , Optional.of(HorizontalAlignment.LEFT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%",false, Optional.empty());
        excel.getEstilos().add(normalEncabezado);

        var normalEncabezadoDerechaColor = new ColorExcel("normalEncabezadoDerecha", "#FFFFFF", "#FFFFFF");
        var normalEncabezadoDerecha = new EstiloCeldaExcel(normalEncabezadoDerechaColor,excel.getWb(),18
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%",true, Optional.empty());
        excel.getEstilos().add(normalEncabezadoDerecha);

        var normalIzquierdaColor = new ColorExcel("normalIzquierda", "#FFFFFF", "#FFFFFF");
        var normalIzquierda = new EstiloCeldaExcel(normalIzquierdaColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.LEFT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%",false, Optional.empty());
        excel.getEstilos().add(normalIzquierda);

        var normalDerechaColor = new ColorExcel("normalDerecha", "#FFFFFF", "#FFFFFF");
        var normalDerecha = new EstiloCeldaExcel(normalDerechaColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%",false,Optional.empty());
        excel.getEstilos().add(normalDerecha);

        var normalIzquierdaBoldColor = new ColorExcel("normalIzquierdaBold", "#FFFFFF", "#FFFFFF");
        var normalIzquierdaBold = new EstiloCeldaExcel(normalIzquierdaBoldColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.LEFT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%",true, Optional.empty());
        excel.getEstilos().add(normalIzquierdaBold);

        var normalDerechaBoldColor = new ColorExcel("normalDerechaBold", "#FFFFFF", "#FFFFFF");
        var normalDerechaBold = new EstiloCeldaExcel(normalDerechaBoldColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%",true,Optional.empty());
        excel.getEstilos().add(normalDerechaBold);

        var normalDerechaBoldBottomColor = new ColorExcel("normalDerechaBoldBottom", "#FFFFFF", "#FFFFFF");
        var normalDerechaBoldBottom = new EstiloCeldaExcel(normalDerechaBoldBottomColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.THIN, Optional.of("b"),"0.0%",true,Optional.empty());
        excel.getEstilos().add(normalDerechaBoldBottom);

        var negroColor = new ColorExcel("negro", "#FFFFFF", "#FFFFFF");
        var negro = new EstiloCeldaExcel(negroColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%",false,Optional.empty());
        excel.getEstilos().add(negro);

        var grisOscuroColor = new ColorExcel("grisOscuro", "#333333", "#333333");
        var grisOscuro = new EstiloCeldaExcel(grisOscuroColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0.0%",false,Optional.empty());
        excel.getEstilos().add(grisOscuro);


        var letraAzulColor = new ColorExcel("letraAzul","#FFFFFF", "#FFFFFF");
        var letraAzul = new EstiloCeldaExcel(letraAzulColor,excel.getWb(),12
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.NONE, Optional.empty(),"0%",
                true, Optional.of("#0070C0"));
        excel.getEstilos().add(letraAzul);


    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }
}
