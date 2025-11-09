package com.organizame.reportes.service;


import com.organizame.reportes.dao.DaoResumenPeriodo;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.repository.VhcModeloperiodoindustriaRepository2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModeloPeriodoService {

    private VhcModeloperiodoindustriaRepository2 repository;

    DateTimeFormatter toIntegerFormater;
    DateTimeFormatter mesAnioFormatter;
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
        LocalDate ahora = LocalDate.now().minusMonths(1);
        LocalDate inicio = ahora.minusMonths(meses);

        int desde = Integer.parseInt(inicio.format(DateTimeFormatter.ofPattern("yyyyMM")));
        int hasta = Integer.parseInt(ahora.format(DateTimeFormatter.ofPattern("yyyyMM")));

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
                                dato.getSegmentoarchivo()) )
                .collect(Collectors.toSet());
    }

    public List<List<Object>> generaDatosTablaSegmentoMarca(Collection<DaoResumenPeriodo> datos, Integer meses) {
        LocalDate ahora = LocalDate.now().minusMonths(1);
        LocalDate inicio = ahora.minusMonths(meses);
        List<List<Object>> respuesta = new ArrayList<>();

        // 🔹 1. Encabezados dinámicos
        List<String> listaMeses = this.obtenerListaMeses(inicio, ahora);
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
}
