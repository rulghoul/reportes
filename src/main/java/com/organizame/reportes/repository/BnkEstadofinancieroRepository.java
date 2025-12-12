package com.organizame.reportes.repository;

import com.organizame.reportes.persistence.entities.BnkEstadofinanciero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BnkEstadofinancieroRepository extends JpaRepository<BnkEstadofinanciero, byte[]> {

    @Query("""
                select b
                from BnkEstadofinanciero b
                where  (b.periodoanio * 100 + b.periodomes) between :desde and :hasta
            """)
    List<BnkEstadofinanciero> findEstadoFinancieroPorFechas(
            @Param("desde") int desde,
            @Param("hasta") int hasta
    );
}
