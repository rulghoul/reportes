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
        var anchos = List.of(2,10,75,35,35,35,2,40,40,35);
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
        columnaC.add(new Celda("Ventas totales", "Estandar", 1));
        columnaC.add(new Celda("Venta totales AUTOS NUEVOS en UNIDADES", "Estandar", 1));
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
        columnaC.add(new Celda("Utilidad Bruta Total ", "Estandar", 1));

        columnaC.add(new Celda("Variables Nuevos", "normalIzquierda", 1));
        columnaC.add(new Celda("Variables Usados", "normalIzquierda", 1));
        columnaC.add(new Celda("Plan Piso", "normalIzquierda", 1));
        columnaC.add(new Celda("De venta de Mecánica", "normalIzquierda", 1));
        columnaC.add(new Celda("De venta de H&P", "normalIzquierda", 1));
        columnaC.add(new Celda("De venta de refacciones", "normalIzquierda", 1));
        columnaC.add(new Celda("Fijos", "normalIzquierda", 1));
        columnaC.add(new Celda("Sueldos de propietarios y funcionarios", "normalIzquierda", 1));
        columnaC.add(new Celda("Gastos totales sin renta o equivalentes", "Estandar", 1));

        columnaC.add(new Celda("Utilidad de Operación sin rentas ni depreciación ", "Estandar", 1));
        columnaC.add(new Celda("Bienes Inmuebles - renta y equivalentes", "normalIzquierda", 1));
        columnaC.add(new Celda("Utilidad de la operación", "Estandar", 1));
        columnaC.add(new Celda("Otros ingresos y deducciones", "normalIzquierda", 1));
        columnaC.add(new Celda("Utilidad neta reportada", "Estandar", 1));

        columnaC.add(new Celda("", "Rojo", 1));

        columnaC.add(new Celda("Dealer que reportaron utilidad", "Estandar", 1));
        columnaC.add(new Celda("% de dealers con utilidad ", "Estandar", 1));
        columnaC.add(new Celda("Utilidad Neta / Ventas Totales (ROS)", "Estandar", 1));

        columnaC.add(new Celda("", "Rojo", 1));
        columnaC.add(new Celda("", "Rojo", 1));
        columnaC.add(new Celda("", "Rojo", 1));

        columnaC.add(new Celda("EBITDA / Ventas Totales", "normalIzquierda", 1));
        columnaC.add(new Celda("Absorción de Servicio", "normalIzquierda", 1));
        columnaC.add(new Celda("ROI %", "normalIzquierda", 1));
        columnaC.add(new Celda("ROI  Operativo%", "normalIzquierda", 1));

        columnaC.add(new Celda("", "Rojo", 1));

        columnaC.add(new Celda("Margen Bruto", "Estandar", 1));
        columnaC.add(new Celda("Plan piso (Utilidad bruta)", "Estandar", 1));

        columnaC.add(new Celda("Autos Nuevos  MENUDEO", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Nuevos  Flotillas", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Nuevos  Bonos planta", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Nuevos  TOTAL SIN FLOTILLAS", "normalIzquierda", 1));
        columnaC.add(new Celda("Autos Usados", "normalIzquierda", 1));
        columnaC.add(new Celda("Contratos de Servicio ", "normalIzquierda", 1));
        columnaC.add(new Celda("Mecánica", "normalIzquierda", 1));
        columnaC.add(new Celda("Hojalatería y Pintura", "normalIzquierda", 1));
        columnaC.add(new Celda("Refacciones", "normalIzquierda", 1));

        columnaC.add(new Celda("Total", "Estandar", 1));

        columnaC.add(new Celda("* NA  =  No Aplica", "Estandar", 1));
        columnaC.add(new Celda("", "Rojo", 1));
        columnaC.add(new Celda("No. De meses", "Estandar", 1));

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

        var fuenteBlanca = excel.getWb().createFont();
        fuenteBlanca.setColor(new XSSFColor(Utilidades.convierteRGB("FBFBFB"), null));
        fuenteBlanca.setFontName(encabezado.getFont().getFontName());
        fuenteBlanca.setFontHeight(encabezado.getFont().getFontHeight());
        fuenteBlanca.setBold(true);

        var fuenteNegra = excel.getWb().createFont();
        fuenteNegra.setColor(new XSSFColor(Utilidades.convierteRGB("000000"), null));
        fuenteNegra.setFontName(encabezado.getFont().getFontName());
        fuenteNegra.setFontHeight(encabezado.getFont().getFontHeight());
        fuenteNegra.setBold(false);

        var fuenteNegraBold = excel.getWb().createFont();
        fuenteNegraBold.setColor(new XSSFColor(Utilidades.convierteRGB("000000"), null));
        fuenteNegraBold.setFontName(encabezado.getFont().getFontName());
        fuenteNegraBold.setFontHeight(encabezado.getFont().getFontHeight());
        fuenteNegraBold.setBold(true);


        var fuenteNegraGirada = excel.getWb().createFont();
        fuenteNegraGirada.setColor(new XSSFColor(Utilidades.convierteRGB("000000"), null));
        fuenteNegraGirada.setFontName(encabezado.getFont().getFontName());
        fuenteNegraGirada.setFontHeight(18);
        fuenteNegraGirada.setBold(true);

        var fuenteAzul = excel.getWb().createFont();
        fuenteAzul.setColor(new XSSFColor(Utilidades.convierteRGB("#0070C0"), null));
        fuenteAzul.setFontName(encabezado.getFont().getFontName());
        fuenteAzul.setFontHeight(encabezado.getFont().getFontHeight());
        fuenteAzul.setBold(true);

        excel.agregaColor(new ColorExcel("Black","#000000", "#000000"));
        excel.agregaColor(new ColorExcel("BlackRotate","#FFFFFF", "#FFFFFF"));
        excel.agregaColor(new ColorExcel("tile","#DEEBF7", "#DEEBF7"));
        excel.agregaColor(new ColorExcel("letraAzul","#DEEBF7", "#DEEBF7"));
        //Modifica Fuente
        var nomEstilos = new ArrayList<>(Arrays.asList("", "BlackRotate"));
        var rotacion = new Short("90");
        excel.getEstilos().stream()
                .filter(estilo -> nomEstilos.contains(estilo.getNombre()))
                .forEach(estilo -> {
                    estilo.getOdd().setBorderTop(BorderStyle.NONE);
                    estilo.getOdd().setBorderBottom(BorderStyle.NONE);
                    estilo.getOdd().setBorderLeft(BorderStyle.NONE);
                    estilo.getOdd().setBorderRight(BorderStyle.NONE);
                    estilo.getOdd().setWrapText(true);
                    estilo.getOdd().setAlignment(HorizontalAlignment.CENTER);
                    estilo.getOdd().setVerticalAlignment(VerticalAlignment.CENTER);
                    estilo.getOdd().setFont(fuenteNegraGirada);

                    estilo.getNormal().setBorderTop(BorderStyle.NONE);
                    estilo.getNormal().setBorderBottom(BorderStyle.NONE);
                    estilo.getNormal().setBorderLeft(BorderStyle.NONE);
                    estilo.getNormal().setBorderRight(BorderStyle.NONE);
                    estilo.getNormal().setWrapText(true);
                    estilo.getNormal().setAlignment(HorizontalAlignment.CENTER);
                    estilo.getNormal().setVerticalAlignment(VerticalAlignment.CENTER);
                    estilo.getNormal().setFont(fuenteNegraGirada);
                    estilo.getNormal().setRotation(rotacion);
                });

        var separadorColor = new ColorExcel("separador", "#7F9AC7", "#7F9AC7");
        var separador = new EstiloCeldaExcel(separadorColor,excel.getWb(),16
                , Optional.of(HorizontalAlignment.LEFT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),"NONE", "0.0%", true);
        excel.getEstilos().add(separador);

        var EncabezadoIzquierdaColor = new ColorExcel("EncabezadoIzquierda", "#002B7F", "#002B7F");
        var EncabezadoIzquierda = new EstiloCeldaExcel(EncabezadoIzquierdaColor,excel.getWb(),16
                , Optional.of(HorizontalAlignment.LEFT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),"NONE", "0.0%", true);
        excel.getEstilos().add(EncabezadoIzquierda);


        var EncabezadoCentroColor = new ColorExcel("EncabezadoCentro", "#002B7F", "#002B7F");
        var EncabezadoCentro = new EstiloCeldaExcel(EncabezadoCentroColor,excel.getWb(),14
                , Optional.of(HorizontalAlignment.CENTER),Optional.of(VerticalAlignment.CENTER),
                Optional.empty(),"NONE", "0.0%", true);
        excel.getEstilos().add(EncabezadoCentro);

        var grisEncabezadoColor = new ColorExcel("grisEncabezado", "#F2F2F2", "#F2F2F2");
        var grisEncabezado = new EstiloCeldaExcel(grisEncabezadoColor,excel.getWb(),16
                , Optional.of(HorizontalAlignment.CENTER),Optional.of(VerticalAlignment.CENTER),
                Optional.empty(),"NONE", "0.0%", true);
        excel.getEstilos().add(grisEncabezado);

        var normalEncabezadoColor = new ColorExcel("normalEncabezado", "#FFFFFF", "#FFFFFF");
        var normalEncabezado = new EstiloCeldaExcel(normalEncabezadoColor,excel.getWb(),20
                , Optional.of(HorizontalAlignment.LEFT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),"NONE", "0.0%",false);
        excel.getEstilos().add(normalEncabezado);

        var normalEncabezadoDerechaColor = new ColorExcel("normalEncabezadoDerecha", "#FFFFFF", "#FFFFFF");
        var normalEncabezadoDerecha = new EstiloCeldaExcel(normalEncabezadoDerechaColor,excel.getWb(),18
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),"NONE", "0.0%",true);
        excel.getEstilos().add(normalEncabezadoDerecha);

        var normalIzquierdaColor = new ColorExcel("normalIzquierda", "#FFFFFF", "#FFFFFF");
        var normalIzquierda = new EstiloCeldaExcel(normalIzquierdaColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.LEFT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),"NONE", "0.0%",false);
        excel.getEstilos().add(normalIzquierda);

        var normalDerechaColor = new ColorExcel("normalDerecha", "#FFFFFF", "#FFFFFF");
        var normalDerecha = new EstiloCeldaExcel(normalDerechaColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),"NONE", "0.0%",false);
        excel.getEstilos().add(normalDerecha);
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }
}
