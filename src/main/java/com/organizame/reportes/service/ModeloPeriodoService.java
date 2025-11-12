package com.organizame.reportes.service;


import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.DaoResumenPeriodo;
import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.dto.TablaContenido;
import com.organizame.reportes.dto.auxiliar.Acumulado;
import com.organizame.reportes.dto.auxiliar.PortadaTotales;
import com.organizame.reportes.dto.auxiliar.ResumenHelp;
import com.organizame.reportes.dto.request.RequestOrigen;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.repository.VhcModeloperiodoindustriaRepository2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ModeloPeriodoService {

    private final VhcModeloperiodoindustriaRepository2 repository;

    private final DateTimeFormatter toIntegerFormater;
    private final DateTimeFormatter mesAnioFormatter;
    private final static Pattern patternSegmento = Pattern.compile("((.+?)\\s(?<segmento1>[\\w]{1,3})\\s.+)|(?<segmento2>.+)");


    private LocalDate fechaFinal;
    private LocalDate inicio;

    @Autowired
    public  ModeloPeriodoService(VhcModeloperiodoindustriaRepository2 repository){
        this.repository = repository;
        this.toIntegerFormater = DateTimeFormatter.ofPattern("yyyyMM");
        this.mesAnioFormatter = DateTimeFormatter.ofPattern("MMM-yyyy");
    }

    public List<String> recuperaOrigenes(){
        return repository.findDistinctByOrderByOrigenarchivoAsc();
    }
    public List<String> recuperaSegmento(){
        return repository.findDistinctByOrderBySegmentoarchivoAsc();
    }


    public List<VhcModeloperiodoindustria> recuperaOrigenFechaInicial(String  pais, Integer meses, LocalDate fechaFinal){
        this.fechaFinal = fechaFinal;
        this.inicio = fechaFinal.minusMonths(meses);

        int desde = Integer.parseInt(this.inicio.format(toIntegerFormater));
        int hasta = Integer.parseInt(this.fechaFinal.format(toIntegerFormater));

        return repository.findUltimosMeses(pais, desde, hasta);
    }

    public Set<DaoResumenPeriodo> ResumeData(Collection<VhcModeloperiodoindustria> datos){
        return datos.stream()
                .map( dato ->
                        new DaoResumenPeriodo(
                                dato.getModeloarchivo(),
                                dato.getCantidad(),
                                this.recuperaMesAnioLabel(dato.getPeriodoanio(), dato.getPeriodomes()),
                                this.recuperaMesAnioDate(dato.getPeriodoanio(), dato.getPeriodomes()),
                                dato.getMarcaarchivo(),
                                dato.getFabricantearchivo(),
                                this.recuperaSegmento(dato.getSegmentoarchivo())) )
                .collect(Collectors.toSet());
    }

    public Map<String, List<List<Object>>> generaDatosPivotPorSegmento(Collection<DaoResumenPeriodo> datos) {
        return generaTablaPivot(datos,
                DaoResumenPeriodo::getSegmento,
                DaoResumenPeriodo::getModelo,
                "Modelo");
    }

    public Map<String, List<List<Object>>> generaDatosPivotPorFabricante(Collection<DaoResumenPeriodo> datos) {
        return generaTablaPivot(datos,
                DaoResumenPeriodo::getFabricante,
                DaoResumenPeriodo::getModelo,
                "Modelo");
    }

    public Map<String, List<List<Object>>> generaDatosPivotPorMarca(Collection<DaoResumenPeriodo> datos) {
        return generaTablaPivot(datos,
                DaoResumenPeriodo::getMarca,
                DaoResumenPeriodo::getModelo,
                "Modelo");
    }

    public List<TablaContenido> generaDatosContenidoPorSegmento(Collection<DaoResumenPeriodo> datos) {
        return generaTablaContenido(datos,
                DaoResumenPeriodo::getSegmento,
                DaoResumenPeriodo::getModelo,
                reg -> reg.getFabricante().equalsIgnoreCase("Stellantis"),
                "Modelo");
    }

    public List<TablaContenido> generaDatosContenidoPorFabricante(Collection<DaoResumenPeriodo> datos) {
        return generaTablaContenido(datos,
                DaoResumenPeriodo::getFabricante,
                DaoResumenPeriodo::getModelo,
                reg -> false,
                "Modelo");
    }

    private List<TablaContenido> generaTablaContenido(
            Collection<DaoResumenPeriodo> datos,
            Function<DaoResumenPeriodo, String> grupoPrincipalExtractor,
            Function<DaoResumenPeriodo, String> subGrupoExtractor,
            Predicate<DaoResumenPeriodo> buscador,
            String subGrupoTitulo) {
        List<String> listaMeses = this.obtenerListaMeses(inicio, this.fechaFinal);
        // Agrupar por el campo principal (segmento o marca)
        Map<String, List<DaoResumenPeriodo>> datosAgrupados = datos.stream()
                .collect(Collectors.groupingBy(grupoPrincipalExtractor));

        List<TablaContenido> resultado = new ArrayList<>();
        for (Map.Entry<String, List<DaoResumenPeriodo>> entry : datosAgrupados.entrySet()) {
            String grupoPrincipal = entry.getKey();
            List<DaoResumenPeriodo> datosGrupo = entry.getValue();

            //Total
            var totalGlobal = datosGrupo.stream().mapToInt(DaoResumenPeriodo::getCantidad).sum();
            var totalStellantis = datosGrupo.stream()
                    .filter(buscador)
                    .mapToInt(DaoResumenPeriodo::getCantidad).sum();

            var tablaGrupo = generaTablaParaGrupo(datosGrupo, listaMeses, subGrupoExtractor, buscador, subGrupoTitulo);

            resultado.add(new TablaContenido(grupoPrincipal, tablaGrupo, totalGlobal, totalStellantis));
        }

        return resultado;
    }


    private Map<String, List<List<Object>>> generaTablaPivot(
            Collection<DaoResumenPeriodo> datos,
            Function<DaoResumenPeriodo, String> grupoPrincipalExtractor,
            Function<DaoResumenPeriodo, String> subGrupoExtractor,
            String subGrupoTitulo) {
        List<String> listaMeses = this.obtenerListaMeses(inicio, this.fechaFinal);
        // Agrupar por el campo principal (segmento o marca)
        Map<String, List<DaoResumenPeriodo>> datosAgrupados = datos.stream()
                .collect(Collectors.groupingBy(grupoPrincipalExtractor));

        Map<String, List<List<Object>>> resultado = new HashMap<>();

        for (Map.Entry<String, List<DaoResumenPeriodo>> entry : datosAgrupados.entrySet()) {
            String grupoPrincipal = entry.getKey();
            List<DaoResumenPeriodo> datosGrupo = entry.getValue();

            List<List<Object>> tablaGrupo = generaTablaParaGrupo(datosGrupo, listaMeses, subGrupoExtractor, subGrupoTitulo);
            resultado.put(grupoPrincipal, tablaGrupo);
        }

        return resultado;
    }


    private List<List<Object>> generaTablaParaGrupo(
            List<DaoResumenPeriodo> datosGrupo,
            List<String> listaMeses,
            Function<DaoResumenPeriodo, String> subGrupoExtractor,
            String subGrupoTitulo) {

        List<List<Object>> respuesta = new ArrayList<>();

        // Encabezados
        List<Object> encabezados = new ArrayList<>();
        encabezados.add(subGrupoTitulo);
        encabezados.addAll(listaMeses);
        encabezados.add("Total");
        respuesta.add(encabezados);

        // Agrupar por el subgrupo (marca o segmento)
        Map<String, List<DaoResumenPeriodo>> datosPorSubGrupo = datosGrupo.stream()
                .collect(Collectors.groupingBy(subGrupoExtractor));

        for (Map.Entry<String, List<DaoResumenPeriodo>> subGrupoEntry : datosPorSubGrupo.entrySet()) {
            String subGrupo = subGrupoEntry.getKey();
            List<DaoResumenPeriodo> lista = subGrupoEntry.getValue();

            // Agrupar por mes y sumar cantidades
            Map<String, Double> porMes = lista.stream()
                    .collect(Collectors.groupingBy(
                            DaoResumenPeriodo::getMesAnio,
                            Collectors.summingDouble(DaoResumenPeriodo::getCantidad)
                    ));

            // Crear fila
            List<Object> fila = new ArrayList<>();
            fila.add(subGrupo);

            double total = 0.0;
            for (String mes : listaMeses) {
                double valor = porMes.getOrDefault(mes, 0.0);
                fila.add(valor);
                total += valor;
            }

            fila.add(total);
            respuesta.add(fila);
        }
        respuesta.subList(1, respuesta.size()).sort((fila1, fila2) -> {
            Double totalFila1 = (Double) fila1.getLast();
            Double totalFila2 = (Double) fila2.getLast();
            return totalFila2.compareTo(totalFila1);
        });
        return respuesta;
    }

    private List<FilaTabla> generaTablaParaGrupo(
            List<DaoResumenPeriodo> datosGrupo,
            List<String> listaMeses,
            Function<DaoResumenPeriodo, String> subGrupoExtractor,
            Predicate<DaoResumenPeriodo> busqueda,
            String subGrupoTitulo) {

        List<FilaTabla> respuesta = new ArrayList<>();

        // Encabezados
        List<Object> encabezados = new ArrayList<>();
        encabezados.add(subGrupoTitulo);
        encabezados.addAll(listaMeses);
        encabezados.add("Total");
        respuesta.add(new FilaTabla("Encanbezado", encabezados));

        // Agrupar por el subgrupo (marca o segmento)
        Map<String, List<DaoResumenPeriodo>> datosPorSubGrupo = datosGrupo.stream()
                .collect(Collectors.groupingBy(subGrupoExtractor));

        for (Map.Entry<String, List<DaoResumenPeriodo>> subGrupoEntry : datosPorSubGrupo.entrySet()) {
            String subGrupo = subGrupoEntry.getKey();
            List<DaoResumenPeriodo> lista = subGrupoEntry.getValue();

            String estilo = busqueda.test(lista.getFirst()) ? "STELLANTIS" :"ESTANDAR";
            // Agrupar por mes y sumar cantidades
            Map<String, Double> porMes = lista.stream()
                    .collect(Collectors.groupingBy(
                            DaoResumenPeriodo::getMesAnio,
                            Collectors.summingDouble(DaoResumenPeriodo::getCantidad)
                    ));

            // Crear fila
            List<Object> fila = new ArrayList<>();
            fila.add(subGrupo);

            double total = 0.0;
            for (String mes : listaMeses) {
                double valor = porMes.getOrDefault(mes, 0.0);
                fila.add(valor);
                total += valor;
            }

            fila.add(total);
            respuesta.add(new FilaTabla(estilo,fila));
        }
        respuesta.subList(1, respuesta.size()).sort((fila1, fila2) -> {
            Double totalFila1 = (Double) fila1.getFila().getLast();
            Double totalFila2 = (Double) fila2.getFila().getLast();
            return totalFila2.compareTo(totalFila1);
        });
        return respuesta;
    }

    public Map<String, List<DaoPeriodo>> generaResumenSegmento(Collection<DaoResumenPeriodo> datos) {
        return generaResumenPorGrupo(
                datos,
                DaoResumenPeriodo::getSegmento,
                DaoResumenPeriodo::getModelo,
                reg -> reg.equalsIgnoreCase("Stellantis")
        );
    }

    public Map<String, List<DaoPeriodo>> generaResumenFabricante(Collection<DaoResumenPeriodo> datos) {
        return generaResumenPorGrupo(
                datos,
                DaoResumenPeriodo::getFabricante,
                DaoResumenPeriodo::getModelo,
                reg -> false
        );
    }

    public Map<String, List<DaoPeriodo>> generaResumenMarca(Collection<DaoResumenPeriodo> datos) {
        return generaResumenPorGrupo(
                datos,
                DaoResumenPeriodo::getMarca,
                DaoResumenPeriodo::getModelo,
                reg -> false
        );
    }

    private Map<String, List<DaoPeriodo>> generaResumenPorGrupo(
            Collection<DaoResumenPeriodo> datos,
            Function<DaoResumenPeriodo, String> grupoPrincipalExtractor,
            Function<DaoResumenPeriodo, String> subGrupoExtractor,
            Predicate<String> buscador
    ) {
        Map<String, List<DaoResumenPeriodo>> agrupado = datos.stream()
                .collect(Collectors.groupingBy(grupoPrincipalExtractor));

        Map<String, List<DaoPeriodo>> resultado = new TreeMap<>();

        for (Map.Entry<String, List<DaoResumenPeriodo>> entry : agrupado.entrySet()) {
            String grupo = entry.getKey();
            List<DaoResumenPeriodo> subset = entry.getValue();
            // Generar resumen interno (por subgrupo)
            List<DaoPeriodo> resumen = generarResumen(subset, subGrupoExtractor, buscador);
            resultado.put(grupo, resumen);
        }

        return resultado;
    }


    public List<DaoPeriodo> generarResumen(
            Collection<DaoResumenPeriodo> datos,
            Function<DaoResumenPeriodo, String> grupoExtractor,
            Predicate<String> buscador
    ) {
        // Agrupar y sumar cantidades
        Map<String, ResumenHelp> totales = datos.stream()
                .collect(Collectors.groupingBy(
                        grupoExtractor,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list ->{
                                    Integer total = list.stream().mapToInt(DaoResumenPeriodo::getCantidad).sum();
                                    var fabricante = list.getFirst().getFabricante();
                                    return new ResumenHelp(total, fabricante);
                                }
                        )
                ));

        Integer totalGlobal = totales.values().stream()
                .mapToInt(res -> res.getCantidad())
                .sum();

        List<DaoPeriodo> tabla = new ArrayList<>();


        totales.entrySet().stream()
                .sorted(Map.Entry.<String, ResumenHelp>comparingByValue(
                        Comparator.comparingDouble(ResumenHelp::getCantidad).reversed()
                ))
                .forEach(entry -> {
                    String nombre = entry.getKey();
                    Integer total = entry.getValue().getCantidad();
                    double porcentaje = totalGlobal > 0 ? (total.doubleValue() / totalGlobal.doubleValue()) * 100 : 0.0;
                    String estilo = buscador.test(entry.getValue().getFabricante()) ? "Stellantis" : "Estandar";
                    tabla.add(new DaoPeriodo(nombre, total, porcentaje, estilo));
                });

        // Fila total
        tabla.add(new DaoPeriodo("TOTAL", totalGlobal, 100.0, "Total"));


        return tabla;
    }



    public String recuperaMesAnioLabel(Integer anio, Integer mes){
        return LocalDate.of(anio, mes, 1).format(mesAnioFormatter);
    }

    public LocalDate recuperaMesAnioDate(Integer anio, Integer mes){
        return LocalDate.of(anio, mes, 1);
    }

    private List<String> obtenerListaMeses(LocalDate inicio, LocalDate fin) {
        List<String> meses = new ArrayList<>();
        LocalDate mesInicial = inicio.withDayOfMonth(1);
        while (!mesInicial.isAfter(fin)) {
            meses.add(mesInicial.format(mesAnioFormatter));
            mesInicial = mesInicial.plusMonths(1);
        }
        return meses;
    }

    private String recuperaSegmento(String segmentoOriginal){
        Matcher match = patternSegmento.matcher(segmentoOriginal);
        if (match.matches()) {
            return Objects.nonNull(match.group("segmento1")) ?
                    match.group("segmento1").trim() :
                    match.group("segmento2").trim();
        }
        return "NA";
    }



    // Datos portada

    public Optional<Integer> getTotalIntustria() {
        int desde = Integer.parseInt(this.inicio.format(toIntegerFormater));
        int hasta = Integer.parseInt(this.fechaFinal.format(toIntegerFormater));
        return  repository.findSumaTotalCantidadGlobalPorFechas(desde, hasta);
    }

    public Optional<Integer> getTotalOrigen(RequestOrigen request) {
        int desde = Integer.parseInt(this.inicio.format(toIntegerFormater));
        int hasta = Integer.parseInt(this.fechaFinal.format(toIntegerFormater));
        return repository.findSumaTotalCantidadPorFiltros(request.getOrigen(), desde, hasta);
    }

    public List<Acumulado> getPortadaMesesResumen(Set<DaoResumenPeriodo> filtrado, Integer totalIndustria, Integer totalOrigen) {
        // Se dividen en dubgrupos divididos por fabricante
        Map<String, List<DaoResumenPeriodo>> portadaMarcas = filtrado.stream()
                .collect(Collectors.groupingBy(DaoResumenPeriodo::getFabricante));

        // Se generan los registros para acumulados
        var acumulados = portadaMarcas.entrySet().stream().map(grupo -> {
            String fabricante = grupo.getKey();
            Integer lineas = grupo.getValue().stream()
                    .map(reg -> reg.getModelo())
                    .collect(Collectors.toSet()).size();
            Integer volumen = grupo.getValue().stream()
                    .mapToInt(reg -> reg.getCantidad())
                    .sum();
            var peso = volumen.doubleValue() / totalOrigen.doubleValue() * 100;
            var porcentaje = volumen.doubleValue() / totalIndustria.doubleValue() * 100;
            return new Acumulado(fabricante, lineas, volumen, peso, porcentaje);
        }).sorted(Comparator.comparing(Acumulado::getVolumen).reversed())
                .toList();
        //Se agrega linea de totales
        var lineas = filtrado.stream().map(reg -> reg.getModelo())
                .collect(Collectors.toSet()).size();
        var porcentaje = totalOrigen.doubleValue() / totalIndustria.doubleValue() * 100;
        acumulados.add(new Acumulado("Total", lineas, totalOrigen, 100.0, porcentaje));

        return acumulados;
    }

    public List<PortadaTotales> getPortadaTotales(Set<DaoResumenPeriodo> filtrado) {

        int desde = Integer.parseInt(this.inicio.format(toIntegerFormater));
        int hasta = Integer.parseInt(this.fechaFinal.format(toIntegerFormater));
        List<Object[]> totalesIndustria = repository.findSumaCantidadPorAnioMesGlobal(desde, hasta);

        // Convertir a mapa usando tu método existente para formatear la cadena
        Map<String, Integer> totalIndustria = totalesIndustria.stream()
                .collect(Collectors.toMap(
                        row -> this.recuperaMesAnioLabel(((Number) row[0]).intValue(), ((Number) row[1]).intValue()), // Tu método existente
                        row -> ((Number) row[2]).intValue()
                ));

        Map<String, Integer> porMes = filtrado.stream()
                .sorted(Comparator.comparing(DaoResumenPeriodo::getMesDate).reversed())
                .collect(Collectors.groupingBy(
                        DaoResumenPeriodo::getMesAnio,
                        Collectors.summingInt(DaoResumenPeriodo::getCantidad)
                ));
        return porMes.entrySet().stream()
                .map(mes -> {
            var totalMes = totalIndustria.get(mes.getKey());
            var porcentaje = mes.getValue().doubleValue() / totalMes.doubleValue();
            return new PortadaTotales(mes.getKey(), mes.getValue(), totalMes, porcentaje );
        }).toList();
    }

    public Object getVolumenMarca(Set<DaoResumenPeriodo> filtrado){
        List<String> listaMeses = this.obtenerListaMeses(inicio, this.fechaFinal);
        return this.generaTablaPivotePorFabricante(filtrado, listaMeses);
    }

    public List<List<Object>> generaTablaPivotePorFabricante(
            Collection<DaoResumenPeriodo> datos,
            List<String> listaMeses) {

        List<List<Object>> respuesta = new ArrayList<>();

        // Encabezados
        List<Object> encabezados = new ArrayList<>();
        encabezados.add("Fabricante");
        encabezados.addAll(listaMeses);
        encabezados.add("Total");
        respuesta.add(encabezados);

        // Agrupar por fabricante y luego por mes
        Map<String, Map<String, Double>> datosAgrupados = datos.stream()
                .collect(Collectors.groupingBy(
                        DaoResumenPeriodo::getFabricante,
                        Collectors.groupingBy(
                                DaoResumenPeriodo::getMesAnio,
                                Collectors.summingDouble(DaoResumenPeriodo::getCantidad)
                        )
                ));

        // Crear una fila por cada fabricante
        for (Map.Entry<String, Map<String, Double>> fabricanteEntry : datosAgrupados.entrySet()) {
            String fabricante = fabricanteEntry.getKey();
            Map<String, Double> porMes = fabricanteEntry.getValue();

            List<Object> fila = new ArrayList<>();
            fila.add(fabricante);

            double totalFabricante = 0.0;
            for (String mes : listaMeses) {
                double valor = porMes.getOrDefault(mes, 0.0);
                fila.add(valor);
                totalFabricante += valor;
            }

            fila.add(totalFabricante);
            respuesta.add(fila);
        }

        // Ordenar por total descendente (excepto encabezados)
        if (respuesta.size() > 1) {
            respuesta.subList(1, respuesta.size()).sort((fila1, fila2) -> {
                Double totalFila1 = (Double) fila1.get(fila1.size() - 1);
                Double totalFila2 = (Double) fila2.get(fila2.size() - 1);
                return totalFila2.compareTo(totalFila1);
            });
        }

        return respuesta;
    }

}
