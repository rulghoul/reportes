package com.organizame.reportes.service;

import com.organizame.reportes.dto.DaoResumenPeriodo;
import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.dto.auxiliar.Acumulado;
import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.utils.Utilidades;
import com.organizame.reportes.utils.excel.ColorExcel;
import com.organizame.reportes.utils.excel.CrearExcel;
import com.organizame.reportes.utils.excel.EstiloCeldaExcel;
import com.organizame.reportes.utils.excel.dto.Posicion;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class ReporteRankingMarca {

    private final DateTimeFormatter fechaSmall;

    private final ModeloPeriodoService service;

    private String nombreArchivo;

    @Autowired
    public ReporteRankingMarca(ModeloPeriodoService service) {
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

        List<VhcModeloperiodoindustria> resultadoActual = service.findTotalUltimosMeses(eneroActual, mesActual);
        Integer totalActual = resultadoActual.stream()
                .mapToInt(VhcModeloperiodoindustria::getCantidad)
                .sum();
        List<VhcModeloperiodoindustria> resultadoAnterior = service.findTotalUltimosMeses(eneroAnterior, mesAnterior);
        Integer totalAnterior = resultadoAnterior.stream()
                .mapToInt(VhcModeloperiodoindustria::getCantidad)
                .sum();
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
        Set<DaoResumenPeriodo> filtradoActual = service.ResumeData(resultadoActual);
        Set<DaoResumenPeriodo> filtradoAnterior = service.ResumeData(resultadoAnterior);

        //Datos para hojas
        CrearExcel excel = new CrearExcel();

        this.modifiStyle(excel);

        String fecha = (Month.of(1).getDisplayName(TextStyle.FULL, Locale.of("es", "MX")) + " - " +
        Month.of(request.getMes()).getDisplayName(TextStyle.FULL, Locale.of("es", "MX"))).toUpperCase();

        this.crearRankingVs(filtradoActual, filtradoAnterior, totalActual, totalAnterior, excel, request, fecha);
        this.creaRanking(filtradoActual, totalActual, excel, request, fecha);


        fin = System.nanoTime();

        duracionNanos = fin - inicio;
        duracionMillis = duracionNanos / 1_000_000;

        log.info("La creacion del excel fue: {} ms", duracionMillis);
        return excel.guardaExcel();
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

    private void crearRankingVs(Set<DaoResumenPeriodo> filtradoActual, Set<DaoResumenPeriodo> filtradoAnterior, Integer totalActual, Integer totalAnterior, CrearExcel excel, RequestRanking request, String fecha) {
        String nomHoja = "Ranking " + request.getAnio() + " Vs " + (request.getAnio() - 1);
        String periodoActual = fecha + " " +  request.getAnio();
        String periodoAnterior = fecha + " " +  ( request.getAnio() -1 );

        var hoja =  excel.CrearHoja(nomHoja);

        var acumuladosActual = this.service.getPortadaAcumulados(filtradoActual, totalActual,totalActual);
        var acumuladosAnterior = this.service.getPortadaAcumulados(filtradoAnterior, totalAnterior,totalAnterior);

        List<Object> encabezado = List.of("RANKING " + periodoActual, "MARCA",
                "*AGENCIAS", "VENTAS " + periodoActual,
                "PROMEDIO DE VENTAS POR AGENCIA  MENSUAL " + periodoActual,
                "PROMEDIO DE VENTAS POR AGENCIA " + periodoActual,"","",
                "*AGENCIAS", "VENTAS " + periodoAnterior,
                "PROMEDIO DE VENTAS POR AGENCIA  MENSUAL " + periodoAnterior,
                "PROMEDIO DE VENTAS POR AGENCIA " + periodoAnterior,
                "RANKING " + periodoAnterior, "");

        var uno = new ArrayList<>(encabezado);
        uno.add("DIFERENCIAS " + request.getAnio() + " Vs " + (request.getAnio() - 1));
        uno.add("");
        var dos = new ArrayList<>(encabezado);
        dos.add("UNIDADES");
        dos.add("%");


        List<FilaTabla> filas = new ArrayList<>();
        filas.add(new FilaTabla("Encabezado", uno));
        filas.add(new FilaTabla("Encabezado", dos));
        var count = new AtomicInteger(1);
        var random = new Random(123456);
        filas.addAll(acumuladosActual.stream()
                .filter(acu -> !acu.getFabricante().equalsIgnoreCase("TOTAL"))
                .map(acumulado -> {
                    var rankActual = count.getAndIncrement();
                    var agenciaActual = this.getAgenciasPeriodo(acumulado,
                            LocalDate.of(request.getAnio(), 1,1),
                            LocalDate.of(request.getAnio(), 1,request.getMes()));
                    var acumuladoAnterior = acumuladosAnterior.stream()
                            .filter(acu -> acu.getFabricante().equalsIgnoreCase(acumulado.getFabricante()))
                            .findFirst();
                    Integer rankAnterior = acumuladoAnterior.isPresent() ? acumuladosAnterior.indexOf(acumuladoAnterior.get()) +1 : 99;
                    var agenciaAnterior = this.getAgenciasPeriodo(acumulado,
                            LocalDate.of(request.getAnio()-1, 1,1),
                            LocalDate.of(request.getAnio()-1, 1,request.getMes()));
                    var anterior = acumuladoAnterior.isPresent() ? acumuladoAnterior.get() : new Acumulado(null,"",0,0,0d,0d);
                    return this.getFilaTabla(acumulado, anterior,
                            agenciaActual, agenciaAnterior,
                            rankActual, rankAnterior,
                            request.getMes());
                }
        ).toList());

        var portPos = new Posicion(0,0);
        excel.creaTexto(hoja, "RANKING DE VENTAS POR AGENCIA DE MARCA", portPos, 6);

        portPos.setRow(1);
        excel.creaTablaEstilo(hoja, filas, portPos);


        //Cambiar color de algunos encabezados
        //estilo negro
        List<Integer> separadores = Arrays.asList(portPos.getCol()+7, portPos.getCol()+13);
        this.changeStyleHeader(excel, hoja, portPos.getRow(), separadores, "black");
        List<Integer> grises = Arrays.asList(portPos.getCol(), portPos.getCol()+4, portPos.getCol()+5, portPos.getCol()+9, portPos.getCol()+10, portPos.getCol()+11);
        this.changeStyleHeader(excel, hoja, portPos.getRow(), grises, "greyHeader");
        List<Integer> diferencias = Arrays.asList(portPos.getCol()+14, portPos.getCol()+15);
        this.changeStyleHeader(excel, hoja, portPos.getRow(), diferencias, "tile");
        this.changeStyleHeader(excel, hoja, portPos.getRow()+1, diferencias, "tile");


        //Congelar filas y columnas
        hoja.createFreezePane(portPos.getCol()+2, portPos.getRow() +2);

        //modifica la fila de encabezados fucionando
        for(var i = 0; i < 14; i++){
            if(i == 7 || i == 13){
                hoja.addMergedRegion(new CellRangeAddress(portPos.getRow(), portPos.getRow() + acumuladosActual.size(),
                        portPos.getCol()+i, portPos.getCol()+i));
            }else{
                hoja.addMergedRegion(new CellRangeAddress(portPos.getRow(), portPos.getRow()+1,
                        portPos.getCol()+i, portPos.getCol()+i));
            }
        }


        //Fucionar encabezado de diferencia
        hoja.addMergedRegion(new CellRangeAddress(portPos.getRow(), portPos.getRow(),
                portPos.getCol()+14, portPos.getCol()+15));

        //Ajustar anchos
        var anchos = List.of(25,60,20,30,40,30,5,1,20,30,40,30,25,1,15,20);
        for(var i = 0; anchos.size() > i; i++ ){
            hoja.setColumnWidth(portPos.getCol()+i, anchos.get(i)*150);
        }

        hoja.setColumnWidth(0, 1);
        hoja.setColumnWidth(1, 1);




        // Se colocan las flechas de estado
        this.semaforoFlechas(hoja, "G4:G50");
        portPos.addRows(acumuladosActual.size() +2);
        this.creaNotaSemaforo(hoja, portPos, request.getAnio());

    }


    private Integer getAgenciasPeriodo(Acumulado acumulado, LocalDate inicio, LocalDate fin){
        var resultado = this.service.findAgenciasMarca(inicio, fin, acumulado.getMarca());
        return resultado.stream().mapToInt(res -> res.getCantidad())
                .sum();
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
        var portPos = new Posicion(0,0);
        excel.creaTablaEstilo(hoja, filas, portPos);

        var anchos = List.of(25,60,20,30,40,30,30);
        for(var i = 0; anchos.size() > i; i++ ){
            hoja.setColumnWidth(portPos.getCol()+i, anchos.get(i)*150);
        }

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
        var porcentajeDiferencia = acumuladoActual.getVolumen().doubleValue()/acumuladoAnterior.getVolumen().doubleValue() * Utilidades.evaluaNumero(diferenciaVolumen);
        var diferencia = Utilidades.evaluaNumero(diferenciaVolumen);
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


    private void creaNotaSemaforo(XSSFSheet hoja, Posicion portPos, Integer anio){
        //Tabla de semaforo

        var row = hoja.createRow(portPos.getRow());
        var cel1 =  row.createCell(portPos.getCol());
        cel1.setCellValue(-1);
        var cel2 =  row.createCell(portPos.getCol()+1);
        cel2.setCellValue("Bajo Volumen de ventas en " + anio + " en comparacion con el año anterior");
        row = hoja.createRow(portPos.getRow()+1);
        cel1 = row.createCell(portPos.getCol());
        cel1.setCellValue(0);
        cel2 =  row.createCell(portPos.getCol()+1);
        cel2.setCellValue("Se mantiene Volumen de ventas en " + anio + " en comparacion con el año anterior");
        row = hoja.createRow(portPos.getRow()+2);
        cel1 = row.createCell(portPos.getCol());
        cel1.setCellValue(1);
        cel2 =  row.createCell(portPos.getCol()+1);
        cel2.setCellValue("Sube Volumen de ventas en " + anio + " en comparacion con el año anterior");

        row =  hoja.createRow(portPos.getRow()+3);
        cel1 = row.createCell(portPos.getCol());
        cel1.setCellValue("Fuente");
        cel2 = row.createCell(portPos.getCol()+1);
        cel2.setCellValue("AMDA: agencias de " + anio);

        this.semaforoFlechas(hoja, "A"+ (portPos.getRow()+1) +":A" + (portPos.getRow() +3));
    }

    private void changeStyleHeader(CrearExcel excel,XSSFSheet hoja, Integer row, List<Integer> colums, String color){
        //Cambiar color de algunos encabezados
        var fila = hoja.getRow(row);

        var estilo = this.recuperaEstilo(excel, color);

        colums.forEach(colum -> {
            var celda = fila.getCell(colum);
            celda.setCellStyle(estilo);
        });
    }

    private XSSFCellStyle recuperaEstilo(CrearExcel excel , String color){
        return excel.getEstilos().stream()
                .filter(es -> es.getNombre().equalsIgnoreCase(color))
                .map(EstiloCeldaExcel::getNormal)
                .findFirst().orElse(excel.getEncabezado());
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

}
