package com.organizame.reportes.repository.service;

import com.organizame.reportes.dto.MargenUtilidad;
import com.organizame.reportes.dto.MargenUtilidadFactory;
import com.organizame.reportes.persistence.entities.VhcBoletinpreciogasto;
import com.organizame.reportes.persistence.entities.VhcDaacuota;
import com.organizame.reportes.persistence.entities.VhcIncentivo;
import com.organizame.reportes.persistence.entities.VhcReembolso;
import com.organizame.reportes.persistence.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MargenUtilidadService {

    private final VhcBoletinprecioRepository boletinprecioRepository;
    private final VhcBoletinpreciogastoRepository boletinpreciogastoRepository;
    private final VhcDaacuotaRepository daacuotaRepository;
    private final VhcIncentivoRepository incentivoRepository;
    private final VhcReembolsoRepository reembolsoRepository;
    private final MargenUtilidadFactory margenUtilidadFactory;

    @Autowired
    public MargenUtilidadService(VhcBoletinprecioRepository boletinprecioRepository, VhcBoletinpreciogastoRepository boletinpreciogastoRepository,
                                 VhcDaacuotaRepository daacuotaRepository, VhcIncentivoRepository incentivoRepository,
                                 VhcReembolsoRepository reembolsoRepository, MargenUtilidadFactory margenUtilidadFactory){
        this.boletinprecioRepository = boletinprecioRepository;
        this.boletinpreciogastoRepository = boletinpreciogastoRepository;
        this.daacuotaRepository = daacuotaRepository;
        this.incentivoRepository = incentivoRepository;
        this.reembolsoRepository = reembolsoRepository;
        this.margenUtilidadFactory = margenUtilidadFactory;
    }

    public List<MargenUtilidad> getMargenes(LocalDate fecha){

        var boletines = boletinprecioRepository
                .lastedBoletin(fecha.atStartOfDay(), String.valueOf(fecha.getYear()));

        var anios = boletines.stream().map(boletin -> boletin.getVhcanio()).toList();

        var boletinesGastos = boletinpreciogastoRepository.getGastosBoletin(boletines);

        var cuotaBoletin = daacuotaRepository.getCuotaBoletin(anios);

        var incentivoBoletin = incentivoRepository.getBoletinIncentivo(anios);

        var rembolsoBoletin = reembolsoRepository.getBoletinRembolso(anios);


        return boletines.stream()
                .map(boletin -> {
                    boletin.setDistribuidorisan(BigDecimal.TEN);
                    boletinprecioRepository.save(boletin);
                    var gastos = boletinesGastos.stream()
                            .filter(gasto -> gasto.getVhcboletinprecio().equals(boletin))
                            .toList();
                    var cuota = cuotaBoletin.stream()
                            .filter(incent -> incent.getVhcanio().equals(boletin.getVhcanio()))
                            .findFirst();
                    var insentivo = incentivoBoletin.stream()
                            .filter(incent -> incent.getVhcanio().equals(boletin.getVhcanio()))
                            .findFirst();

                    var rembolso = rembolsoBoletin.stream()
                            .filter( remb -> remb.getVhcanio().equals(boletin.getVhcanio()))
                            .findFirst();


                    return margenUtilidadFactory.crearMargenUtilidad(
                            boletin, gastos, cuota, insentivo, rembolso
                    );
                }).toList();

    }


}
