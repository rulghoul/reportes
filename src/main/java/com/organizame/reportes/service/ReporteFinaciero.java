package com.organizame.reportes.service;

import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.persistence.entities.BnkEstadofinanciero;
import com.organizame.reportes.repository.service.EstadoFinancieroService;
import com.organizame.reportes.utils.Utilidades;
import com.organizame.reportes.utils.excel.ColorExcel;
import com.organizame.reportes.utils.excel.CrearExcel;
import com.organizame.reportes.utils.excel.dto.Celda;
import com.organizame.reportes.utils.excel.dto.ColumnaFila;
import com.organizame.reportes.utils.excel.dto.Posicion;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
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

        excel.creaColumna(hoja, new ColumnaFila(new Posicion(3,2), mesActual.toCeldas(request.getAnio().toString(), mesCadena)));
        excel.creaColumna(hoja, new ColumnaFila(new Posicion(4,2), mesAnterior.toCeldas(request.getAnio().toString(), mesCadena)));

        excel.creaColumna(hoja, new ColumnaFila(new Posicion(6,2), List.of(new Celda("", "encabezado", 67))));

        excel.creaColumna(hoja, new ColumnaFila(new Posicion(7,2), mesActual.toCeldas(request.getAnio().toString(), fecha)));
        excel.creaColumna(hoja, new ColumnaFila(new Posicion(8,2), mesAnterior.toCeldas(request.getAnio().toString(), fecha)));
    }

    private void encabezadosYtitiulos(CrearExcel excel, XSSFSheet hoja){

        var posicion = new Posicion(0,0);

        List<Celda> columnaA1 = new ArrayList<>();
        columnaA1.add(new Celda("INFORMACION FINANCIERA DE  STELLANTIS", "Estandar", 5));


        excel.creaFila(hoja, new ColumnaFila(posicion, columnaA1));

        posicion.setRow(1);
        posicion.setCol(0);
        List<Celda> columnaA2 = new ArrayList<>();
        columnaA2.add(new Celda("STELLANTIS", "Encabezado", 3));
        excel.creaFila(hoja, new ColumnaFila(posicion, columnaA2));

        posicion.setRow(5);
        posicion.setCol(1);

        List<Celda> columnaB = new ArrayList<>();

        columnaB.add(new Celda("VENTAS", "BlackRotate", 12));
        columnaB.add(new Celda("", "BlackRotate", 11));
        columnaB.add(new Celda("GASTOS", "BlackRotate", 9));

        excel.creaColumna(hoja, new ColumnaFila(posicion, columnaB));

        List<Celda> columnaC = new ArrayList<>();

        columnaC.add(new Celda("Muestra", "Estandar", 1));
        columnaC.add(new Celda("Autos nuevos MENUDEO", "Estandar", 1));
        columnaC.add(new Celda("Autos nuevos Flotillas", "Estandar", 1));
        columnaC.add(new Celda("Mercancías varias y otros", "Estandar", 1));
        columnaC.add(new Celda("TOTAL de autos nuevos", "Estandar", 1));
        columnaC.add(new Celda("Autos Usados", "Estandar", 1));
        columnaC.add(new Celda("Contratos de servicio nuevos y usados", "Estandar", 1));
        columnaC.add(new Celda("Mecánica ", "Estandar", 1));
        columnaC.add(new Celda("H&P", "Estandar", 1));
        columnaC.add(new Celda("Refacciones", "Estandar", 1));
        columnaC.add(new Celda("Ventas totales", "Estandar", 1));
        columnaC.add(new Celda("Venta totales AUTOS NUEVOS en UNIDADES", "Estandar", 1));
        columnaC.add(new Celda("Precio promedio x unidad (MEZCLA)", "Estandar", 1));
        columnaC.add(new Celda("Autos nuevos MENUDEO", "Estandar", 1));
        columnaC.add(new Celda("Autos nuevos Flotillas", "Estandar", 1));
        columnaC.add(new Celda("Bonos planta", "Estandar", 1));
        columnaC.add(new Celda("Transferencias/Bonos e Incentivos Financieras", "Estandar", 1));
        columnaC.add(new Celda("Autos Nuevos ", "Estandar", 1));
        columnaC.add(new Celda("Autos Usados", "Estandar", 1));
        columnaC.add(new Celda("Contratos de servicio nuevos y usados", "Estandar", 1));
        columnaC.add(new Celda("Mecánica ", "Estandar", 1));
        columnaC.add(new Celda("H&P", "Estandar", 1));
        columnaC.add(new Celda("Refacciones", "Estandar", 1));
        columnaC.add(new Celda("Utilidad Bruta Total ", "Estandar", 1));
        columnaC.add(new Celda("Variables Nuevos", "Estandar", 1));
        columnaC.add(new Celda("Variables Usados", "Estandar", 1));
        columnaC.add(new Celda("Plan Piso", "Estandar", 1));
        columnaC.add(new Celda("De venta de Mecánica", "Estandar", 1));
        columnaC.add(new Celda("De venta de H&P", "Estandar", 1));
        columnaC.add(new Celda("De venta de refacciones", "Estandar", 1));
        columnaC.add(new Celda("Fijos", "Estandar", 1));
        columnaC.add(new Celda("Sueldos de propietarios y funcionarios", "Estandar", 1));
        columnaC.add(new Celda("Gastos totales sin renta o equivalentes", "Estandar", 1));
        columnaC.add(new Celda("Utilidad de Operación sin rentas ni depreciación ", "Estandar", 1));
        columnaC.add(new Celda("Bienes Inmuebles - renta y equivalentes", "Estandar", 1));
        columnaC.add(new Celda("Utilidad de la operación", "Estandar", 1));
        columnaC.add(new Celda("Otros ingresos y deducciones", "Estandar", 1));
        columnaC.add(new Celda("Utilidad neta reportada", "Estandar", 1));
        columnaC.add(new Celda("", "Rojo", 1));
        columnaC.add(new Celda("Dealer que reportaron utilidad", "Estandar", 1));
        columnaC.add(new Celda("% de dealers con utilidad ", "Estandar", 1));
        columnaC.add(new Celda("Utilidad Neta / Ventas Totales (ROS)", "Estandar", 1));
        columnaC.add(new Celda("", "Rojo", 1));
        columnaC.add(new Celda("", "Rojo", 1));
        columnaC.add(new Celda("", "Rojo", 1));
        columnaC.add(new Celda("EBITDA / Ventas Totales", "Estandar", 1));
        columnaC.add(new Celda("Absorción de Servicio", "Estandar", 1));
        columnaC.add(new Celda("ROI %", "Estandar", 1));
        columnaC.add(new Celda("ROI  Operativo%", "Estandar", 1));
        columnaC.add(new Celda("", "Rojo", 1));
        columnaC.add(new Celda("Margen Bruto", "Estandar", 1));
        columnaC.add(new Celda("Plan piso (Utilidad bruta)", "Estandar", 1));
        columnaC.add(new Celda("Autos Nuevos  MENUDEO", "Estandar", 1));
        columnaC.add(new Celda("Autos Nuevos  Flotillas", "Estandar", 1));
        columnaC.add(new Celda("Autos Nuevos  Bonos planta", "Estandar", 1));
        columnaC.add(new Celda("Autos Nuevos  TOTAL SIN FLOTILLAS", "Estandar", 1));
        columnaC.add(new Celda("Autos Usados", "Estandar", 1));
        columnaC.add(new Celda("Contratos de Servicio ", "Estandar", 1));
        columnaC.add(new Celda("Mecánica", "Estandar", 1));
        columnaC.add(new Celda("Hojalatería y Pintura", "Estandar", 1));
        columnaC.add(new Celda("Refacciones", "Estandar", 1));
        columnaC.add(new Celda("Total", "Estandar", 1));
        columnaC.add(new Celda("* NA  =  No Aplica", "Estandar", 1));
        columnaC.add(new Celda("", "Rojo", 1));
        columnaC.add(new Celda("No. De meses", "Estandar", 1));

        posicion.setCol(2);
        posicion.setRow(4);
        excel.creaColumna(hoja, new ColumnaFila(posicion, columnaC));

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
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }
}
