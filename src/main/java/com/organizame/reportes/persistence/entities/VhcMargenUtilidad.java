package com.organizame.reportes.persistence.entities;

import com.organizame.reportes.dto.Response.MargenUtilidadDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@Table(name = "vhc_margenutilidad", indexes = {
        @Index(name = "VHC_MARGENUTILIDAD_IDX_IDANIO", columnList = "IDANIO"),
        @Index(name = "VHC_MARGENUTILIDAD_IDX_PERIODOS", columnList = "IDANIO, PERIODOANIO, PERIODOMES, PERIODODIA")
})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VhcMargenUtilidad implements Serializable, Persistable<byte[]> {

    @Serial
    private static final long serialVersionUID = 6358816122783250687L;

    @Id
    @Column(name = "IDMARGENUTILIDAD", columnDefinition = "BINARY(16)", length = 16)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private byte[] idMargenUtilidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDANIO", referencedColumnName = "IDANIO", nullable = false,
            foreignKey = @ForeignKey(name = "VHC_MARGENUTILIDAD_FK_IDANIO"))
    @EqualsAndHashCode.Include
    private VhcAnio vhcAnio;

    @Column(name = "PERIODOANIO", nullable = false)
    @EqualsAndHashCode.Include
    private Integer periodoAnio;

    @Column(name = "PERIODOMES", nullable = false)
    @EqualsAndHashCode.Include
    private Integer periodoMes;

    @Column(name = "PERIODODIA", nullable = false)
    @EqualsAndHashCode.Include
    private Integer periodoDia;

    @Column(name = "BODYMODELCPOS", nullable = false, length = 100)
    private String bodyModelCpos;

    @Column(name = "VERSIONARCHIVO", length = 50)
    private String versionArchivo;

    @Column(name = "PRECIOLISTA", nullable = false, precision = 65, scale = 2)
    private BigDecimal precioLista = BigDecimal.ZERO;

    @Column(name = "PRECIOCREDITO", nullable = false, precision = 65, scale = 2)
    private BigDecimal precioCredito = BigDecimal.ZERO;

    @Column(name = "PRECIOCONTADO", nullable = false, precision = 65, scale = 2)
    private BigDecimal precioContado = BigDecimal.ZERO;

    @Column(name = "PRECIOLISTAUTILIDADSINIMPUESTOMONTO", nullable = false, precision = 65, scale = 2)
    private BigDecimal precioListaUtilidadSinImpuestoMonto = BigDecimal.ZERO;

    @Column(name = "PRECIOLISTAUTILIDADSINIMPUESTOPORC", nullable = false, precision = 65, scale = 2)
    private BigDecimal precioListaUtilidadSinImpuestoPorc = BigDecimal.ZERO;

    @Column(name = "PRECIOCREDITOUTILIDADSINIMPUESTOMONTO", nullable = false, precision = 65, scale = 2)
    private BigDecimal precioCreditoUtilidadSinImpuestoMonto = BigDecimal.ZERO;

    @Column(name = "PRECIOCREDITOUTILIDADSINIMPUESTOPORC", nullable = false, precision = 65, scale = 2)
    private BigDecimal precioCreditoUtilidadSinImpuestoPorc = BigDecimal.ZERO;

    @Column(name = "PRECIOCONTADOUTILIDADSINIMPUESTOMONTO", nullable = false, precision = 65, scale = 2)
    private BigDecimal precioContadoUtilidadSinImpuestoMonto = BigDecimal.ZERO;

    @Column(name = "PRECIOCONTADOUTILIDADSINIMPUESTOPORC", nullable = false, precision = 65, scale = 2)
    private BigDecimal precioContadoUtilidadSinImpuestoPorc = BigDecimal.ZERO;

    @Column(name = "FECHACALCULO", nullable = false)
    private LocalDate fechaCalculo;

    @PrePersist
    private void generateId() {
        if (this.idMargenUtilidad == null) {
            this.idMargenUtilidad = toBytes(UUID.randomUUID());
        }else{
            persisted = true;
        }
    }

    @PostLoad
    @PostPersist
    public void markAsPersisted() {
        this.persisted = true;
    }

    @Override
    public byte[] getId() {
        return idMargenUtilidad;
    }

    @Transient
    private boolean persisted = false;

    @Override
    @Transient // 👈 ¡Crítico! No mapear este método como columna
    public boolean isNew() {
        return !persisted;
    }

    private byte[] toBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (mostSigBits >> (8 * (7 - i)));
            bytes[8 + i] = (byte) (leastSigBits >> (8 * (7 - i)));
        }
        return bytes;
    }

    public UUID getUuid() {
        return toUUID(this.idMargenUtilidad);
    }

    private UUID toUUID(byte[] bytes) {
        if (bytes == null || bytes.length != 16) return null;
        long mostSigBits = 0;
        long leastSigBits = 0;
        for (int i = 0; i < 8; i++) mostSigBits = (mostSigBits << 8) | (bytes[i] & 0xff);
        for (int i = 8; i < 16; i++) leastSigBits = (leastSigBits << 8) | (bytes[i] & 0xff);
        return new UUID(mostSigBits, leastSigBits);
    }

    public MargenUtilidadDto toDto(){
        return new MargenUtilidadDto(this.toUUID(vhcAnio.getIdanio()).toString(), periodoAnio, periodoMes, periodoDia, bodyModelCpos,
                versionArchivo, precioLista, precioCredito, precioContado,
                precioListaUtilidadSinImpuestoMonto, precioCreditoUtilidadSinImpuestoMonto, precioContadoUtilidadSinImpuestoMonto,
                precioListaUtilidadSinImpuestoPorc, precioCreditoUtilidadSinImpuestoPorc, precioContadoUtilidadSinImpuestoPorc,LocalDate.now());
    }
}
