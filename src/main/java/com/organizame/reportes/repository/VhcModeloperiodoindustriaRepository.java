package com.organizame.reportes.repository;

import com.organizame.reportes.entity.VhcModeloperiodoindustria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface VhcModeloperiodoindustriaRepository extends JpaRepository<VhcModeloperiodoindustria, byte[]> {

    @Query("select v from VhcModeloperiodoindustria v where v.origenarchivo = ?1")
    Set<VhcModeloperiodoindustria> findByOrigenarchivo(String origenarchivo);

    @Query("""
            select v from VhcModeloperiodoindustria v
            where v.origenarchivo = ?1 and v.periodoanio = ?2 and v.periodomes = ?3""")
    List<VhcModeloperiodoindustria> findByOrigenarchivoAndPeriodoanioAndPeriodomes(String origenarchivo, int periodoanio, int periodomes);

    @Query("select distinct v from VhcModeloperiodoindustria v order by v.origenarchivo")
    List<String> findDistinctByOrderByOrigenarchivoAsc();
}