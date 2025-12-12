package com.organizame.reportes.repository.service;

import com.organizame.reportes.persistence.entities.BnkEstadofinanciero;
import com.organizame.reportes.repository.BnkEstadofinancieroRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class EstadoFinancieroService {
    private final DateTimeFormatter toIntegerFormater;
    private final DateTimeFormatter mesAnioFormatter;
    private final BnkEstadofinancieroRepository repository;

    @Autowired
    public EstadoFinancieroService(BnkEstadofinancieroRepository repository){
        this.repository = repository;
        this.toIntegerFormater = DateTimeFormatter.ofPattern("yyyyMM");
        this.mesAnioFormatter = DateTimeFormatter.ofPattern("MMM-yyyy");
    }

    public List<BnkEstadofinanciero> findEstadoFechas(LocalDate inicio, LocalDate fin){

        int desde = Integer.parseInt(inicio.format(toIntegerFormater));
        int hasta = Integer.parseInt(fin.format(toIntegerFormater));
        return repository.findEstadoFinancieroPorFechas(desde, hasta);
    }
}
