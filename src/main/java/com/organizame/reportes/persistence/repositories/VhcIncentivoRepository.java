package com.organizame.reportes.persistence.repositories;

import com.organizame.reportes.persistence.entities.VhcAnio;
import com.organizame.reportes.persistence.entities.VhcIncentivo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

public interface VhcIncentivoRepository  extends PagingAndSortingRepository<VhcIncentivo, byte[]> {
    @Query("select v from VhcIncentivo v where v.vhcanio in ?1")
    List<VhcIncentivo> getBoletinIncentivo(Collection<VhcAnio> vhcanios);
}
