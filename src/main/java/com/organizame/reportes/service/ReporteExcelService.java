package com.organizame.reportes.service;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.DaoResumenPeriodo;
import com.organizame.reportes.dto.FilaTabla;
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

    private final ModeloPeriodoService service;

    private final Graficas graficas;

    @Autowired
    public ReporteExcelService(ModeloPeriodoService service, Graficas graficas){
        this.service = service;
        this.graficas = graficas;
        this.fechaSmall = DateTimeFormatter.ofPattern("MMMuu");
    }

    public ByteArrayInputStream CrearExcelOrigen(RequestOrigen request) throws IOException {
        List<VhcModeloperiodoindustria> resultado = service.recuperaOrigenFechaInicial(request.getOrigen(), request.getMesReporte(), request.getMesFinal());
        Set<DaoResumenPeriodo> filtrado = service.ResumeData(resultado);
        var fabricantes = service.generaDatosContenidoPorFabricante(filtrado);
        var fabricanteResumen = service.generaResumenFabricante(filtrado);
        var segmentos = service.generaDatosContenidoPorSegmento(filtrado);
        var segmentoResumen = service.generaResumenSegmento(filtrado);

        CrearExcel excel = new CrearExcel();
        LocalDate fechaInicial = request.getMesFinal().minusMonths(request.getMesReporte());
        String fecha = fechaSmall.format(fechaInicial) + "-" + fechaSmall.format(request.getMesFinal());
        // Genera portada

        // Genera hojas segmento

        segmentos.forEach(segmento -> {
            var hoja = excel.CrearHoja(segmento.getNombreTabla());
            //Tabla principal
            var posicion = excel.creaTablaEstilo(hoja, segmento.getDatos(), 0, 0);
            var resumen = this.creaResumen(segmentoResumen.get(segmento.getNombreTabla()), fecha);
            posicion.setRow(posicion.getRow()+2);
            var posGrafica = new PosicionGrafica(posicion,1200, 800);
            //Tabla resumen
            posicion = excel.creaTablaEstilo(hoja, resumen, 0, posicion.getRow());
            var datosGrafica = graficas.generaDataset(segmentoResumen.get(segmento.getNombreTabla()));
            var grafica =graficas.graficaBarras("Segmento de " + segmento.getNombreTabla() + " - Origen " + request.getOrigen() + "fechas",
                    "Modelos" , "Participacion", datosGrafica);
            posGrafica.setCol(posicion.getCol() + 2);
            excel.InsertarGrafica(hoja, grafica, posGrafica);
        });


        // Genera hojas fabricantes

        fabricantes.forEach(fabricante -> {
            var hoja = excel.CrearHoja(fabricante.getNombreTabla());
            //Tabla princial
            var posicion = excel.creaTablaEstilo(hoja, fabricante.getDatos(), 0, 0);
            var resumen = this.creaResumen(fabricanteResumen.get(fabricante.getNombreTabla()), fecha);
            posicion.setRow(posicion.getRow()+2);
            var posGrafica = new PosicionGrafica(posicion,1200, 800);
            //Tabla resumen
            posicion = excel.creaTablaEstilo(hoja, resumen, 0, posicion.getRow());
            var datosGrafica = graficas.generaDataset(fabricanteResumen.get(fabricante.getNombreTabla()));
            var grafica = graficas.graficaBarras("Volumen de ventas, origen " + request.getOrigen() + "fechas",
                    "Modelos" , "Participacion", datosGrafica);
            posGrafica.setCol(posicion.getCol() + 2);
            excel.InsertarGrafica(hoja, grafica, posGrafica);
        });

        return excel.guardaExcel();
    }

    private List<FilaTabla> creaResumen(List<DaoPeriodo> resumen, String fecha){
        List<FilaTabla> resultado = new ArrayList<>();
        resultado.add(new FilaTabla("Estandar",List.of("Modelo", fecha, "Part.")));
        resultado.addAll(
                resumen.stream().map(DaoPeriodo::toFilas)
                        .toList()
        );
        return resultado;
    }
}
