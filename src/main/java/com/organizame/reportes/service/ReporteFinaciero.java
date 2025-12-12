package com.organizame.reportes.service;

import com.organizame.reportes.dto.DaoResumenPeriodo;
import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.persistence.entities.BnkEstadofinanciero;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.repository.BnkEstadofinancieroRepository;
import com.organizame.reportes.repository.service.EstadoFinancieroService;
import com.organizame.reportes.utils.Utilidades;
import com.organizame.reportes.utils.excel.ColorExcel;
import com.organizame.reportes.utils.excel.CrearExcel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFColor;
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

    public ByteArrayInputStream CrearExcelRanking(RequestRanking request) throws IOException {
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

        if (resultadoActual.isEmpty()) {
            throw new SinDatos("No se encontraron datos para el origen el periodo de " +
                    eneroActual.format(this.fechaSmall) + mesActual.format(this.fechaSmall));
        }

        this.nombreArchivo = "Ranking " + request.getAnio() + " Vs " + (request.getAnio() - 1);
        //Datos resumidos

        //Datos para hojas
        CrearExcel excel = new CrearExcel();

        this.modifiStyle(excel);

        String fecha = (Month.of(1).getDisplayName(TextStyle.FULL, Locale.of("es", "MX")) + " - " +
                Month.of(request.getMes()).getDisplayName(TextStyle.FULL, Locale.of("es", "MX"))).toUpperCase();




        fin = System.nanoTime();

        duracionNanos = fin - inicio;
        duracionMillis = duracionNanos / 1_000_000;

        log.info("La creacion del excel fue: {} ms", duracionMillis);
        return excel.guardaExcel();
    }



    private void mensual(Set<DaoResumenPeriodo> filtradoActual, Set<DaoResumenPeriodo> filtradoAnterior, Integer totalActual, Integer totalAnterior, CrearExcel excel, RequestRanking request, String fecha) {

    }

    private void acumulado(Set<DaoResumenPeriodo> filtradoActual, Integer totalActual, CrearExcel excel, RequestRanking request, String fecha) {
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
        fuenteNegra.setBold(true);
        excel.agregaColor(new ColorExcel("Black","#000000", "#000000"));
        excel.agregaColor(new ColorExcel("greyHeader","#BFBFBF", "#BFBFBF"));
        excel.agregaColor(new ColorExcel("tile","#DEEBF7", "#DEEBF7"));
        //Modifica Fuente
        var nomEstilos = new ArrayList<>(Arrays.asList("greyHeader", "tile"));
        excel.getEstilos().stream()
                .filter(estilo -> nomEstilos.contains(estilo.getNombre()))
                .forEach(estilo -> {
                    estilo.getOdd().setBorderTop(BorderStyle.MEDIUM);
                    estilo.getOdd().setBorderBottom(BorderStyle.MEDIUM);
                    estilo.getOdd().setBorderLeft(BorderStyle.MEDIUM);
                    estilo.getOdd().setBorderRight(BorderStyle.MEDIUM);
                    estilo.getOdd().setWrapText(true);
                    estilo.getOdd().setAlignment(HorizontalAlignment.CENTER);
                    estilo.getOdd().setVerticalAlignment(VerticalAlignment.CENTER);
                    estilo.getOdd().setFont(fuenteNegra);

                    estilo.getNormal().setBorderTop(BorderStyle.MEDIUM);
                    estilo.getNormal().setBorderBottom(BorderStyle.MEDIUM);
                    estilo.getNormal().setBorderLeft(BorderStyle.MEDIUM);
                    estilo.getNormal().setBorderRight(BorderStyle.MEDIUM);
                    estilo.getNormal().setWrapText(true);
                    estilo.getNormal().setAlignment(HorizontalAlignment.CENTER);
                    estilo.getNormal().setVerticalAlignment(VerticalAlignment.CENTER);
                    estilo.getNormal().setFont(fuenteNegra);
                });
    }
}
