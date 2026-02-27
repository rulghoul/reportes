package com.organizame.reportes.repository.service;

import com.organizame.reportes.dto.auxiliar.CalculoISAN;
import com.organizame.reportes.persistence.entities.ISANParametro;
import com.organizame.reportes.persistence.entities.ISANTarifa;
import com.organizame.reportes.persistence.repositories.ISANParametroRepository;
import com.organizame.reportes.persistence.repositories.ISANTarifaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
public class ISANService {
    private final ISANParametroRepository parametroRepository;
    private final ISANTarifaRepository tarifaRepository;

    @Autowired
    public ISANService(ISANParametroRepository parametroRepository,
                       ISANTarifaRepository tarifaRepository){
        this.parametroRepository = parametroRepository;
        this.tarifaRepository = tarifaRepository;
    }

    @Cacheable(value = "parametroISAN", unless = "#result.isEmpty()")
    public List<ISANParametro> findAllParametros(){
        return parametroRepository.findAll();
    }

    @Cacheable(value = "parameterISAN", unless = "#result.isEmpty()")
    public List<ISANTarifa> findAllTarifas(){
        return tarifaRepository.findAll();
    }

    @CacheEvict(value = {"parametroISAN", "tarifaISAN"}, allEntries = true)
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void limpiarCacheISAN() {
        // Este método está vacío, su única función es disparar la anotación @CacheEvict
    }

    @CacheEvict(value = {"parametroISAN", "tarifaISAN"}, allEntries = true)
    public void forzarActualizacionCache() {
        // Llamar este método después de actualizar parámetros/tarifas en BD
    }

    public CalculoISAN calculaISAN(BigDecimal monto, Integer anio){
        var parametros = findAllParametros().stream()
                .filter(par ->
                        anio.equals(par.getFechaInicio().getYear())
                )
                .findFirst();

        //Aplica tarifa
        if(parametros.isPresent()
                && monto.compareTo(parametros.get().getLimiteSuperiorTasaCero()) <= 0){
            log.info("El monto {} excenta el ISAN", monto);
            return new CalculoISAN(monto, anio, BigDecimal.ZERO, BigDecimal.ZERO, "El monto excenta el ISAN");
        }

        var tarifaOptional = findAllTarifas().stream()
                .filter(tar ->
                        anio.equals(tar.getFechaInicio().getYear())
                )
                .filter(tar -> monto.compareTo(tar.getLimiteInferior()) >= 0)
                .filter(tar -> monto.compareTo(tar.getLimiteSuperior()) < 0 )
                .findFirst();

        if(tarifaOptional.isEmpty()){
            log.error("No se encontro valor para el monto {} en el año {}", monto, anio);
            return new CalculoISAN(monto, anio, BigDecimal.ZERO, BigDecimal.ZERO, "No se encontro tarifa para el monto y el año requeridos");
        }

        var tarifa = tarifaOptional.get();

        var isan = monto.subtract(tarifa.getLimiteInferior())
                .multiply(tarifa.getPorcentaje())
                .add(tarifa.getCuotaFija());


        if(parametros.isPresent()
                && monto.compareTo(parametros.get().getLimiteSuperiorTasaCincuenta()) <= 0){
            log.info("El monto {} está exento del ISAN", monto);
            var isanCincuenta = isan.divide(BigDecimal.TWO, 2, RoundingMode.HALF_UP);
            return new CalculoISAN(monto, anio, isanCincuenta, isan, "Aplica descuento del 50%");
        }else{
            return new CalculoISAN(monto, anio, BigDecimal.ZERO, isan, "No aplica descuento del 50%");
        }
    }
}
