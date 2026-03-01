package com.organizame.reportes.dto.Response;

import jakarta.persistence.Column;
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
public class ISANParametrosDto {

    private LocalDateTime fechaInicio;

    private BigDecimal limiteSuperiorTasaCero;

    private BigDecimal limiteSuperiorTasaCincuenta;

    private BigDecimal iva;

    private BigDecimal exceso;

    private BigDecimal tasareducir;
}
