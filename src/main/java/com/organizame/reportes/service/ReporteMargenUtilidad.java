package com.organizame.reportes.service;

import com.organizame.reportes.dto.request.RequestMargen;
import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.repository.service.MargenUtilidadService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class ReporteMargenUtilidad {

    private String nombreArchivo;

    private static final Map<String, Integer> ANCHOS_COLUMNAS;

    private final MargenUtilidadService margenUtilidadService;

    @Autowired
    public ReporteMargenUtilidad(MargenUtilidadService margenUtilidadService){
        this.margenUtilidadService = margenUtilidadService;
    }

    public ByteArrayInputStream CrearExcelRanking(RequestMargen request) throws IOException {
        //datos brutos todos los origenes por
        var margenes = margenUtilidadService.getMargenes(request.getFecha());
        if(margenes.isEmpty()){
            throw new IOException("No se encontraron datos para las fechas solicitadas");
        }
        nombreArchivo = "margenUtilidad.xlsx";
        var excel = new CrearExcel();
        this.modifiStyle(excel);

        var hoja = excel.CrearHoja("hoja1");
        excel.creaFila(hoja, new ColumnaFila(new Posicion(0,0),this.ENCABEZADOS));

        //cuerpo
        var posicion = new Posicion(0,1);

        margenes.stream().forEach(margen -> {

                    excel.creaFila(hoja, new ColumnaFila(posicion, margen.toCeldaList()));
                    posicion.setCol(0);
                    posicion.setRow(posicion.getRow()+1);
                }
        );

        AtomicInteger columna = new AtomicInteger(0);
        ANCHOS_COLUMNAS.values()
                .forEach(ancho ->
                    hoja.setColumnWidth(columna.getAndIncrement(), ancho * 256)
                );
        return excel.guardaExcel();
    }



    private void modifiStyle(CrearExcel excel) {

        var normalColor = new ColorExcel("normal", "#FFFFFF", "#FFFFFF");
        var normal = new EstiloCeldaExcel(normalColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.THIN, Optional.empty(),"#,##0",false,Optional.empty(), Optional.of("#FF0000"), false);
        excel.getEstilos().add(normal);


        var porcentajeColor = new ColorExcel("porcentaje", "#FFFFFF", "#FFFFFF");
        var porcentaje = new EstiloCeldaExcel(porcentajeColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.THIN, Optional.empty(),"0.00%",false,Optional.empty(), Optional.of("#FF0000"), false);
        excel.getEstilos().add(porcentaje);

        var monedaColor = new ColorExcel("moneda", "#FFFFFF", "#FFFFFF");
        var moneda = new EstiloCeldaExcel(monedaColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.THIN, Optional.empty(),"#,##0",false,Optional.empty(), Optional.of("#FF0000"), false);
        excel.getEstilos().add(moneda);

    }

    private final List<Celda> ENCABEZADOS = Arrays.asList(
            new Celda("Nombre Version (MY)", "Encabezado",1),
            new Celda("PRECIO DIST", "encabezado", 1),
            new Celda("Precio Base", "encabezado", 1),
            new Celda("CUOTA SPDC", "encabezado", 1),
            new Celda("Márgen Base %", "encabezado", 1),
            new Celda("Precio de Lista - PRECIO DE LISTA", "encabezado", 1),
            new Celda("Precio de Lista - UTILIDAD SIN IMPUESTO $ ", "encabezado", 1),
            new Celda("Precio de Lista - UTILIDAD SIN IMPUESTO % ", "encabezado", 1),
            new Celda("Precio Credito - Precio Crédito", "encabezado", 1),
            new Celda("Precio Credito - UTILIDAD SIN IMPUESTO $ ", "encabezado", 1),
            new Celda("Precio Credito - UTILIDAD SIN IMPUESTO % ", "encabezado", 1),
            new Celda("Precio Credito - Reembolso ", "encabezado", 1),
            new Celda("Precio Contado - Precio Contado", "encabezado", 1),
            new Celda("Precio Contado - UTILIDAD SIN IMPUESTO $ ", "encabezado", 1),
            new Celda("Precio Contado - UTILIDAD SIN IMPUESTO % ", "encabezado", 1),
            new Celda("Precio Contado - Reembolso ", "encabezado", 1),
            new Celda("E1", "encabezado", 1),
            new Celda("C1", "encabezado", 1),
            new Celda("E2", "encabezado", 1),
            new Celda("Precio de Listsa - PRECIO DE LISTA", "encabezado", 1),
            new Celda("Precio de Listsa - Desglose de IVA", "encabezado", 1),
            new Celda("Precio de Listsa - IVA", "encabezado", 1),
            new Celda("Precio de Listsa - ISAN", "encabezado", 1),
            new Celda("Precio de Listsa - Gastos", "encabezado", 1),
            new Celda("Precio de Listsa - Precio Base", "encabezado", 1),
            new Celda("Precio de Listsa - Márgen Base $", "encabezado", 1),
            new Celda("Precio de Listsa - Márgen Base %", "encabezado", 1),
            new Celda("Precio de Listsa - PRECIO DE LISTA", "encabezado", 1),
            new Celda("Precio de Listsa - UTILIDAD SIN IMPUESTO $ ", "encabezado", 1),
            new Celda("Precio de Listsa - UTILIDAD SIN IMPUESTO % ", "encabezado", 1),
            new Celda("C3", "encabezado", 1),
            new Celda("C4", "encabezado", 1),
            new Celda("Precio Crédito - Precio Crédito", "encabezado", 1),
            new Celda("Precio Crédito - Desglose de IVA", "encabezado", 1),
            new Celda("Precio Crédito - IVA", "encabezado", 1),
            new Celda("Precio Crédito - ISAN", "encabezado", 1),
            new Celda("Precio Crédito - Gastos", "encabezado", 1),
            new Celda("Precio Crédito - Precio Base", "encabezado", 1),
            new Celda("Precio Crédito - PRECIO DIST", "encabezado", 1),
            new Celda("Precio Crédito - CUOTA SPDC", "encabezado", 1),
            new Celda("Precio Crédito - Costo de la unidad sin publicidad", "encabezado", 1),
            new Celda("Precio Crédito - Menos devolución al Dist con IVA", "encabezado", 1),
            new Celda("Precio Crédito - Menos devolución al Dist sin IVA", "encabezado", 1),
            new Celda("Precio Crédito - Costo Neto a Dist. Unidad", "encabezado", 1),
            new Celda("Precio Crédito - Precio Base", "encabezado", 1),
            new Celda("Precio Crédito - Costo Neto a Dist. Unidad", "encabezado", 1),
            new Celda("Precio Crédito - Márgen Base $", "encabezado", 1),
            new Celda("Precio Crédito - Márgen Base %", "encabezado", 1),
            new Celda("Precio Crédito - PRECIO Crédito", "encabezado", 1),
            new Celda("Precio Crédito - UTILIDAD SIN IMPUESTO $ ", "encabezado", 1),
            new Celda("Precio Crédito - UTILIDAD SIN IMPUESTO % ", "encabezado", 1),
            new Celda("Precio Crédito - PARTICIPACION CON IVA stellantis", "encabezado", 1),
            new Celda("Precio Crédito - PARTICIPACION CON IVA PART DIST.", "encabezado", 1),
            new Celda("Diferencia en %", "encabezado", 1),
            new Celda("Diferencia en $", "encabezado", 1),
            new Celda("C6", "encabezado", 1),
            new Celda("C7", "encabezado", 1),
            new Celda("cuota de traslado", "encabezado", 1),
            new Celda("D1", "encabezado", 1),
            new Celda("D2", "encabezado", 1),
            new Celda("C8", "encabezado", 1),
            new Celda("C9", "encabezado", 1),
            new Celda("E3", "encabezado", 1),
            new Celda(" D3 ", "encabezado", 1),
            new Celda("C9", "encabezado", 1),
            new Celda("E4", "encabezado", 1),
            new Celda("C10", "encabezado", 1),
            new Celda("C11", "encabezado", 1),
            new Celda("C12", "encabezado", 1),
            new Celda("E5", "encabezado", 1),
            new Celda("C13", "encabezado", 1),
            new Celda("C14", "encabezado", 1),
            new Celda("C15", "encabezado", 1),
            new Celda("E6", "encabezado", 1),
            new Celda("Descuento de Contado", "encabezado", 1),
            new Celda("reem bono Contado", "encabezado", 1),
            new Celda("Precio Contado - PRECIO Contado", "encabezado", 1),
            new Celda("Precio Contado - C16", "encabezado", 1),
            new Celda("Precio Contado - IVA", "encabezado", 1),
            new Celda("Precio Contado - ISAN", "encabezado", 1),
            new Celda("Precio Contado - Gastos", "encabezado", 1),
            new Celda("Precio Contado - Precio Base", "encabezado", 1),
            new Celda("Precio Contado - PRECIO DIST", "encabezado", 1),
            new Celda("Precio Contado - CUOTA SPDC", "encabezado", 1),
            new Celda("Precio Contado - Costo de la unidad sin publicidad", "encabezado", 1),
            new Celda("Precio Contado - Menos devolución al Dist con IVA", "encabezado", 1),
            new Celda("Precio Contado - Menos devolución al Dist sin IVA", "encabezado", 1),
            new Celda("Precio Contado - Costo Neto a Dist. Unidad", "encabezado", 1),
            new Celda("Precio Contado - Precio Base", "encabezado", 1),
            new Celda("Precio Contado - Costo Neto a Dist. Unidad", "encabezado", 1),
            new Celda("Precio Contado - Márgen Base $", "encabezado", 1),
            new Celda("Precio Contado - Márgen Base %", "encabezado", 1),
            new Celda("Precio Contado - PRECIO Contado ", "encabezado", 1),
            new Celda("Precio Contado - UTILIDAD SIN IMPUESTO $ ", "encabezado", 1),
            new Celda("Precio Contado - UTILIDAD SIN IMPUESTO % ", "encabezado", 1),
            new Celda("Precio Contado - PARTICIPACION CON IVA Stellantis", "encabezado", 1),
            new Celda("Precio Contado - PARTICIPACION CON IVA PART DIST.","encabezado", 1)
    );

    static {
        ANCHOS_COLUMNAS = new LinkedHashMap<>();
        ANCHOS_COLUMNAS.put("A", 30);
        ANCHOS_COLUMNAS.put("B", 30);
        ANCHOS_COLUMNAS.put("C", 30);
        ANCHOS_COLUMNAS.put("D", 30);
        ANCHOS_COLUMNAS.put("E", 30);
        ANCHOS_COLUMNAS.put("F", 30);
        ANCHOS_COLUMNAS.put("G", 30);
        ANCHOS_COLUMNAS.put("H", 30);
        ANCHOS_COLUMNAS.put("I", 30);
        ANCHOS_COLUMNAS.put("J", 30);
        ANCHOS_COLUMNAS.put("K", 30);
        ANCHOS_COLUMNAS.put("L", 30);
        ANCHOS_COLUMNAS.put("M", 30);
        ANCHOS_COLUMNAS.put("N", 30);
        ANCHOS_COLUMNAS.put("O", 30);
        ANCHOS_COLUMNAS.put("P", 30);
        ANCHOS_COLUMNAS.put("Q", 30);
        ANCHOS_COLUMNAS.put("R", 30);
        ANCHOS_COLUMNAS.put("S", 30);
        ANCHOS_COLUMNAS.put("T", 30);
        ANCHOS_COLUMNAS.put("U", 30);
        ANCHOS_COLUMNAS.put("V", 30);
        ANCHOS_COLUMNAS.put("W", 30);
        ANCHOS_COLUMNAS.put("X", 30);
        ANCHOS_COLUMNAS.put("Y", 30);
        ANCHOS_COLUMNAS.put("Z", 30);
        ANCHOS_COLUMNAS.put("AA", 30);
        ANCHOS_COLUMNAS.put("AB", 30);
        ANCHOS_COLUMNAS.put("AC", 30);
        ANCHOS_COLUMNAS.put("AD", 30);
        ANCHOS_COLUMNAS.put("AE", 30);
        ANCHOS_COLUMNAS.put("AF", 30);
        ANCHOS_COLUMNAS.put("AG", 30);
        ANCHOS_COLUMNAS.put("AH", 30);
        ANCHOS_COLUMNAS.put("AI", 30);
        ANCHOS_COLUMNAS.put("AJ", 30);
        ANCHOS_COLUMNAS.put("AK", 30);
        ANCHOS_COLUMNAS.put("AL", 30);
        ANCHOS_COLUMNAS.put("AM", 30);
        ANCHOS_COLUMNAS.put("AN", 30);
        ANCHOS_COLUMNAS.put("AO", 30);
        ANCHOS_COLUMNAS.put("AP", 30);
        ANCHOS_COLUMNAS.put("AQ", 30);
        ANCHOS_COLUMNAS.put("AR", 30);
        ANCHOS_COLUMNAS.put("AS", 30);
        ANCHOS_COLUMNAS.put("AT", 30);
        ANCHOS_COLUMNAS.put("AU", 30);
        ANCHOS_COLUMNAS.put("AV", 30);
        ANCHOS_COLUMNAS.put("AW", 30);
        ANCHOS_COLUMNAS.put("AX", 30);
        ANCHOS_COLUMNAS.put("AY", 30);
        ANCHOS_COLUMNAS.put("AZ", 30);
        ANCHOS_COLUMNAS.put("BA", 30);
        ANCHOS_COLUMNAS.put("BB", 30);
        ANCHOS_COLUMNAS.put("BC", 30);
        ANCHOS_COLUMNAS.put("BD", 30);
        ANCHOS_COLUMNAS.put("BE", 30);
        ANCHOS_COLUMNAS.put("BF", 30);
        ANCHOS_COLUMNAS.put("BG", 30);
        ANCHOS_COLUMNAS.put("BH", 30);
        ANCHOS_COLUMNAS.put("BI", 30);
        ANCHOS_COLUMNAS.put("BJ", 30);
        ANCHOS_COLUMNAS.put("BK", 30);
        ANCHOS_COLUMNAS.put("BL", 30);
        ANCHOS_COLUMNAS.put("BM", 30);
        ANCHOS_COLUMNAS.put("BN", 30);
        ANCHOS_COLUMNAS.put("BO", 30);
        ANCHOS_COLUMNAS.put("BP", 30);
        ANCHOS_COLUMNAS.put("BQ", 30);
        ANCHOS_COLUMNAS.put("BR", 30);
        ANCHOS_COLUMNAS.put("BS", 30);
        ANCHOS_COLUMNAS.put("BT", 30);
        ANCHOS_COLUMNAS.put("BU", 30);
        ANCHOS_COLUMNAS.put("BV", 30);
        ANCHOS_COLUMNAS.put("BW", 30);
        ANCHOS_COLUMNAS.put("BX", 30);
        ANCHOS_COLUMNAS.put("BY", 30);
        ANCHOS_COLUMNAS.put("BZ", 30);
        ANCHOS_COLUMNAS.put("CA", 30);
        ANCHOS_COLUMNAS.put("CB", 30);
        ANCHOS_COLUMNAS.put("CC", 30);
        ANCHOS_COLUMNAS.put("CD", 30);
        ANCHOS_COLUMNAS.put("CE", 30);
        ANCHOS_COLUMNAS.put("CF", 30);
        ANCHOS_COLUMNAS.put("CG", 30);
        ANCHOS_COLUMNAS.put("CH", 30);
        ANCHOS_COLUMNAS.put("CI", 30);
        ANCHOS_COLUMNAS.put("CJ", 30);
        ANCHOS_COLUMNAS.put("CK", 30);
        ANCHOS_COLUMNAS.put("CL", 30);
        ANCHOS_COLUMNAS.put("CM", 30);
        ANCHOS_COLUMNAS.put("CN", 30);
        ANCHOS_COLUMNAS.put("CO", 30);
        ANCHOS_COLUMNAS.put("CP", 30);
        ANCHOS_COLUMNAS.put("CQ", 30);
        ANCHOS_COLUMNAS.put("CR", 30);
        ANCHOS_COLUMNAS.put("CS", 30);
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }
}
