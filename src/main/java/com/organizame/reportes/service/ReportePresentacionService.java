package com.organizame.reportes.service;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.DaoResumenPeriodo;
import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.dto.auxiliar.Acumulado;
import com.organizame.reportes.dto.auxiliar.PortadaTotales;
import com.organizame.reportes.dto.request.RequestOrigen;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.utils.excel.dto.PosicionGrafica;
import com.organizame.reportes.utils.graficas.graficas2;
import com.organizame.reportes.utils.presentacion.CrearPresentacion;
import com.organizame.reportes.utils.presentacion.TipoDiapositiva;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class ReportePresentacionService {

    private  final DateTimeFormatter fechaSmall;

    private  final ModeloPeriodoService service;

    private  final ResourceLoader resourceLoader;

    private  final graficas2 graficas;

    private String nombreArchivo;

    @Autowired
    public ReportePresentacionService(ModeloPeriodoService service, graficas2 graficas, ResourceLoader resourceLoader){
        this.service = service;
        this.graficas = graficas;
        this.resourceLoader = resourceLoader;
        this.fechaSmall = DateTimeFormatter.ofPattern("MMMuu");
    }

    public XMLSlideShow cargarPlantilla(String rutaPlantilla) throws IOException {
        Resource resource = this.resourceLoader.getResource("classpath:" + rutaPlantilla);
        return new XMLSlideShow(resource.getInputStream());
    }

    public ByteArrayInputStream CrearPresentacionOrigen(RequestOrigen request) throws IOException {
        var plantilla =this.cargarPlantilla("plantilla.pptx");
        return this.CrearPresentacionOrigen(request, plantilla);
    }

    public ByteArrayInputStream CrearPresentacionOrigen(RequestOrigen request, XMLSlideShow plantilla) throws IOException {
        //datos brutos
        List<VhcModeloperiodoindustria> resultado = service.recuperaOrigenFechaInicial(request.getOrigen(), request.getMesReporte(), request.getMesFinal());
        if(resultado.isEmpty()){
            throw new SinDatos("No se encontraron datos para el origen " +
                    request.getOrigen() + " en " + request.getMesReporte() + " meses antes de " + request.getMesFinal() );
        }
        this.nombreArchivo = "Ventas origen_" + request.getOrigen() + " " + request.getMesFinal().format(DateTimeFormatter.ofPattern("LLLL yyyy"));
        //Datos resumidos
        Set<DaoResumenPeriodo> filtrado = service.ResumeData(resultado);

        //Datos para hojas
        CrearPresentacion presentacion = new CrearPresentacion(plantilla);
        LocalDate fechaInicial = request.getMesFinal().minusMonths(request.getMesReporte());
        String fecha = fechaSmall.format(fechaInicial) + "-" + fechaSmall.format(request.getMesFinal());

        this.crearPortada(filtrado, presentacion, request, fecha);
        this.creaVolumenPorMarca(filtrado, presentacion);
        this.crearHojasPorSegmento(filtrado, presentacion, request, fecha);
        this.crearTopLineas(filtrado, presentacion, fecha);
        this.creaHojasporMarca(filtrado, presentacion, request, fecha);


        return presentacion.guardaPresentacion();
    }

    private void crearPortada(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, RequestOrigen request, String fecha){
        var totalIndustria = service.getTotalIntustria();
        var totalOrigen = service.getTotalOrigen(request);

        var portadaAcumulados = service.getPortadaAcumulados(filtrado, totalIndustria.orElse(0), totalOrigen.orElse(0));
        var portadaTotales = service.getPortadaTotales(filtrado);
        // Genera portada

        var portada = presentacion.crearDiapositiva(TipoDiapositiva.PORTADA);
        Optional<XSLFTextShape> texto = portada.getShapes().stream()
                .filter(shape -> shape instanceof XSLFTextShape)
                .map(shape -> (XSLFTextShape) shape ).findFirst();
        if(texto.isPresent()) {
            var completo = texto.get().getText().replace("{{ORIGEN}}", request.getOrigen().toUpperCase());
            texto.get().clearText();
            texto.get().setText(completo);
        }


        var portada2 = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);



        var portPos = new PosicionGrafica(2,2, 800, 600);
        presentacion.creaTexto(portada, "Acumulado " + fecha, portPos, "#234325");
        portPos.setCol(2);
        portPos.addRows(-1);

        var portHeader = new FilaTabla("Encabezado", List.of("Marcas", "Número de lineas", "Volumen", "Peso", "% MS Industria total"));
        List<FilaTabla> acumuladosTabla = new ArrayList<>();
        acumuladosTabla.add(portHeader);
        acumuladosTabla.addAll(portadaAcumulados.stream().map(Acumulado::getFilaTabla).toList());
        //Se imprime la tabla de acumulados
        presentacion.creaTablaEstilo(portada, acumuladosTabla, portPos);
        portPos.setCol(2);
        portPos.addRows(2);

        presentacion.creaTexto(portada, "Total Industria " + fecha, portPos, "#232323");
        portPos.addRows(1);

        portPos.setCol(2);
        var portResHeader = new FilaTabla("Encabezado",
                List.of("Periodo", "Ventas modelos de origen " + request.getOrigen(), "Ventas totales de la Industria",
                        "% de Market share"));
        List<FilaTabla> vasmensuales = new ArrayList<>();
        vasmensuales.add(portResHeader);
        vasmensuales.addAll(portadaTotales.stream().map(PortadaTotales::getFilaTabla).toList());

        presentacion.creaTablaEstilo(portada, vasmensuales, portPos);



        portPos.setRow(2);
        portPos.setCol(8);

        var tituloGrafica = "Ventas por Origen Brasil, Industria y Market Share";
        try {
            presentacion.insertarGrafica(portada, graficas.createComboChart(tituloGrafica, portadaTotales, request.getOrigen()), new PosicionGrafica(portPos, 1600, 1000), new PosicionGrafica(portPos, 1600, 1000));
        }catch (GraficaException e){
            log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
        }

    }

    private void creaVolumenPorMarca(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion){
        var contraPortada = service.getVolumenMarca(filtrado);
        // Volumen por Marca

        var contra = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
        presentacion.creaTablaEstilo(contra, contraPortada, new PosicionGrafica(0,0,100,100));

     try{
        presentacion.insertarGrafica(contra, graficas.LineChartFabricantes(contraPortada), new PosicionGrafica(0,0, 2400, 800), new PosicionGrafica(0,0, 2400, 800));
    }catch (GraficaException e){
        log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
    }

    }

    private void crearTopLineas(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, String fecha){
        List<DaoPeriodo> resumenDatos = service.generarResumen(filtrado, DaoResumenPeriodo::getModelo, s -> s.equalsIgnoreCase("Stellantis"));

        var top = service.getVolumenTop(filtrado);

        var hoja = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
        //Tabla principal
        presentacion.creaTablaEstilo(hoja, top, new PosicionGrafica(0,0, 50, 50));

        //Recuoerar solo los 10 modelos topo
        var totalOrigen =resumenDatos.getLast();
        var cuerpo = resumenDatos.subList(0,9);
        Integer totalTop = cuerpo.stream()
                .mapToInt(dP -> dP.getTotal())
                .sum();
        var porcentajeTop = totalTop.doubleValue() / totalOrigen.getTotal().doubleValue() ;
        var topTotal = new DaoPeriodo("Total Top",totalTop,porcentajeTop, "Encabezado");

        List<DaoPeriodo> soloTop = new ArrayList<>(cuerpo);
        soloTop.add(topTotal);
        soloTop.add(totalOrigen);

        var resumen = this.creaResumen(soloTop, fecha);


        var posGrafica = new PosicionGrafica(10,10,100, 80);
        //Tabla resumen
        presentacion.creaTablaEstilo(hoja, resumen, posGrafica);
        var topGrafica = soloTop.subList(0,soloTop.size()-2);

        var grafica =graficas.createChart( topGrafica, "Stellantis");

        try {
            presentacion.insertarGrafica(hoja, grafica, posGrafica, posGrafica);
        } catch (GraficaException e) {
            log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
        }
    }

    private void crearHojasPorSegmento(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, RequestOrigen request, String fecha){
        // Genera hojas segmento

        var segmentos = service.generaDatosContenidoPorSegmento(filtrado);
        var segmentoResumen = service.generaResumenSegmento(filtrado);

        segmentos.forEach(segmento -> {
            var hoja = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
            //Tabla principal
            presentacion.creaTablaEstilo(hoja, segmento.getDatos(), new PosicionGrafica(0,0, 100, 50));
            var resumen = this.creaResumen(segmentoResumen.get(segmento.getNombreTabla()), fecha);

            var posGrafica = new PosicionGrafica(50,50,1200, 800);
            //Tabla resumen
            presentacion.creaTablaEstilo(hoja, resumen, new PosicionGrafica(0,0, 100, 50));
            var datosGrafica = graficas.generaDataset(segmentoResumen.get(segmento.getNombreTabla()));
            var grafica =graficas.graficaBarras("Segmento de " + segmento.getNombreTabla() + " - Origen " + request.getOrigen() + " fechas",
                    "Modelos" , "Participacion", datosGrafica);

            try {
                presentacion.insertarGrafica(hoja, grafica, posGrafica, posGrafica);
            } catch (GraficaException e) {
                log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
            }
        });
    }

    private void creaHojasporMarca(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, RequestOrigen request, String fecha){

        var fabricantes = service.generaDatosContenidoPorFabricante(filtrado);
        var fabricanteResumen = service.generaResumenFabricante(filtrado);

        // Genera hojas fabricantes

        fabricantes.forEach(fabricante -> {
            var hoja = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
            //Tabla princial
            presentacion.creaTablaEstilo(hoja, fabricante.getDatos(), new PosicionGrafica(0,0, 50, 100));
            var resumen = this.creaResumen(fabricanteResumen.get(fabricante.getNombreTabla()), fecha);

            var posGrafica = new PosicionGrafica(200, 200,1200, 800);
            //Tabla resumen
            presentacion.creaTablaEstilo(hoja, resumen, posGrafica);
            var datosGrafica = graficas.generaDataset(fabricanteResumen.get(fabricante.getNombreTabla()));
            var grafica = graficas.graficaBarrasColor("Volumen de ventas, origen " + request.getOrigen() + "fechas",
                    "Modelos" , "Participacion", datosGrafica);

            try {
                presentacion.insertarGrafica(hoja, grafica, posGrafica, posGrafica);
            } catch (GraficaException e) {
                log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
            }
        });

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

    public String getNombreArchivo() {
        return nombreArchivo;
    }
}
