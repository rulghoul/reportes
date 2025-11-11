package com.organizame.reportes.service;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.DaoResumenPeriodo;
import com.organizame.reportes.dto.request.RequestOrigen;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.utils.excel.CrearExcel;
import com.organizame.reportes.utils.excel.dto.PosicionGrafica;
import com.organizame.reportes.utils.graficas.Graficas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReporteExcelService {

    private final DateTimeFormatter fechaSmall;

    private ModeloPeriodoService service;

    private Graficas graficas;

    @Autowired
    public ReporteExcelService(ModeloPeriodoService service, Graficas graficas){
        this.service = service;
        this.graficas = graficas;
        this.fechaSmall = DateTimeFormatter.ofPattern("MMyy");
    }

    public ByteArrayInputStream CrearExcelOrigen(RequestOrigen request) throws IOException {
        List<VhcModeloperiodoindustria> resultado = service.recuperaOrigenFechaInicial(request.getOrigen(), request.getMesReporte(), request.getMesFinal());
        Set<DaoResumenPeriodo> filtrado = service.ResumeData(resultado);
        var fabricantes = service.generaDatosPivotPorFabricante(filtrado);
        var fabricanteResumen = service.generaResumenFabricante(filtrado);
        var segmentos = service.generaDatosPivotPorSegmento(filtrado);
        var segmentoResumen = service.generaResumenSegmento(filtrado);

        CrearExcel excel = new CrearExcel();

        LocalDate fechaInicial = request.getMesFinal().minusMonths(request.getMesReporte());
        String fecha = fechaSmall.format(fechaInicial) + "-" + fechaSmall.format(request.getMesFinal());
        // Genera portada

        // Genera hojas segmento

        segmentos.entrySet().forEach(segmento -> {
            var hoja = excel.CrearHoja(segmento.getKey());
            var posicion = excel.creaTabla(hoja, segmento.getValue(), 0, 0);
            var resumen = this.creaResumen(segmentoResumen.get(segmento.getKey()), fecha);
            posicion.setRow(posicion.getRow()+2);
            var posGrafica = new PosicionGrafica(posicion,1200, 800);
            posicion = excel.creaTabla(hoja, resumen, 0, posicion.getRow());
            var datosGrafica = graficas.generaDataset(segmentoResumen.get(segmento.getKey()));
            var grafica =graficas.graficaBarras("Segmento de " + segmento.getKey() + " - Origen " + request.getOrigen() + "fechas",
                    "Modelos" , "Participacion", datosGrafica);
            posGrafica.setCol(posicion.getCol() + 2);
            excel.InsertarGrafica(hoja, grafica, posGrafica);
        });


        // Genera hojas fabricantes

        fabricantes.entrySet().forEach(fabricante -> {
            var hoja = excel.CrearHoja(fabricante.getKey());
            var posicion = excel.creaTabla(hoja, fabricante.getValue(), 0, 0);
            var resumen = this.creaResumen(fabricanteResumen.get(fabricante.getKey()), fecha);
            posicion.setRow(posicion.getRow()+2);
            var posGrafica = new PosicionGrafica(posicion,1200, 800);
            posicion = excel.creaTabla(hoja, resumen, 0, posicion.getRow());
            var datosGrafica = graficas.generaDataset(fabricanteResumen.get(fabricante.getKey()));
            var grafica = graficas.graficaBarrasColor("Volumen de ventas, origen " + request.getOrigen() + "fechas",
                    "Modelos" , "Participacion", datosGrafica);
            posGrafica.setCol(posicion.getCol() + 2);
            excel.InsertarGrafica(hoja, grafica, posGrafica);
        });

        return excel.guardaExcel();
    }

    private List<List<Object>> creaResumen(List<DaoPeriodo> resumen, String fecha){
        List<List<Object>> resultado = new ArrayList<>();
        resultado.add(List.of("Modelo", fecha, "Part."));
        resultado.addAll(
                resumen.stream().map(r -> r.toListObject())
                        .toList()
        );
        return resultado;
    }
}
