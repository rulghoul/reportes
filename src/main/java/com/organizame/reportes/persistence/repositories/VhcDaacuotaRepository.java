package com.organizame.reportes.persistence.repositories;

import com.organizame.reportes.persistence.entities.VhcAnio;
import com.organizame.reportes.persistence.entities.VhcDaacuota;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

public interface VhcDaacuotaRepository  extends PagingAndSortingRepository<VhcDaacuota, byte[]> {


    @Query("select v from VhcDaacuota v where v.vhcanio in ?1")
    List<VhcDaacuota> getCuotaBoletin(Collection<VhcAnio> vhcanios);
}
