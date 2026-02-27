package com.organizame.reportes.persistence.repositories;

import com.organizame.reportes.persistence.entities.VhcBoletinprecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VhcBoletinprecioRepository extends JpaRepository<VhcBoletinprecio, byte[]> {

    @Query("select v from VhcBoletinprecio v where v.fechainicio between ?1 and ?2")
    List<VhcBoletinprecio> boletinFromPeriodo(LocalDateTime fechainicioStart, LocalDateTime fechainicioEnd);

    @Query("select v from VhcBoletinprecio v where v.fechainicio <= ?1 and v.aniomyarchivo like ?2")
    List<VhcBoletinprecio> lastedBoletin(LocalDateTime fechainicioStart, String year);
}
