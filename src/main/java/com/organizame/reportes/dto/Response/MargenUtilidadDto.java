package com.organizame.reportes.dto.Response;


import com.organizame.reportes.persistence.entities.VhcMargenUtilidad;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MargenUtilidadDto {

    // El ID se genera automáticamente, no se recibe del cliente

    @NotNull(message = "El ID del año es requerido")
    private String idAnio; // UUID en formato String

    @NotNull(message = "El año del periodo es requerido")
    @Min(value = 1900, message = "Año inválido")
    @Max(value = 2100, message = "Año inválido")
    private Integer periodoAnio;

    @NotNull(message = "El mes del periodo es requerido")
    @Min(value = 1, message = "Mes debe estar entre 1 y 12")
    @Max(value = 12, message = "Mes debe estar entre 1 y 12")
    private Integer periodoMes;

    @NotNull(message = "El día del periodo es requerido")
    @Min(value = 1, message = "Día debe estar entre 1 y 31")
    @Max(value = 31, message = "Día debe estar entre 1 y 31")
    private Integer periodoDia;

    @NotBlank(message = "El modelo es requerido")
    @Size(max = 100, message = "El modelo no puede exceder 100 caracteres")
    private String bodyModelCpos;

    @Size(max = 50, message = "La versión no puede exceder 50 caracteres")
    private String versionArchivo;

    @NotNull(message = "El precio de lista es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal precioLista;

    @NotNull(message = "El precio a crédito es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal precioCredito;

    @NotNull(message = "El precio de contado es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal precioContado;

    // Utilidades Monto
    private BigDecimal precioListaUtilidadSinImpuestoMonto = BigDecimal.ZERO;
    private BigDecimal precioCreditoUtilidadSinImpuestoMonto = BigDecimal.ZERO;
    private BigDecimal precioContadoUtilidadSinImpuestoMonto = BigDecimal.ZERO;

    // Utilidades Porcentaje
    private BigDecimal precioListaUtilidadSinImpuestoPorc = BigDecimal.ZERO;
    private BigDecimal precioCreditoUtilidadSinImpuestoPorc = BigDecimal.ZERO;
    private BigDecimal precioContadoUtilidadSinImpuestoPorc = BigDecimal.ZERO;

    private LocalDate fechaCalculo;

    // ==================== CONVERSIÓN A ENTITY ====================

    public VhcMargenUtilidad toEntity() {
        return VhcMargenUtilidad.builder()
                //.vhcAnio(uuidStringToBytes(this.))
                .periodoAnio(this.periodoAnio)
                .periodoMes(this.periodoMes)
                .periodoDia(this.periodoDia)
                .bodyModelCpos(this.bodyModelCpos)
                .versionArchivo(this.versionArchivo)
                .precioLista(this.precioLista != null ? this.precioLista : BigDecimal.ZERO)
                .precioCredito(this.precioCredito != null ? this.precioCredito : BigDecimal.ZERO)
                .precioContado(this.precioContado != null ? this.precioContado : BigDecimal.ZERO)
                .precioListaUtilidadSinImpuestoMonto(
                        this.precioListaUtilidadSinImpuestoMonto != null ?
                                this.precioListaUtilidadSinImpuestoMonto : BigDecimal.ZERO)
                .precioListaUtilidadSinImpuestoPorc(
                        this.precioListaUtilidadSinImpuestoPorc != null ?
                                this.precioListaUtilidadSinImpuestoPorc : BigDecimal.ZERO)
                .precioCreditoUtilidadSinImpuestoMonto(
                        this.precioCreditoUtilidadSinImpuestoMonto != null ?
                                this.precioCreditoUtilidadSinImpuestoMonto : BigDecimal.ZERO)
                .precioCreditoUtilidadSinImpuestoPorc(
                        this.precioCreditoUtilidadSinImpuestoPorc != null ?
                                this.precioCreditoUtilidadSinImpuestoPorc : BigDecimal.ZERO)
                .precioContadoUtilidadSinImpuestoMonto(
                        this.precioContadoUtilidadSinImpuestoMonto != null ?
                                this.precioContadoUtilidadSinImpuestoMonto : BigDecimal.ZERO)
                .precioContadoUtilidadSinImpuestoPorc(
                        this.precioContadoUtilidadSinImpuestoPorc != null ?
                                this.precioContadoUtilidadSinImpuestoPorc : BigDecimal.ZERO)
                .fechaCalculo(this.fechaCalculo != null ? this.fechaCalculo : LocalDate.now())
                .build();
    }

    // Utilidad para convertir String UUID a byte[]
    private byte[] uuidStringToBytes(String uuid) {
        if (uuid == null) return null;
        String clean = uuid.replace("-", "");
        if (clean.length() != 32) {
            throw new IllegalArgumentException("UUID inválido: debe tener 32 caracteres hexadecimales");
        }
        byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            bytes[i] = (byte) Integer.parseInt(clean.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
