package com.organizame.reportes.service;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.DaoResumenPeriodo;
import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.dto.auxiliar.Acumulado;
import com.organizame.reportes.dto.request.RequestOrigen;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.utils.excel.dto.PosicionGrafica;
import com.organizame.reportes.utils.graficas.graficas2;
import com.organizame.reportes.utils.presentacion.CrearPresentacion;
import com.organizame.reportes.utils.presentacion.TipoDiapositiva;
import com.organizame.reportes.utils.presentacion.dto.ColorPresentacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ReportePresentacionService {

    private final DecimalFormat formatoDecimal;

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
        this.fechaSmall = DateTimeFormatter.ofPattern("MMM yyyy");
        this.formatoDecimal = new DecimalFormat("#.#");
    }

    public Resource cargarImagen(String rutaPlantilla) throws IOException {
        return this.resourceLoader.getResource("classpath:" + rutaPlantilla);
    }


    public ByteArrayInputStream CrearPresentacionOrigen(RequestOrigen request) throws IOException {
        // Carga fondos de para diapositiva de portada y diapositiva de contenido
        Resource bg_contenido = this.cargarImagen("static/fondo_contenido.png");
        Resource bg_portada =this.cargarImagen("static/fondo_portada.png");
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
        CrearPresentacion presentacion = new CrearPresentacion(bg_contenido,  bg_portada, this.creaColoresBase());
        LocalDate fechaInicial = request.getMesFinal().minusMonths(request.getMesReporte());
        String fecha = fechaSmall.format(fechaInicial) + " a " + fechaSmall.format(request.getMesFinal());

        this.crearPortada(filtrado, presentacion, request, fecha);
        this.creaVolumenPorMarca(filtrado, presentacion, request, fecha);
        //this.crearHojasPorSegmento(filtrado, presentacion, request, fecha);
        //this.crearTopLineas(filtrado, presentacion, fecha);
        //this.creaHojasporMarca(filtrado, presentacion, request, fecha);


        return presentacion.guardaPresentacion();
    }

    private void crearPortada(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, RequestOrigen request, String fecha){
        var totalIndustria = service.getTotalIntustria();
        var totalOrigen = service.getTotalOrigen(request);

        var portadaAcumulados = service.getPortadaAcumulados(filtrado, totalIndustria.orElse(1), totalOrigen.orElse(1));
        var acumuladoCorregido = portadaAcumulados.stream()
                .map(acumulado -> {
                    return new Acumulado(
                            acumulado.getFabricante(),
                            acumulado.getLineas(),
                            acumulado.getVolumen(),
                            acumulado.getPeso() * 100,
                            acumulado.getPorcentajeIndustria() * 100
                    );
                })
                .toList();
        var portadaTotales = service.getPortadaTotales(filtrado);
        // Genera portada

        var portada = presentacion.crearDiapositiva(TipoDiapositiva.PORTADA);

        presentacion.creaTexto(portada, "ANÁLISIS VENTAS DE VEHÍCULOS DE ORIGEN "
                        + request.getOrigen().toUpperCase(),
                new PosicionGrafica(80,52,40,20), "Portada");


        var portada2 = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);

        var portPos = new PosicionGrafica(2,2, 64, 35);
        presentacion.creaTexto(portada2, """
                        En la industria mexicana, 6 marcas
                        comercializan {{MODELOS}} vehículos de
                        origen {{ORIGEN}}. Durante el periodo {{PERIODO}},
                        se vendieron {{UNIDADES}} unidades, lo que
                        representó una participación de
                        mercado del {{PARTICIPACION}}% del total de la industria
                        """.replace("{{MODELOS}}", acumuladoCorregido.getLast().getLineas().toString())
                        .replace("{{ORIGEN}}", request.getOrigen().toUpperCase())
                        .replace("{{PERIODO}}", fecha)
                        .replace("{{UNIDADES}}", acumuladoCorregido.getLast().getVolumen().toString())
                        .replace("{{PARTICIPACION}}", formatoDecimal.format(acumuladoCorregido.getLast().getPorcentajeIndustria()))
                , portPos, "normal");

        portPos.setRow(38);

        var stellantis = acumuladoCorregido.stream()
                        .filter(acu -> acu.getFabricante().equalsIgnoreCase("STELLANTIS"))
                                .findFirst();

        if(stellantis.isPresent()) {
            var posicion = acumuladoCorregido.indexOf(stellantis);
            var posicionString = convertirNumeroAOrdinal(posicion);
            presentacion.creaTexto(portada2, """
                            En el periodo {{PERIODO}}, Stellantis se
                            posicionó en {{POSICION}} posición con una participación
                            del {{PARTICIPACION}} de las ventas totales que corresponden a los
                            vehículos importados desde {{ORIGEN}}, esto represento la
                            comercialización de {{UNIDADES}} unidades.
                            """.replace("{{ORIGEN}}", request.getOrigen().toUpperCase())
                            .replace("{{PERIODO}}", fecha)
                            .replace("{{POSICION}}", posicionString)
                            .replace("{{UNIDADES}}", stellantis.get().getVolumen().toString())
                            .replace("{{PARTICIPACION}}", formatoDecimal.format(stellantis.get().getPorcentajeIndustria()))
                    , portPos, "normal");
        }

        var portHeader = new FilaTabla("Encabezado", List.of("Marcas", "Número de lineas", "Volumen", "Peso", "% MS Industria total"));
        List<FilaTabla> acumuladosTabla = new ArrayList<>();
        acumuladosTabla.add(portHeader);
        acumuladosTabla.addAll(acumuladoCorregido.stream().map(acumulado -> acumulado.getFilaTabla(this.formatoDecimal)).toList());
        //Se imprime la tabla de acumulados
        portPos.setCol(66);
        portPos.setRow(20);
        portPos.setAlto(70);
        presentacion.creaTablaEstilo(portada2, acumuladosTabla, portPos, List.of(24,10, 10,10,10));

        try {
            var grafica = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
            var tituloGrafica = "Ventas por Origen Brasil, Industria y Market Share";

            presentacion.insertarGrafica(grafica, graficas.createComboChart(tituloGrafica, portadaTotales, request.getOrigen()),
                    new PosicionGrafica(2, 2, 124, 70),
                    new PosicionGrafica(66, 2, 1280, 740));
        }catch (GraficaException e){
            log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
        }

    }

    private void creaVolumenPorMarca(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, RequestOrigen request, String fecha){
        var contraPortada = service.getVolumenMarca(filtrado);
        var grafica = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
        // Volumen por Marca
        try{
            presentacion.insertarGrafica(grafica, graficas.LineChartFabricantes(contraPortada),
                    new PosicionGrafica(2, 2, 84, 49),
                    new PosicionGrafica(66, 2, 1280, 740));
        }catch (GraficaException e){
            log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
        }


        presentacion.creaTexto(grafica,"""
        Estas 5 marcas representaron el 94.0% de la venta
        total correspondiente a los vehículos provenientes de
        {{ORIGEN}} durante el periodo {{PERIODO}}
                """.replace("{{ORIGEN}}", request.getOrigen().toUpperCase())
                        .replace("{{PERIODO}}", fecha)
                , new PosicionGrafica(2, 53, 84, 19), "normal");

        //Aqui se generaran los logos de las marcas y su numero de modelos
        var logosPos =new PosicionGrafica(88, 2, 20, 8);
        var nombrePos =new PosicionGrafica(108, 2, 20, 8);
        contraPortada
                .stream()
                .filter(fila -> !fila.getFila().getFirst().toString().equalsIgnoreCase("Fabricante"))
                .forEach(fabricante -> {
                    try {
                        var imagenResorce = this.cargarImagen("static/images/marcas/logo.png");
                        presentacion.insertarImagen(grafica, logosPos, imagenResorce.getContentAsByteArray());
                    }catch (Exception e){
                        log.warn("No se puedo cargar la imagen para la marca{}", fabricante.getFila().getFirst().toString());
                    }

                    presentacion.creaTexto(grafica, fabricante.getFila().getFirst().toString(),
                            nombrePos, "Normal");
                    nombrePos.addRows(10);
                    logosPos.addRows(10);
                });

    }

    private void crearTopLineas(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, String fecha){
        List<DaoPeriodo> resumenDatos = service.generarResumen(filtrado, DaoResumenPeriodo::getModelo, s -> s.equalsIgnoreCase("Stellantis"));

        var top = service.getVolumenTop(filtrado);

        var hoja = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
        //Tabla principal
        presentacion.creaTablaEstilo(hoja, top, new PosicionGrafica(0,0, 50, 50), List.of(24,10, 10,10,10));

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
        presentacion.creaTablaEstilo(hoja, resumen, posGrafica, List.of(24,10, 10,10,10));
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
            presentacion.creaTablaEstilo(hoja, segmento.getDatos(), new PosicionGrafica(0,0, 100, 50), List.of(24,10, 10,10,10));
            var resumen = this.creaResumen(segmentoResumen.get(segmento.getNombreTabla()), fecha);

            var posGrafica = new PosicionGrafica(50,50,1200, 800);
            //Tabla resumen
            presentacion.creaTablaEstilo(hoja, resumen, new PosicionGrafica(0,0, 100, 50), List.of(24,10, 10,10,10));
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
            presentacion.creaTablaEstilo(hoja, fabricante.getDatos(), new PosicionGrafica(0,0, 50, 100), List.of(24,10, 10,10,10));
            var resumen = this.creaResumen(fabricanteResumen.get(fabricante.getNombreTabla()), fecha);

            var posGrafica = new PosicionGrafica(200, 200,1200, 800);
            //Tabla resumen
            presentacion.creaTablaEstilo(hoja, resumen, posGrafica, List.of(24,10, 10,10,10));
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

    private String convertirNumeroAOrdinal(int numero) {
        return switch (numero) {
            case 1 -> "PRIMERO";
            case 2 -> "SEGUNDO";
            case 3 -> "TERCERO";
            case 4 -> "CUARTO";
            case 5 -> "QUINTO";
            case 6 -> "SEXTO";
            case 7 -> "SÉPTIMO";
            case 8 -> "OCTAVO";
            case 9 -> "NOVENO";
            case 10 -> "DÉCIMO";
            default -> String.valueOf(numero); // Si es mayor a 10, devuelve el número como cadena
        };
    }

    private List<ColorPresentacion> creaColoresBase(){
        List<ColorPresentacion> colores = new ArrayList<>();
        colores.add(new ColorPresentacion("Portada", "FFFFFF", "FAFAFA", 18, true));
        colores.add(new ColorPresentacion("Normal", "FFFFFF", "002B7F", 16, true));
        colores.add(new ColorPresentacion("Titulo", "FFFFFF", "000000", 18, true));
        colores.add(new ColorPresentacion("Destacado", "FFFFFF", "FF0000", 18, true));
        colores.add(new ColorPresentacion("Normal2", "FFFFFF", "373F66", 18, true));
        colores.add(new ColorPresentacion("Numeracion", "F6C6CE", "9B182E", 18, true));
        // Colores de tablas
        colores.add(new ColorPresentacion("Encabezado", "002B7F", "FAFAFA", 12, true));
        colores.add(new ColorPresentacion("Estandar", "FFFFFF", "000000", 11, false));
        colores.add(new ColorPresentacion("Stellantis", "96938E", "FAFAFA", 11, false));
        colores.add(new ColorPresentacion("RowOdd", "FAFAFA", "000000", 11, false));
        colores.add(new ColorPresentacion("TOTAL", "000000", "FAFAFA", 11, true));
        return colores;
    }
}
