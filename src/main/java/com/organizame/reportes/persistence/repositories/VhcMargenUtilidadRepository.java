package com.organizame.reportes.persistence.repositories;

import com.organizame.reportes.persistence.entities.VhcAnio;
import com.organizame.reportes.persistence.entities.VhcMargenUtilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VhcMargenUtilidadRepository extends JpaRepository<VhcMargenUtilidad, byte[]> {

    boolean existsByVhcAnioAndPeriodoAnioAndPeriodoMesAndPeriodoDia(VhcAnio vhcAnio, Integer periodoAnio, Integer periodoMes, Integer periodoDia);
}
