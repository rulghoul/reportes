package com.organizame.reportes.service;

import com.organizame.reportes.entity.VhcModeloperiodoindustria;
import com.organizame.reportes.repository.VhcModeloperiodoindustriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ModeloPeriodoService {

    private VhcModeloperiodoindustriaRepository repository;

    @Autowired
    public  ModeloPeriodoService(VhcModeloperiodoindustriaRepository repository){
        this.repository = repository;
    }

    public List<String> recuperaOrigenes(){
        return repository.findDistinctByOrderByOrigenarchivoAsc();
    }

    public Set<VhcModeloperiodoindustria> recuperaOperacionesOrigen(String  pais){
        return repository.findByOrigenarchivo(pais);
    }
}
