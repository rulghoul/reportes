package com.organizame.reportes.persistence.repositories;

import com.organizame.reportes.persistence.entities.VhcMargenUtilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VhcMargenUtilidadRepository extends JpaRepository<VhcMargenUtilidad, byte[]> {

    // ==================== CONSULTAS POR PERIODO ====================

    /**
     * Busca todos los márgenes de un año específico
     */
    List<VhcMargenUtilidad> findByPeriodoAnio(Integer periodoAnio);

    /**
     * Busca todos los márgenes de un año y mes específicos
     */
    List<VhcMargenUtilidad> findByPeriodoAnioAndPeriodoMes(Integer periodoAnio, Short periodoMes);

    /**
     * Busca todos los márgenes de una fecha específica (año, mes, día)
     */
    List<VhcMargenUtilidad> findByPeriodoAnioAndPeriodoMesAndPeriodoDia(
            Integer periodoAnio, Short periodoMes, Short periodoDia);


    // ==================== CONSULTAS ESPECÍFICAS ====================

    /**
     * Busca un margen específico por ID de margen utilidad
     * (Alternativa al findById del JpaRepository)
     */
    Optional<VhcMargenUtilidad> findByIdMargenUtilidad(byte[] idMargenUtilidad);

    /**
     * Verifica si existen registros para un periodo específico
     */
    boolean existsByPeriodoAnioAndPeriodoMesAndPeriodoDia(
            Integer periodoAnio, Short periodoMes, Short periodoDia);

    /**
     * Cuenta cuántos registros existen para un modelo en un periodo
     */
    int countByBodyModelCposAndPeriodoAnioAndPeriodoMes(
            String bodyModelCpos, Integer periodoAnio, Short periodoMes);
}
