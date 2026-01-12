package com.organizame.reportes.repository.service;

import com.organizame.reportes.dto.Margen;
import com.organizame.reportes.persistence.entities.VhcModeloperiodo;
import com.organizame.reportes.persistence.repositories.VhcModeloperiodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PeriodoService {

    //la reduccion de meses que sea con datetime para utilizar diciembre en caso de enero
    public VhcModeloperiodoRepository periodoRepository;

    @Autowired
    public PeriodoService(VhcModeloperiodoRepository periodoRepository){
        this.periodoRepository = periodoRepository;
    }


    public List<VhcModeloperiodo> getRegistrosMes(Integer anio, Integer mes){
        return periodoRepository.findByPeriodoanioAndPeriodomes(anio, mes);
    }



    public Map<String, List<Margen>> groupByMarca(List<Margen> registros){
        return registros.stream()
                .collect(Collectors.groupingBy(Margen::getMarca));
    }

    public List<Margen> getMargenes(List<VhcModeloperiodo> anterior, List<VhcModeloperiodo> actual){
        return actual.stream()
                .map(marg -> {
                    var anteri = anterior.stream()
                            .filter( ant -> ant.getVhcmodelo().equals(marg.getVhcmodelo()))
                            .findFirst().orElse(new VhcModeloperiodo());

                    var flotilla = Objects.isNull(marg.getMenudeoflotillanormal()) ? 0
                            :marg.getMenudeoflotillanormal();

                    var cierre = flotilla == 0
                            ? 0
                            : ((marg.getInventario()/marg.getMenudeoflotillanormal())  * 30);

                    return new Margen(marg.getVhcmodelo().getVhcmarca().getNombre(),
                            marg.getVhcmodelo().getNombre(), anteri.getInventario(), marg.getInventario(),
                            marg.getMenudeoflotillanormal(), cierre);
                } ).toList();

    }
}
