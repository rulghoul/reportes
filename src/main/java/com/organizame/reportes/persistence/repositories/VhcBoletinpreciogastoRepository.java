package com.organizame.reportes.persistence.repositories;

import com.organizame.reportes.persistence.entities.VhcBoletinprecio;
import com.organizame.reportes.persistence.entities.VhcBoletinpreciogasto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Set;

public interface VhcBoletinpreciogastoRepository  extends PagingAndSortingRepository<VhcBoletinpreciogasto, byte[]> {


    @Query("select v from VhcBoletinpreciogasto v where v.vhcboletinprecio in ?1")
    Set<VhcBoletinpreciogasto> getGastosBoletin(Collection<VhcBoletinprecio> vhcboletinprecios);
}
