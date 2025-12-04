package com.organizame.reportes.service;

import com.organizame.reportes.dto.DaoResumenPeriodo;
import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.dto.auxiliar.Acumulado;
import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.utils.excel.ColorExcel;
import com.organizame.reportes.utils.excel.CrearExcel;
import com.organizame.reportes.utils.excel.dto.Posicion;
import com.organizame.reportes.utils.graficas.graficas2;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class ReporteRankingMarca {

    private final DateTimeFormatter fechaSmall;

    private final ModeloPeriodoService service;

    private final graficas2 graficas;

    private final DecimalFormat formatoSpanish;

    private final DecimalFormat formatSinDecimales;

    private String nombreArchivo;

    @Autowired
    public ReporteRankingMarca(ModeloPeriodoService service, graficas2 graficas) {
        this.service = service;
        this.graficas = graficas;
        this.fechaSmall = DateTimeFormatter.ofPattern("MMMuu");
        this.formatoSpanish = new DecimalFormat("#,##0.00");
        this.formatSinDecimales = new DecimalFormat("#,##0");

    }

    public ByteArrayInputStream CrearExcelRanking(RequestRanking request) throws IOException {
        //datos brutos todos los origenes por
        var eneroActual = LocalDate.of(request.getAnio(), 1, 1);
        var mesActual = LocalDate.of(request.getAnio(), request.getMes(), 1);

        var eneroAnterior = LocalDate.of(request.getAnio() - 1, 1, 1);
        var mesAnterior = LocalDate.of(request.getAnio() - 1, request.getMes(), 1);
        long inicio = System.nanoTime();

        List<VhcModeloperiodoindustria> resultadoActual = service.findTotalUltimosMeses(eneroActual, mesActual);
        Integer totalActual = resultadoActual.stream()
                .mapToInt(a -> a.getCantidad())
                .sum();
        List<VhcModeloperiodoindustria> resultadoAnterior = service.findTotalUltimosMeses(eneroAnterior, mesAnterior);
        Integer totalAnterior = resultadoAnterior.stream()
                .mapToInt(a -> a.getCantidad())
                .sum();
        long fin = System.nanoTime();


        long duracionNanos = fin - inicio;
        long duracionMillis = duracionNanos / 1_000_000;

        log.info("Recuperacion de datos fue: {} ms", duracionMillis);

        if (resultadoActual.isEmpty()) {
            throw new SinDatos("No se encontraron datos para el origen el periodo de " +
                    eneroActual.format(this.fechaSmall) + mesActual.format(this.fechaSmall));
        }

        this.nombreArchivo = "Ranking " + request.getAnio() + " Vs " + (request.getAnio() - 1);
        //Datos resumidos
        Set<DaoResumenPeriodo> filtradoActual = service.ResumeData(resultadoActual);
        Set<DaoResumenPeriodo> filtradoAnterior = service.ResumeData(resultadoAnterior);

        //Datos para hojas
        CrearExcel excel = new CrearExcel();
        //Modifica los encabezados para este reporte
        var encabezado = excel.getEncabezado();
        encabezado.setWrapText(true);
        encabezado.setAlignment(HorizontalAlignment.CENTER);
        encabezado.setVerticalAlignment(VerticalAlignment.CENTER);
        excel.setEncabezado(encabezado);
        excel.agregaColor(new ColorExcel("Black","#000000", "#000000"));


        String fecha = (Month.of(1).getDisplayName(TextStyle.FULL, Locale.of("es", "MX")) + " - " +
        Month.of(request.getMes()).getDisplayName(TextStyle.FULL, Locale.of("es", "MX"))).toUpperCase();

        this.crearRankingVs(filtradoActual, filtradoAnterior, totalActual, totalAnterior, excel, request, fecha);
        this.creaRanking(filtradoActual, totalActual, excel, request, fecha);


        return excel.guardaExcel();
    }

    private void crearRankingVs(Set<DaoResumenPeriodo> filtradoActual, Set<DaoResumenPeriodo> filtradoAnterior, Integer totalActual, Integer totalAnterior, CrearExcel excel, RequestRanking request, String fecha) {
        String nomHoja = "Ranking " + request.getAnio() + " Vs " + (request.getAnio() - 1);
        String periodoActual = fecha + " " +  request.getAnio();
        String periodoAnterior = fecha + " " +  ( request.getAnio() -1 );
        var black = excel.getEstilos().stream()
                .filter(es -> es.getNombre().equalsIgnoreCase("Black"))
                .map(es -> es.getNormal())
                .findFirst().orElse(excel.getEncabezado());

        var hoja =  excel.CrearHoja(nomHoja);

        var acumuladosActual = this.service.getPortadaAcumulados(filtradoActual, totalActual,totalActual);
        var acumuladosAnterior = this.service.getPortadaAcumulados(filtradoAnterior, totalAnterior,totalAnterior);

        var actualHeader = new FilaTabla("Encabezado",
                List.of("RANKING " + periodoActual, "MARCA",
                        "*AGENCIAS", "VENTAS " + periodoActual,
                        "PROMEDIO DE VENTAS POR AGENCIA  MENSUAL " + periodoActual,
                        "PROMEDIO DE VENTAS POR AGENCIA " + periodoActual,"","",
                        "*AGENCIAS", "VENTAS " + periodoAnterior,
                        "PROMEDIO DE VENTAS POR AGENCIA  MENSUAL " + periodoAnterior,
                        "PROMEDIO DE VENTAS POR AGENCIA " + periodoAnterior,
                        "RANKING " + periodoAnterior, "",
                        "UNIDADES", "%")
        );
        List<FilaTabla> filas = new ArrayList<>();
        filas.add(actualHeader);
        filas.add(actualHeader);
        var count = new AtomicInteger(1);
        var random = new Random(123456);
        filas.addAll(acumuladosActual.stream()
                .filter(acu -> !acu.getFabricante().equalsIgnoreCase("TOTAL"))
                .map(acumulado -> {
                    var rankActual = count.getAndIncrement();
                    var agenciaActual = random.nextInt(80, 150);
                    var acumuladoAnterior = acumuladosAnterior.stream()
                            .filter(acu -> acu.getFabricante().equalsIgnoreCase(acumulado.getFabricante()))
                            .findFirst();
                    Integer rankAnterior = acumuladoAnterior.isPresent() ? acumuladosAnterior.indexOf(acumuladoAnterior.get()) +1 : 99;
                    var agenciaAnterior = random.nextInt(80, 150);
                    var anterior = acumuladoAnterior.isPresent() ? acumuladoAnterior.get() : new Acumulado(acumulado.getFabricante(),0,0,0d,0d);
                    return this.getFilaTabla(acumulado, anterior,
                            agenciaActual, agenciaAnterior,
                            rankActual, rankAnterior,
                            request.getMes());
                }
        ).toList());
        var portPos = new Posicion(2,2);
        excel.creaTablaEstilo(hoja, filas, portPos);
        //modifica la fila de encabezados
        for(var i = 0; i < 14; i++){
            hoja.addMergedRegion(new CellRangeAddress(portPos.getRow(), portPos.getRow()+1,
                    portPos.getCol()+i, portPos.getCol()+i));
        }
        //fusionar celdas de separacion
        hoja.addMergedRegion(new CellRangeAddress(portPos.getRow()+2, portPos.getRow() +acumuladosActual.size(),
                portPos.getCol()+7, portPos.getCol()+7));
        hoja.addMergedRegion(new CellRangeAddress(portPos.getRow()+2, portPos.getRow() +acumuladosActual.size(),
                portPos.getCol()+13, portPos.getCol()+13));

        //Resescribir el header
        portPos.setCol(portPos.getCol()+14);
        //excel.creaTexto(hoja,"DIFERENCIAS " + request.getAnio() + " Vs " + (request.getAnio() - 1), portPos, 1 );

        // Se colocan las flechas de estado
        this.semaforoFlechas(hoja, "I4:I50");
    }

    private void creaRanking(Set<DaoResumenPeriodo> filtradoActual, Integer totalActual, CrearExcel excel, RequestRanking request, String fecha) {

        String nomHoja = "Ranking " + request.getAnio();
        String periodo = fecha + " " +  request.getAnio();
        XSSFSheet hoja = excel.CrearHoja(nomHoja);
        var acumulados = this.service.getPortadaAcumulados(filtradoActual, totalActual,totalActual);

        var portHeader = new FilaTabla("Encabezado",
                List.of("RANKING " + periodo, "MARCA", "*AGENCIAS", "VENTAS " + periodo,
                        "PROMEDIO DE VENTAS MENSUAL", "PROMEDIO DE VENTAS POR AGENCIA  MENSUAL " + periodo,
                        "PROMEDIO DE VENTAS POR AGENCIA " + periodo)
        );
        List<FilaTabla> filas = new ArrayList<>();
        filas.add(portHeader);
        var count = new AtomicInteger(1);
        var random = new Random(123456);
        filas.addAll(acumulados.stream()
                .filter(acu -> !acu.getFabricante().equalsIgnoreCase("TOTAL"))
                .map(acumulado -> {
                    var rank = count.getAndIncrement();
                    var agencia = random.nextInt(80, 150);
                    return this.getFilaTabla(acumulado, agencia, rank, request.getMes());
                }
        ).toList());
        var portPos = new Posicion(2,2);
        excel.creaTablaEstilo(hoja, filas, portPos);

    }

    // Solo funciona con valores -1, 0 y 1
    private void semaforoFlechas(XSSFSheet hoja, String rango){

        SheetConditionalFormatting sheetCF = hoja.getSheetConditionalFormatting();

        ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule(
                IconMultiStateFormatting.IconSet.GYR_3_ARROW
        );

        IconMultiStateFormatting iconFmt = rule.getMultiStateFormatting();
        iconFmt.setIconOnly(true);

        sheetCF.addConditionalFormatting(
                new CellRangeAddress[]{CellRangeAddress.valueOf(rango)},
                rule
        );
    }



    public FilaTabla getFilaTabla(Acumulado acumulado, Integer agencias, Integer ranking, Integer numMeses){
        var estilo = acumulado.getFabricante().equalsIgnoreCase("Stellantis") ? "Stellantis" : "Estandar";
        estilo = acumulado.getFabricante().equalsIgnoreCase("TOTAL") ? "TOTAL" : estilo;
        return new FilaTabla(estilo, List.of(
                ranking , acumulado.getFabricante(),
                agencias, acumulado.getVolumen(),
                Double.valueOf(acumulado.getVolumen().doubleValue() / numMeses.doubleValue()).intValue() ,
                Double.valueOf(acumulado.getVolumen().doubleValue() / numMeses.doubleValue() / agencias.doubleValue()).intValue(),
                Double.valueOf(acumulado.getVolumen().doubleValue() / agencias.doubleValue()).intValue()
        ));
    }

    public FilaTabla getFilaTabla(Acumulado acumuladoActual, Acumulado acumuladoAnterior, Integer agenciasActual,Integer agenciasAnterior, Integer rankingActual, Integer rankingAnterior,  Integer numMeses){
        var estilo = acumuladoActual.getFabricante().equalsIgnoreCase("Stellantis") ? "Stellantis" : "Estandar";
        estilo = acumuladoActual.getFabricante().equalsIgnoreCase("TOTAL") ? "TOTAL" : estilo;
        var diferenciaVolumen = acumuladoAnterior.getVolumen() - acumuladoActual.getVolumen();
        var porcentajeDiferencia = acumuladoActual.getVolumen().doubleValue()/acumuladoAnterior.getVolumen().doubleValue();
        var diferencia = evaluaNumero(diferenciaVolumen);
        return new FilaTabla(estilo, List.of(
                rankingActual , acumuladoActual.getFabricante(),
                agenciasActual, acumuladoActual.getVolumen(),
                Double.valueOf(acumuladoActual.getVolumen().doubleValue() / numMeses.doubleValue() / agenciasActual.doubleValue()).intValue(),
                Double.valueOf(acumuladoActual.getVolumen().doubleValue() / agenciasActual.doubleValue()).intValue(),
                diferencia,
                "",
                agenciasAnterior, acumuladoAnterior.getVolumen(),
                Double.valueOf(acumuladoAnterior.getVolumen().doubleValue() / numMeses.doubleValue() / agenciasAnterior.doubleValue()).intValue(),
                Double.valueOf(acumuladoAnterior.getVolumen().doubleValue() / agenciasAnterior.doubleValue()).intValue(),
                rankingAnterior,
                "",
                diferenciaVolumen,
                porcentajeDiferencia
        ));
    }



    private int evaluaNumero(int numero){
        if(numero > 0){
            return 1;
        }
        if(numero == 0){
            return 0;
        }
        return -1;
    }


    public String getNombreArchivo() {
        return nombreArchivo;
    }

}
