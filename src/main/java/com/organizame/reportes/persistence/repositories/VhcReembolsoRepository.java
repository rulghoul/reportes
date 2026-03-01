package com.organizame.reportes.persistence.repositories;

import com.organizame.reportes.persistence.entities.VhcAnio;
import com.organizame.reportes.persistence.entities.VhcReembolso;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

public interface VhcReembolsoRepository  extends PagingAndSortingRepository<VhcReembolso, byte[]> {


    @Query("select v from VhcReembolso v where v.vhcanio in ?1")
    List<VhcReembolso> getBoletinRembolso(Collection<VhcAnio> vhcanios);
}
