package com.organizame.reportes.dto;

import com.organizame.reportes.persistence.entities.*;
import com.organizame.reportes.repository.service.ISANService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MargenUtilidadFactory {

    @Autowired
    private ISANService isanService;

    public MargenUtilidad crearMargenUtilidad(VhcBoletinprecio boletinprecio,
                                              List<VhcBoletinpreciogasto> boletinpreciogasto,
                                              Optional<VhcDaacuota> daacuotaEntity,
                                              Optional<VhcIncentivo> incentivo,
                                              Optional<VhcReembolso> reembolso) {

        return new MargenUtilidad(boletinprecio, boletinpreciogasto,
                daacuotaEntity, incentivo, reembolso, isanService);
    }
}
