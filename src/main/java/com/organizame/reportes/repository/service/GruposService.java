package com.organizame.reportes.repository.service;

import com.organizame.reportes.persistence.entities.VhcGrupo;
import com.organizame.reportes.persistence.repositories.VhcGrupoRepository;
import com.organizame.reportes.utils.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class GruposService {    private final DateTimeFormatter toIntegerFormater;
    private final DateTimeFormatter mesAnioFormatter;
    private final VhcGrupoRepository repository;

    @Autowired
    public GruposService(VhcGrupoRepository repository){
        this.repository = repository;
        this.toIntegerFormater = DateTimeFormatter.ofPattern("yyyyMM", Constantes.LOCALE_MX);
        this.mesAnioFormatter = DateTimeFormatter.ofPattern("MMM-yyyy", Constantes.LOCALE_MX);
    }

    public List<VhcGrupo> findAll(){
        return repository.findAll();
    }
}
