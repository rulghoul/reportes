package com.organizame.reportes.repository.service;

import com.organizame.reportes.persistence.entities.VhcPeriodo;
import com.organizame.reportes.persistence.repositories.VhcPeriodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PeriodoService {

    public final VhcPeriodoRepository periodoRepository;

    @Autowired
    public PeriodoService(VhcPeriodoRepository periodoRepository){
        this.periodoRepository = periodoRepository;
    }


    public List<VhcPeriodo> getRegistrosMes(Integer anio, Integer mes){
        return periodoRepository.findByPeriodoanioAndPeriodomes(anio, mes);
    }

    public Map<String, List<VhcPeriodo>> groupByMarca(List<VhcPeriodo> registros){
        return registros.stream()
                .collect(Collectors.groupingBy(VhcPeriodo::getMarcaArchivo));
    }

    public List<VhcPeriodo> consolidaModelo(List<VhcPeriodo> registros){
        Map<String, List<VhcPeriodo>> modelos = registros.stream()
                .collect(Collectors.groupingBy(VhcPeriodo::getModeloArchivo));
        return modelos.entrySet()
                .stream()
                .map(modelo -> modelo.getValue().stream().reduce(new VhcPeriodo(), VhcPeriodo::sumarCon))
                .toList();
    }
}
