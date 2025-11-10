package com.organizame.reportes.service;


import com.organizame.reportes.dao.DaoResumenPeriodo;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.repository.VhcModeloperiodoindustriaRepository2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ModeloPeriodoService {

    private final VhcModeloperiodoindustriaRepository2 repository;

    private final DateTimeFormatter toIntegerFormater;
    private final DateTimeFormatter mesAnioFormatter;
    private final static Pattern patternSegmento = Pattern.compile("(^(?<segmento1>.+?)\\s+([\\w-]{1,3})\\s.+$)|(^(?<segmento2>.+)$)");

    private LocalDate fechaFinal;
    private LocalDate inicio;
    private Integer mesesRevicion;

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

    public Set<VhcModeloperiodoindustria> recuperaOperacionesOrigen(String  pais){
        return repository.findByOrigenarchivo(pais);
    }

    public List<VhcModeloperiodoindustria> recuperaOrigenVeinticuatoMeses(String  pais, Integer meses){
        this.fechaFinal = LocalDate.now().minusMonths(1);
        this.mesesRevicion = meses;
        this.inicio = fechaFinal.minusMonths(meses);

        int desde = Integer.parseInt(inicio.format(toIntegerFormater));
        int hasta = Integer.parseInt(fechaFinal.format(toIntegerFormater));

        return repository.findUltimosMeses(pais, desde, hasta);
    }

    public List<VhcModeloperiodoindustria> recuperaOrigenFechaInicial(String  pais, Integer meses, LocalDate fechaFinal){
        this.fechaFinal = fechaFinal;
        this.mesesRevicion = meses;
        this.inicio = fechaFinal.minusMonths(meses);

        int desde = Integer.parseInt(inicio.format(toIntegerFormater));
        int hasta = Integer.parseInt(fechaFinal.format(toIntegerFormater));

        return repository.findUltimosMeses(pais, desde, hasta);
    }

    public Set<DaoResumenPeriodo> ResumeData(Collection<VhcModeloperiodoindustria> datos){
        return datos.stream()
                .map( dato ->
                        new DaoResumenPeriodo(
                                Objects.isNull(dato.getVhcmodelo()) ? "NA" : dato.getVhcmodelo().getNombre(),
                                dato.getCantidad(),
                                this.recuperaMesAnioLabel(dato.getPeriodoanio(), dato.getPeriodomes()),
                                dato.getMarcaarchivo(),
                                dato.getFabricantearchivo(),
                                this.recuperaSegmento(dato.getSegmentoarchivo())) )
                .collect(Collectors.toSet());
    }

    public Map<String, List<List<Object>>> generaDatosPivotPorSegmento(Collection<DaoResumenPeriodo> datos) {
        List<String> listaMeses = this.obtenerListaMeses(inicio, this.fechaFinal);

        return generaTablaPivot(datos, listaMeses,
                DaoResumenPeriodo::getSegmento,
                DaoResumenPeriodo::getModelo,
                "Modelo");
    }

    public Map<String, List<List<Object>>> generaDatosPivotPorFabricante(Collection<DaoResumenPeriodo> datos) {
        List<String> listaMeses = this.obtenerListaMeses(inicio, this.fechaFinal);

        return generaTablaPivot(datos, listaMeses,
                DaoResumenPeriodo::getFabricante,
                DaoResumenPeriodo::getModelo,
                "Modelo");
    }

    public Map<String, List<List<Object>>> generaDatosPivotPorMarca(Collection<DaoResumenPeriodo> datos) {

        List<String> listaMeses = this.obtenerListaMeses(this.inicio, this.fechaFinal);

        return generaTablaPivot(datos, listaMeses,
                DaoResumenPeriodo::getMarca,
                DaoResumenPeriodo::getModelo,
                "Modelo");
    }

    private Map<String, List<List<Object>>> generaTablaPivot(
            Collection<DaoResumenPeriodo> datos,
            List<String> listaMeses,
            Function<DaoResumenPeriodo, String> grupoPrincipalExtractor,
            Function<DaoResumenPeriodo, String> subGrupoExtractor,
            String subGrupoTitulo) {

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
            java.util.function.Function<DaoResumenPeriodo, String> subGrupoExtractor,
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

        return respuesta;
    }

    public List<List<Object>> generaDatosTablaSegmentoMarca(Collection<DaoResumenPeriodo> datos) {

        List<List<Object>> respuesta = new ArrayList<>();
        // 🔹 1. Encabezados dinámicos
        List<String> listaMeses = this.obtenerListaMeses(this.inicio, this.fechaFinal);
        List<Object> encabezados = new ArrayList<>();
        encabezados.add("Segmento");
        encabezados.add("Marca");
        encabezados.addAll(listaMeses);
        encabezados.add("Total");
        respuesta.add(encabezados);

        // 🔹 2. Agrupar los datos por segmento, y dentro de cada segmento por marca
        Map<String, Map<String, List<DaoResumenPeriodo>>> agrupado = datos.stream()
                .collect(Collectors.groupingBy(
                        DaoResumenPeriodo::getSegmento,
                        Collectors.groupingBy(DaoResumenPeriodo::getMarca)
                ));

        // 🔹 3. Construir filas: una por marca dentro de cada segmento
        for (var segmentoEntry : agrupado.entrySet()) {
            String segmento = segmentoEntry.getKey();
            var marcas = segmentoEntry.getValue();

            for (var marcaEntry : marcas.entrySet()) {
                String marca = marcaEntry.getKey();
                List<DaoResumenPeriodo> lista = marcaEntry.getValue();

                // Agrupar los registros por mes y sumar cantidades
                Map<String, Double> porMes = lista.stream()
                        .collect(Collectors.groupingBy(
                                DaoResumenPeriodo::getMesAnio,
                                Collectors.summingDouble(DaoResumenPeriodo::getCantidad)
                        ));

                // Crear fila
                List<Object> fila = new ArrayList<>();
                fila.add(segmento);
                fila.add(marca);

                double total = 0.0;
                for (String mes : listaMeses) {
                    double valor = porMes.getOrDefault(mes, 0.0);
                    fila.add(valor);
                    total += valor;
                }

                fila.add(total);
                respuesta.add(fila);
            }
        }

        return respuesta;
    }


    public String recuperaMesAnioLabel(Integer anio, Integer mes){
        return LocalDate.of(anio, mes, 1).format(mesAnioFormatter);
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
}
