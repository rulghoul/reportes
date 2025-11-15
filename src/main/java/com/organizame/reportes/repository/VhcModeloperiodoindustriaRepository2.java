package com.organizame.reportes.repository;


import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VhcModeloperiodoindustriaRepository2 extends JpaRepository<VhcModeloperiodoindustria, byte[]> {

    @Query("select v from VhcModeloperiodoindustria v where v.origenarchivo = ?1")
    Set<VhcModeloperiodoindustria> findByOrigenarchivo(String origenarchivo);

    @Query("""
                select v 
                from VhcModeloperiodoindustria v
                where v.origenarchivo = :origen
                  and (v.periodoanio * 100  + v.periodomes) between :desde and :hasta
                order by v.periodoanio, v.periodomes
            """)
    List<VhcModeloperiodoindustria> findUltimosMeses(
            @Param("origen") String origen,
            @Param("desde") int desde,
            @Param("hasta") int hasta
    );

    @Query("""
                select sum(v.cantidad) 
                from VhcModeloperiodoindustria v
                where v.origenarchivo = :origen
                  and (v.periodoanio * 100 + v.periodomes) between :desde and :hasta
            """)
    Optional<Integer> findSumaTotalCantidadPorFiltros(
            @Param("origen") String origen,
            @Param("desde") int desde,
            @Param("hasta") int hasta
    );

    @Query("""
                select sum(v.cantidad) 
                from VhcModeloperiodoindustria v
                where  (v.periodoanio * 100 + v.periodomes) between :desde and :hasta
            """)
    Optional<Integer> findSumaTotalCantidadGlobalPorFechas(
            @Param("desde") int desde,
            @Param("hasta") int hasta
    );


    @Query("""
                select v.periodoanio, v.periodomes, sum(v.cantidad)
                from VhcModeloperiodoindustria v
                where (v.periodoanio * 100 + v.periodomes) between :desde and :hasta
                group by v.periodoanio, v.periodomes
                order by v.periodoanio, v.periodomes
            """)
    List<Object[]> findSumaCantidadPorAnioMesGlobal(
            @Param("desde") int desde,
            @Param("hasta") int hasta
    );

    @Query("select distinct v.origenarchivo from VhcModeloperiodoindustria v order by v.origenarchivo")
    List<String> findDistinctByOrderByOrigenarchivoAsc();

    @Query("select distinct v.segmentoarchivo from VhcModeloperiodoindustria v order by v.segmentoarchivo")
    List<String> findDistinctByOrderBySegmentoarchivoAsc();
}