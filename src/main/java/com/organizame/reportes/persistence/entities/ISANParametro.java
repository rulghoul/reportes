package com.organizame.reportes.persistence.entities;

import com.organizame.reportes.dto.Response.ISANParametrosDto;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name="vhc_isanparametro", catalog="adistemdb" )
public class ISANParametro implements Serializable {

    @Serial
    private static final long serialVersionUID = 7525502835743716929L;

    //--- ENTITY PRIMARY KEY
    @Id
    @Lob
    @Column(name="IDISANPARAMETRO", nullable=false)
    private byte[]     id ;

    @Column(name="FECHAINICIO")
    private LocalDateTime fechaInicio ;

    @Column(name = "LIMITESUPERIORTASACERO", nullable = false)
    private BigDecimal limiteSuperiorTasaCero;

    @Column(name = "LIMITESUPERIORTASACINCUENTA", nullable = false)
    private BigDecimal limiteSuperiorTasaCincuenta;

    @Column(name = "IVA", nullable = false)
    private BigDecimal iva;

    @Column(name = "EXCESO", nullable = false)
    private BigDecimal exceso;

    @Column(name = "TASAREDUCIR", nullable = false)
    private BigDecimal tasareducir;

    public ISANParametrosDto toDto(){
        return new ISANParametrosDto(fechaInicio, limiteSuperiorTasaCero,
                limiteSuperiorTasaCincuenta, iva, exceso, tasareducir);
    }

}
