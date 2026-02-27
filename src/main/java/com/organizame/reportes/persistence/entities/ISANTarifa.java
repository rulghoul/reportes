package com.organizame.reportes.persistence.entities;

import com.organizame.reportes.dto.Response.ISANTarifaDto;
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
@Table(name="vhc_isantarifa", catalog="adistemdb" )
public class ISANTarifa implements Serializable {

    @Serial
    private static final long serialVersionUID = 7525502835743716929L;

    //--- ENTITY PRIMARY KEY
    @Id
    @Lob
    @Column(name="IDISANTARIFA", nullable=false)
    private byte[]  id ;

    @Column(name="FECHAINICIO")
    private LocalDateTime fechaInicio ;

    @Column(name = "LIMITEINFERIOR", nullable = false)
    private BigDecimal limiteInferior;

    @Column(name = "LIMITESUPERIOR", nullable = false)
    private BigDecimal limiteSuperior;

    @Column(name = "CUOTAFIJA", nullable = false)
    private BigDecimal cuotaFija;

    @Column(name = "PORCENTAJE", nullable = false)
    private BigDecimal porcentaje;

    public ISANTarifaDto toDto(){
        return new ISANTarifaDto(fechaInicio, limiteInferior, limiteSuperior, cuotaFija, porcentaje);
    }
}
