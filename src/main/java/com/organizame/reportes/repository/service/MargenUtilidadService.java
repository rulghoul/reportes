package com.organizame.reportes.repository.service;

import com.organizame.reportes.dto.MargenUtilidad;
import com.organizame.reportes.dto.MargenUtilidadFactory;
import com.organizame.reportes.persistence.entities.*;
import com.organizame.reportes.persistence.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MargenUtilidadService {

    private final VhcBoletinprecioRepository boletinprecioRepository;
    private final VhcBoletinpreciogastoRepository boletinpreciogastoRepository;
    private final VhcDaacuotaRepository daacuotaRepository;
    private final VhcIncentivoRepository incentivoRepository;
    private final VhcReembolsoRepository reembolsoRepository;
    private final VhcMargenUtilidadRepository margenUtilidadRepository;
    private final MargenUtilidadFactory margenUtilidadFactory;

    @Autowired
    public MargenUtilidadService(VhcBoletinprecioRepository boletinprecioRepository, VhcBoletinpreciogastoRepository boletinpreciogastoRepository,
                                 VhcDaacuotaRepository daacuotaRepository, VhcIncentivoRepository incentivoRepository,
                                 VhcReembolsoRepository reembolsoRepository, VhcMargenUtilidadRepository margenUtilidadRepository,
                                 MargenUtilidadFactory margenUtilidadFactory){
        this.boletinprecioRepository = boletinprecioRepository;
        this.boletinpreciogastoRepository = boletinpreciogastoRepository;
        this.daacuotaRepository = daacuotaRepository;
        this.incentivoRepository = incentivoRepository;
        this.reembolsoRepository = reembolsoRepository;
        this.margenUtilidadRepository = margenUtilidadRepository;
        this.margenUtilidadFactory = margenUtilidadFactory;
    }

    public List<MargenUtilidad> getMargenes(LocalDate fecha){

        var boletines = boletinprecioRepository
                .lastedBoletin(fecha.atTime(23,59), String.valueOf(fecha.getYear()));

        Map<VhcAnio, VhcBoletinprecio> resultado =
                boletines.stream()
                        .collect(Collectors.toMap(
                                VhcBoletinprecio::getVhcanio,
                                Function.identity(),
                                (e1, e2) -> e1.getFechainicio().isAfter(e2.getFechainicio()) ? e1 : e2
                        ));

        List<VhcBoletinprecio> filtrada = new ArrayList<>(resultado.values());

        var anios = new ArrayList<>(resultado.keySet());

        var boletinesGastos = boletinpreciogastoRepository.getGastosBoletin(filtrada);

        var cuotaBoletin = daacuotaRepository.getCuotaBoletin(anios);

        var incentivoBoletin = incentivoRepository.findByVhcanioInAndPeriodomesLessThanEqualOrderByPeriodomesDesc(anios,fecha.getMonth().getValue());

        var rembolsoBoletin = reembolsoRepository.getBoletinRembolso(anios);


        return filtrada.stream()
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

    public void SaveMargenes(List<VhcMargenUtilidad> margenes){
        margenUtilidadRepository.saveAll(margenes);
    }
}
