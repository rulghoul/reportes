package com.organizame.reportes.service;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.DaoResumenPeriodo;
import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.dto.auxiliar.Acumulado;
import com.organizame.reportes.dto.request.RequestOrigen;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.utils.Utilidades;
import com.organizame.reportes.utils.excel.dto.PosicionGrafica;
import com.organizame.reportes.utils.graficas.Images;
import com.organizame.reportes.utils.graficas.graficas2;
import com.organizame.reportes.utils.presentacion.CrearPresentacion;
import com.organizame.reportes.utils.presentacion.TipoDiapositiva;
import com.organizame.reportes.utils.presentacion.dto.ColorPresentacion;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Service
public class ReportePresentacionService {

    private final DecimalFormat formatoDecimal;

    private  final DateTimeFormatter fechaSmall;

    private  final ModeloPeriodoService service;

    private  final ResourceLoader resourceLoader;

    private final Images images;

    private  final graficas2 graficas;

    private String nombreArchivo;
    
    private final String STELANTIS = "Stellantis";

    @Autowired
    public ReportePresentacionService(ModeloPeriodoService service, graficas2 graficas,
                                      ResourceLoader resourceLoader, Images images){
        this.service = service;
        this.graficas = graficas;
        this.resourceLoader = resourceLoader;
        this.images = images;
        this.fechaSmall = DateTimeFormatter.ofPattern("MMMM 'del' yyyy");
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
        this.crearHojasPorSegmento(filtrado, presentacion, request, fecha);
        this.crearTopLineas(filtrado, presentacion, request, fecha);
        this.creaHojasporMarca(filtrado, presentacion, request, fecha);


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
        presentacion.creaTexto(portada2, "En la industria mexicana, 6 marcas comercializan {{MODELOS}} vehículos de origen {{ORIGEN}}. Durante el periodo {{PERIODO}}, se vendieron {{UNIDADES}} unidades, lo que representó una participación de mercado del {{PARTICIPACION}}% del total de la industria"
                        .replace("{{MODELOS}}", acumuladoCorregido.getLast().getLineas().toString())
                        .replace("{{ORIGEN}}", request.getOrigen().toUpperCase())
                        .replace("{{PERIODO}}", fecha)
                        .replace("{{UNIDADES}}", acumuladoCorregido.getLast().getVolumen().toString())
                        .replace("{{PARTICIPACION}}", formatoDecimal.format(acumuladoCorregido.getLast().getPorcentajeIndustria()))
                , portPos, "normal");

        portPos.setRow(38);

        var stellantis = acumuladoCorregido.stream()
                        .filter(acu -> acu.getFabricante().equalsIgnoreCase(STELANTIS))
                                .findFirst();

        if(stellantis.isPresent()) {
            var posicion = acumuladoCorregido.indexOf(stellantis.get());
            var posicionString = Utilidades.convertirNumeroAOrdinal(posicion+1);
            presentacion.creaTexto(portada2, "En el periodo {{PERIODO}}, Stellantis se posicionó en {{POSICION}} posición con una participación del {{PARTICIPACION}} de las ventas totales que corresponden a los vehículos importados desde {{ORIGEN}}, esto represento la comercialización de {{UNIDADES}} unidades."
                            .replace("{{ORIGEN}}", request.getOrigen().toUpperCase())
                            .replace("{{PERIODO}}", fecha)
                            .replace("{{POSICION}}", posicionString)
                            .replace("{{UNIDADES}}", stellantis.get().getVolumen().toString())
                            .replace("{{PARTICIPACION}}", formatoDecimal.format(stellantis.get().getPorcentajeIndustria()))
                    , portPos, "normal");
        }

        var portHeader = new FilaTabla("Encabezado", List.of("Marcas", "Número de lineas", "Volumen", "Peso", "% MS Industria total para el periodo" + fecha));
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
            var tituloGrafica = "Ventas por Origen " + request.getOrigen() + ", Industria y Market Share del periodo " + fecha;

            presentacion.insertarGrafica(grafica, graficas.createComboChart(tituloGrafica, portadaTotales, request.getOrigen()),
                    new PosicionGrafica(2, 2, 124, 70),
                    new PosicionGrafica(66, 2, 1280, 740));
        }catch (GraficaException e){
            log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
        }

    }

    private void creaVolumenPorMarca(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, RequestOrigen request, String fecha){
        var contraPortada = service.getVolumenMarca(filtrado);
        int endIndex = Math.min(7, contraPortada.size());
        var top = contraPortada.subList(0, endIndex);
        var diapositiva = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);

        //Calculo prcentaje top 6(o menos)
        double totalOrigen = contraPortada.subList(1, contraPortada.size())
                .stream().mapToDouble(valor -> (int) valor.getFila().getLast())
                .sum();

        double totalTop = top.subList(1, endIndex)
                .stream().mapToDouble(valor -> (int) valor.getFila().getLast())
                .sum();

        double porcentaje = totalTop / totalOrigen * 100;
        // Volumen por Marca
        try{
            presentacion.insertarGrafica(diapositiva,
                    graficas.generarGraficaLineasMarcas("Ventas Mensuales por Fabricante " + fecha,top),
                    new PosicionGrafica(2, 2, 84, 49),
                    new PosicionGrafica(66, 2, 1280, 740));
        }catch (GraficaException e){
            log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
        }


        presentacion.creaTexto(diapositiva,"Estas {{NUM_MARCAS}} marcas representaron el {{PORCENTAJE}}% de la venta total correspondiente a los vehículos provenientes de {{ORIGEN}} durante el periodo {{PERIODO}}"
                        .replace("{{NUM_MARCAS}}",String.valueOf(endIndex-1))
                        .replace("{{PORCENTAJE}}", formatoDecimal.format(porcentaje))
                        .replace("{{ORIGEN}}", request.getOrigen().toUpperCase() )
                        .replace("{{PERIODO}}", fecha)
                , new PosicionGrafica(2, 53, 84, 19), "normal");

        //Aqui se generaran los logos de las marcas y su numero de modelos
        var logosPos =new PosicionGrafica(88, 2, 20, 8);
        var nombrePos =new PosicionGrafica(108, 2, 20, 8);
        top
                .stream()
                .filter(fila -> !fila.getFila().getFirst().toString().equalsIgnoreCase("Fabricante"))
                .forEach(fabricante -> {
                    try {
                        var imagenResorce = this.images.recuperaMarca(fabricante.getFila().getFirst().toString().toLowerCase());
                        presentacion.insertarImagen(diapositiva, logosPos, imagenResorce);
                    }catch (Exception e){
                        log.warn("No se puedo cargar la imagen para la marca {}", fabricante.getFila().getFirst().toString());
                    }

                    presentacion.creaTexto(diapositiva, fabricante.getFila().getFirst().toString(),
                            nombrePos, "Normal");
                    nombrePos.addRows(10);
                    logosPos.addRows(10);
                });

    }

    private void crearTopLineas(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, RequestOrigen request,String fecha) {
        List<DaoPeriodo> resumenDatos = service.generarResumen(filtrado, DaoResumenPeriodo::getModelo, s -> s.equalsIgnoreCase(STELANTIS));

        var diapositiva = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);

        //Recuperar solo los 10 modelos top
        var totalOrigen = resumenDatos.getLast();
        var cuerpo = resumenDatos.subList(0, 9); //Estos son el top 10
        Integer totalTop = cuerpo.stream()
                .mapToInt(dP -> dP.getTotal())
                .sum();
        var porcentajeTop = totalTop.doubleValue() / totalOrigen.getTotal().doubleValue() * 100;

        var posGrafica = new PosicionGrafica(2, 2, 90, 50);
        var graficaSize = new PosicionGrafica(2, 2, 900, 500);


        var grafica = graficas.createChart(cuerpo, STELANTIS, "Top 10 ventas de vehículos origen "+request.getOrigen()+", " + fecha);

        try {
            presentacion.insertarGrafica(diapositiva, grafica, posGrafica, graficaSize);
        } catch (GraficaException e) {
            log.info("Fallo la creacion de la grafica por: {}", e.getMessage());
        }
        var posTexto = new PosicionGrafica(4, 54, 90, 45);
        presentacion.creaTexto(diapositiva, "Estos 10 vehículos representan el {{PORCENTAJE}}% de las ventas totales que corresponden a las líneas provenientes de {{ORIGEN}} de {{PERIODO}}"
                        .replace("{{ORIGEN}}", request.getOrigen())
                                .replace("{{PERIODO}}", fecha)
                                .replace("{{PORCENTAJE}}", this.formatoDecimal.format(porcentajeTop))
                , posTexto, "Normal");


        var logosPos =new PosicionGrafica(96, 2, 32, 8);
        var marcaGanadora = resumenDatos.subList(1, resumenDatos.size()-1).getFirst().getFabicante();
        try {
            var imagenResorce = this.images.recuperaMarca(marcaGanadora);
            presentacion.insertarImagen(diapositiva, logosPos, imagenResorce);
        }catch (Exception e){
            log.warn("No se puedo cargar la imagen para la marca");
        }

        logosPos.addRows(10);
        var primerLugar = resumenDatos.stream()
                .filter(res -> res.getFabicante().equalsIgnoreCase(STELANTIS) )
                .map(rs -> rs.getModelo())
                .findFirst().orElse("ram700");
        try {
            log.info("Se intenta recuperar la imagen para la marca {} modelo {}", STELANTIS, primerLugar);
            var imagenResorce = this.images.recuperaModelo(STELANTIS, primerLugar);
            presentacion.insertarImagen(diapositiva, logosPos, imagenResorce);
        }catch (Exception e){
            log.warn("No se puedo cargar la imagen para la marca");
        }

        logosPos.addRows(10);

        try {
            var imagenResorce = this.images.recuperaMarca(STELANTIS);
            presentacion.insertarImagen(diapositiva, logosPos, imagenResorce);
        }catch (Exception e){
            log.warn("No se puedo cargar la imagen para la marca");
        }

        logosPos.addRows(10);
        presentacion.creaTexto(diapositiva, this.getPosicionesTop(cuerpo), logosPos, "Destacado");

        logosPos.addRows(16);

        var segundoLugar = resumenDatos.stream()
                .filter(res -> !res.getModelo().equalsIgnoreCase(primerLugar))
                .filter(res -> res.getFabicante().equalsIgnoreCase(STELANTIS) )
                .map(rs -> rs.getModelo())
                .findFirst().orElse("ram700");
        try {
            log.info("Se intenta recuperar la imagen para la marca {} modelo {}", STELANTIS, primerLugar);
            var imagenResorce = this.images.recuperaModelo(STELANTIS, segundoLugar);
            presentacion.insertarImagen(diapositiva, logosPos, imagenResorce);
        }catch (Exception e){
            log.warn("No se puedo cargar la imagen para la marca {} en el modelo {}", STELANTIS, segundoLugar);
        }
    }

    private String getPosicionesTop(List<DaoPeriodo> datos){
        StringBuilder resultado = new StringBuilder();
        var posiciones = IntStream.range(0, datos.size())
                .filter(i -> datos.get(i).getEstilo().equalsIgnoreCase(STELANTIS))
                .mapToObj(i ->
                        this.capitalizar(datos.get(i).getModelo()) +
                                " se posicionó en " + Utilidades.convertirNumeroAOrdinalShort(i + 1)
                )
                .toList();
        int size = posiciones.size();
        if(size == 1) resultado.append(posiciones.getFirst());
        if(size >= 2){
            resultado.append(String.join(", ", posiciones.subList(0, size-1)))
                    .append(" y ").append(posiciones.getLast());
        }
        return resultado.toString();
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    private void crearHojasPorSegmento(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, RequestOrigen request, String fecha){
        // Genera hojas segmento

        var segmentos = service.generaDatosContenidoPorSegmento(filtrado);
        var segmentoResumen = service.generaResumenSegmento(filtrado);

        var contador = new AtomicInteger(0);
        XSLFSlide diapositiva = null;
        for (var segmento : segmentos) {
            var operacion = contador.getAndIncrement();
            if (operacion % 2 == 0){
                diapositiva = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
            }

            if(Objects.isNull(diapositiva)) {
                diapositiva = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
            }

            //Tabla resumen
            var datosGrafica = graficas.generaDataset(segmentoResumen.get(segmento.getNombreTabla()));
            var grafica =graficas.graficaBarras("Segmento de " + segmento.getNombreTabla() + " - Origen " + request.getOrigen() + " "+ fecha,
                    "Modelos" , "Participacion", datosGrafica);
            try {
                //var marca = this.cargarImagen("static/images/marcas/stellantis.png");
                //var modelo = this.cargarImagen("static/images/marcas/modelo.png");
                var modelo = segmento.getDatos().get(1).getFila().getFirst().toString();

                var fabricante = filtrado.stream()
                        .filter(dato -> dato.getModelo().equalsIgnoreCase(modelo))
                        .map(dato -> dato.getFabricante())
                        .findFirst().orElse("logo");

                var marca = this.images.recuperaMarca(fabricante);
                var modeloImagen = this.images.recuperaModelo(fabricante, modelo);
                log.info("Se recuperaron las imagenes para el modelo {}  y la marca {}", modelo, fabricante);
                if (operacion % 2 == 0) {
                    this.dibujaGraficaIzquierda(presentacion, diapositiva, grafica, modeloImagen, marca, "Arriba");
                }else{
                    this.dibujaGraficaDerecha(presentacion, diapositiva, grafica, modeloImagen, marca, "abajo");
                }
            }catch (Exception e){
                log.warn("No se pudieron cargar las images por: {}", e.getMessage());
            }
        }
    }

    private void creaHojasporMarca(Set<DaoResumenPeriodo> filtrado, CrearPresentacion presentacion, RequestOrigen request, String fecha){

        var fabricantes = service.generaDatosContenidoPorFabricante(filtrado);
        var fabricanteResumen = service.generaResumenFabricante(filtrado);

        // Genera hojas fabricantes

        var contador = new AtomicInteger(0);
        XSLFSlide diapositiva = null;
        for (var fabricante : fabricantes) {
            var operacion = contador.getAndIncrement();
            var totalMarca = service.getTotalFabricante(fabricante.getNombreTabla());
            var totalOrigen = fabricante.getTotal();
            var porcentaje = totalOrigen.doubleValue() / totalMarca.orElse(1).doubleValue() *100 ;
            log.debug("Se obtubo un porcentaje de {} para un total global de {} y un total de origen de {}", porcentaje, totalMarca, totalOrigen);
            if (operacion % 2 == 0) {
                diapositiva = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
            }

            if (Objects.isNull(diapositiva)) {
                diapositiva = presentacion.crearDiapositiva(TipoDiapositiva.CONTENIDO);
            }

            var datosGrafica = graficas.generaPieDataset(fabricanteResumen.get(fabricante.getNombreTabla()));
            var grafica = graficas.graficaDonut("Volumen de ventas, origen " + request.getOrigen() + " " + fecha + " para la marca " + fabricante.getNombreTabla(),
                     datosGrafica);

            var mensaje = " El total de modelos " + fabricante.getNombreTabla() + " procedentes de "
                    + request.getOrigen() + " representaron el "
                    + this.formatoDecimal.format(porcentaje) + "% del volumen total que vendió la marca durante el periodo " +
                    fecha;

            try {
                var marca = this.images.recuperaMarca(fabricante.getNombreTabla());
                var modelo = this.images.recuperaModelo(fabricante.getNombreTabla(),
                        fabricante.getDatos().getFirst().getFila().getFirst().toString());
                if (operacion % 2 == 0) {
                    this.dibujaGraficaIzquierda(presentacion, diapositiva, grafica, modelo, marca, mensaje);
                }else{
                    this.dibujaGraficaDerecha(presentacion, diapositiva, grafica, modelo, marca, mensaje);
                }
            }catch (Exception e){
                log.warn("No se pudieron cargar las images por {}", e.getMessage());
            }
        }

    }

    private void dibujaGraficaIzquierda(CrearPresentacion presentacion,
                                        XSLFSlide diapositiva, JFreeChart grafica,
                                        File modeloResorce, File marcaResorce,
                                        String mensaje){
        var graficaImagen = new PosicionGrafica(0,0,620,340);
        var graficaPocision = new PosicionGrafica(2,2,62, 34);

        var textoGanador = new PosicionGrafica(88, 2, 45,4);
        var marca = new PosicionGrafica(68, 2,16,8);
        var modelo = new PosicionGrafica(68, 8, 38, 18);
        var texto = new PosicionGrafica(68,25,62,10);

        this.dibujaGraficaDiapositiva(graficaImagen, graficaPocision, textoGanador, marca, modelo,
                texto, presentacion,  diapositiva, grafica, modeloResorce, marcaResorce, mensaje);
    }

    private void dibujaGraficaDerecha(CrearPresentacion presentacion,
                                      XSLFSlide diapositiva, JFreeChart grafica,
                                      File modeloResorce, File marcaResorce,
                                      String mensaje){

        var graficaImagen = new PosicionGrafica(0,0,620,340);
        var graficaPocision = new PosicionGrafica(66,39,62, 34);

        var textoGanador = new PosicionGrafica(17, 38, 45,4);
        var marca = new PosicionGrafica(2, 38,16,8);
        var modelo = new PosicionGrafica(37, 45, 38, 18);
        var texto = new PosicionGrafica(2,62,62,10);

        this.dibujaGraficaDiapositiva(graficaImagen, graficaPocision, textoGanador, marca, modelo,
                texto, presentacion,  diapositiva, grafica, modeloResorce, marcaResorce, mensaje);
    }

    private void dibujaGraficaCompleta(CrearPresentacion presentacion,
                                       XSLFSlide diapositiva, JFreeChart grafica,
                                       File modeloResorce, File marcaResorce,
                                       String mensaje){ //Para stellantis

        var graficaImagen = new PosicionGrafica(0,0,640,350);
        var graficaPocision = new PosicionGrafica(2,2,64, 35);

        var textoGanador = new PosicionGrafica(98, 2, 20,5);
        var marca = new PosicionGrafica(45, 2,20,10);
        var modelo = new PosicionGrafica(37, 8, 30, 15);
        var texto = new PosicionGrafica(37,25,64,10);
        this.dibujaGraficaDiapositiva(graficaImagen, graficaPocision, textoGanador, marca, modelo,
                texto, presentacion,  diapositiva, grafica, modeloResorce, marcaResorce, mensaje);
    }

    private void dibujaGraficaDiapositiva(PosicionGrafica graficaImagen, PosicionGrafica graficaPosicion,
                                          PosicionGrafica textoGanador, PosicionGrafica marca,
                                          PosicionGrafica modelo, PosicionGrafica texto,
                                          CrearPresentacion presentacion,
                                          XSLFSlide diapositiva, JFreeChart grafica,
                                          File modeloResorce, File marcaResorce,
                                          String mensaje
                                          ){

        try{
            presentacion.insertarGrafica(diapositiva, grafica, graficaPosicion, graficaImagen);
        } catch (GraficaException e) {
            log.info("Fallo la creacion de la grafica por: {}" , e.getMessage());
        }
        presentacion.creaTexto(diapositiva, "Producto ganador", textoGanador,"Titulo");

        try{
            FileInputStream inputStream = new FileInputStream(marcaResorce);
            presentacion.insertarImagen(diapositiva, marca, inputStream.readAllBytes());
        } catch (IOException e) {
            log.info("Fallo la creacion de la imagen del fabricante por: {}" , e.getMessage());
        }

        try{
            FileInputStream inputStream = new FileInputStream(modeloResorce);
            presentacion.insertarImagen(diapositiva, modelo, inputStream.readAllBytes());
        } catch (IOException e) {
            log.info("Fallo la creacion del modelo por: {}" , e.getMessage());
        }

        presentacion.creaTexto(diapositiva, mensaje,texto ,"Small");

    }


    public String getNombreArchivo() {
        return nombreArchivo;
    }


    private List<ColorPresentacion> creaColoresBase(){
        List<ColorPresentacion> colores = new ArrayList<>();
        colores.add(new ColorPresentacion("Portada", "FFFFFF", "FAFAFA", 18, true));
        colores.add(new ColorPresentacion("Normal", "FFFFFF", "002B7F", 16, true));
        colores.add(new ColorPresentacion("Small", "FFFFFF", "002B7F", 12, true));
        colores.add(new ColorPresentacion("Titulo", "FFFFFF", "000000", 18, true));
        colores.add(new ColorPresentacion("Destacado", "FFFFFF", "FF0000", 18, true));
        colores.add(new ColorPresentacion("Normal2", "FFFFFF", "373F66", 18, true));
        colores.add(new ColorPresentacion("Numeracion", "F6C6CE", "9B182E", 18, true));
        // Colores de tablas
        colores.add(new ColorPresentacion("Encabezado", "002B7F", "FAFAFA", 12, true));
        colores.add(new ColorPresentacion("Estandar", "FFFFFF", "000000", 11, false));
        colores.add(new ColorPresentacion(STELANTIS, "96938E", "FAFAFA", 11, false));
        colores.add(new ColorPresentacion("RowOdd", "FAFAFA", "000000", 11, false));
        colores.add(new ColorPresentacion("TOTAL", "000000", "FAFAFA", 11, true));
        //Color de la lina de divicion
        colores.add(new ColorPresentacion("linea","91162B","91162B", 1, false));
        return colores;
    }
}
