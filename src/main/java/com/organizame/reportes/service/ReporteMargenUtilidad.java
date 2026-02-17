package com.organizame.reportes.service;

import com.organizame.reportes.dto.request.RequestMargen;
import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.repository.service.MargenUtilidadService;
import com.organizame.reportes.utils.excel.ColorExcel;
import com.organizame.reportes.utils.excel.CrearExcel;
import com.organizame.reportes.utils.excel.EstiloCeldaExcel;
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

@Slf4j
@Service
public class ReporteMargenUtilidad {

    private String nombreArchivo;

    private final MargenUtilidadService margenUtilidadService;

    @Autowired
    public ReporteMargenUtilidad(MargenUtilidadService margenUtilidadService){
        this.margenUtilidadService = margenUtilidadService;
    }

    public ByteArrayInputStream CrearExcelRanking(RequestMargen request) throws IOException {
        //datos brutos todos los origenes por
        var margenes = margenUtilidadService.getMargenes(request.getInicio(), request.getFin());
        if(margenes.isEmpty()){
            throw new IOException("No se encontraron datos para las fechas solicitadas");
        }
        nombreArchivo = "margenUtilidad.xlsx";
        var excel = new CrearExcel();
        this.modifiStyle(excel);

        excel.creaFila()

        return excel.guardaExcel();
    }



    private void modifiStyle(CrearExcel excel) {


        var porcentajeColor = new ColorExcel("porcentaje", "#FFFFFF", "#FFFFFF");
        var porcentaje = new EstiloCeldaExcel(porcentajeColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.THIN, Optional.empty(),"0.0%",false,Optional.empty(), Optional.of("#FF0000"), false);
        excel.getEstilos().add(porcentaje);

        var monedaColor = new ColorExcel("moneda", "#FFFFFF", "#FFFFFF");
        var moneda = new EstiloCeldaExcel(monedaColor,excel.getWb(),10
                , Optional.of(HorizontalAlignment.RIGHT),Optional.of(VerticalAlignment.BOTTOM),
                Optional.empty(),BorderStyle.THIN, Optional.empty(),"$0.0",false,Optional.empty(), Optional.of("#FF0000"), false);
        excel.getEstilos().add(moneda);

    }

    private final List<String> ENCABEZADOS = Arrays.asList(
            "Nombre Version (MY)",
            "PRECIO DIST",
            "Precio Base",
            "CUOTA SPDC",
            "Márgen Base %",
            "Precio de Lista - PRECIO DE LISTA",
            "Precio de Lista - UTILIDAD SIN IMPUESTO $ ",
            "Precio de Lista - UTILIDAD SIN IMPUESTO % ",
            "Precio Credito - Precio Crédito",
            "Precio Credito - UTILIDAD SIN IMPUESTO $ ",
            "Precio Credito - UTILIDAD SIN IMPUESTO % ",
            "Precio Credito - Reembolso ",
            "Precio Contado - Precio Contado",
            "Precio Contado - UTILIDAD SIN IMPUESTO $ ",
            "Precio Contado - UTILIDAD SIN IMPUESTO % ",
            "Precio Contado - Reembolso ",
            "E1",
            "C1",
            "E2",
            "Precio de Listsa - PRECIO DE LISTA",
            "Precio de Listsa - Desglose de IVA",
            "Precio de Listsa - IVA",
            "Precio de Listsa - ISAN",
            "Precio de Listsa - Gastos",
            "Precio de Listsa - Precio Base",
            "Precio de Listsa - Márgen Base $",
            "Precio de Listsa - Márgen Base %",
            "Precio de Listsa - PRECIO DE LISTA",
            "Precio de Listsa - UTILIDAD SIN IMPUESTO $ ",
            "Precio de Listsa - UTILIDAD SIN IMPUESTO % ",
            "C3",
            "C4",
            "Precio Crédito - Precio Crédito",
            "Precio Crédito - Desglose de IVA",
            "Precio Crédito - IVA",
            "Precio Crédito - ISAN",
            "Precio Crédito - Gastos",
            "Precio Crédito - Precio Base",
            "Precio Crédito - PRECIO DIST",
            "Precio Crédito - CUOTA SPDC",
            "Precio Crédito - Costo de la unidad sin publicidad",
            "Precio Crédito - Menos devolución al Dist con IVA",
            "Precio Crédito - Menos devolución al Dist sin IVA",
            "Precio Crédito - Costo Neto a Dist. Unidad",
            "Precio Crédito - Precio Base",
            "Precio Crédito - Costo Neto a Dist. Unidad",
            "Precio Crédito - Márgen Base $",
            "Precio Crédito - Márgen Base %",
            "Precio Crédito - PRECIO Crédito",
            "Precio Crédito - UTILIDAD SIN IMPUESTO $ ",
            "Precio Crédito - UTILIDAD SIN IMPUESTO % ",
            "Precio Crédito - PARTICIPACION CON IVA stellantis",
            "Precio Crédito - PARTICIPACION CON IVA PART DIST.",
            "Diferencia en %",
            "Diferencia en $",
            "C6",
            "C7",
            "cuota de traslado",
            "D1",
            "D2",
            "C8",
            "C9",
            "E3",
            " D3 ",
            "C9",
            "E4",
            "C10",
            "C11",
            "C12",
            "E5",
            "C13",
            "C14",
            "C15",
            "E6",
            "Descuento de Contado",
            "reem bono Contado",
            "Precio Contado - PRECIO Contado",
            "Precio Contado - C16",
            "Precio Contado - IVA",
            "Precio Contado - ISAN",
            "Precio Contado - Gastos",
            "Precio Contado - Precio Base",
            "Precio Contado - PRECIO DIST",
            "Precio Contado - CUOTA SPDC",
            "Precio Contado - Costo de la unidad sin publicidad",
            "Precio Contado - Menos devolución al Dist con IVA",
            "Precio Contado - Menos devolución al Dist sin IVA",
            "Precio Contado - Costo Neto a Dist. Unidad",
            "Precio Contado - Precio Base",
            "Precio Contado - Costo Neto a Dist. Unidad",
            "Precio Contado - Márgen Base $",
            "Precio Contado - Márgen Base %",
            "Precio Contado - PRECIO Contado ",
            "Precio Contado - UTILIDAD SIN IMPUESTO $ ",
            "Precio Contado - UTILIDAD SIN IMPUESTO % ",
            "Precio Contado - PARTICIPACION CON IVA Stellantis",
            "Precio Contado - PARTICIPACION CON IVA PART DIST."
    );

    public String getNombreArchivo() {
        return nombreArchivo;
    }
}
