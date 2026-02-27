package com.organizame.reportes.dto.auxiliar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalculoISAN {
    private BigDecimal monto;
    private Integer anio;
    private BigDecimal isanCincuenta;   // ISAN con 50% de descuento (si aplica)
    private BigDecimal isan;            // ISAN completo calculado
    private String message;        // Estado o descripción del resultado
}
