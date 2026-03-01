package com.organizame.reportes.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ISANTarifaDto {

    private LocalDateTime fechaInicio ;

    private BigDecimal limiteInferior;

    private BigDecimal limiteSuperior;

    private BigDecimal cuotaFija;

    private BigDecimal porcentaje;
}
