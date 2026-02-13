package com.organizame.reportes.dto;

import com.organizame.reportes.persistence.entities.*;
import com.organizame.reportes.utils.Constantes;
import com.organizame.reportes.utils.excel.dto.Celda;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class MargenUtilidad {
    // Campos fuente (valores directos de las entidades)
    private String versionArchivo;
    private BigDecimal distribuidorTotal;
    private BigDecimal distribuidorGastosSubtotal;
    private BigDecimal daacuota;
    private BigDecimal precioLista;
    private BigDecimal financiamientoPrecioPromocional;
    private BigDecimal ofertaPrincipalReembolso;
    private BigDecimal contadoPrecio;
    private BigDecimal contadoReembolso;
    private BigDecimal distribuidorIva;
    private BigDecimal sumatoriaDistribuidorGastos;
    private BigDecimal cuotaTraslado;
    private BigDecimal programaPrimeroCliente;
    private BigDecimal seguroTraslado;
    private BigDecimal contadoDescuento;

    public MargenUtilidad(VhcBoletinprecio boletinprecio,
                          VhcBoletinpreciogasto boletinpreciogasto,
                          VhcDaacuota daacuotaEntity,
                          VhcIncentivo incentivo,
                          VhcReembolso reembolso) {

        // VhcBoletinprecio
        this.versionArchivo = boletinprecio != null ? boletinprecio.getVersionarchivo() : null; //
        this.distribuidorTotal = boletinprecio != null ? boletinprecio.getDistribuidortotal() : null; //
        this.distribuidorGastosSubtotal = boletinprecio != null ? boletinprecio.getDistribuidorgastosubtotal() : null; //
        this.distribuidorIva = boletinprecio != null ? boletinprecio.getDistribuidoriva() : null; //

        // VhcDaacuota
        this.daacuota = daacuotaEntity != null ? daacuotaEntity.getDaacuota() : null; //

        // VhcIncentivo
        this.financiamientoPrecioPromocional = incentivo != null ? incentivo.getFinanciamientoalternopreciopromocional() : null;
        this.contadoPrecio = incentivo != null ? incentivo.getPreciolista() : null;

        // VhcReembolso
        this.precioLista = reembolso != null ? reembolso.getPreciolista() : null;
        this.ofertaPrincipalReembolso = reembolso != null ? reembolso.getOfertaprincipalreembolso() : null;
        this.contadoReembolso = reembolso != null ? reembolso.getContadoreembolso() : null;
        this.contadoDescuento = reembolso != null ? reembolso.getContadodescuento() : null;

        // VhcBoletinpreciogasto - Sumatoria y valores específicos

            this.sumatoriaDistribuidorGastos = null;
            this.cuotaTraslado = null;
            this.programaPrimeroCliente = null;
            this.seguroTraslado = null;

    }

    // Método para convertir a lista de objetos con manejo de nulos y cálculos
    public List<Celda> toList() {
        List<Celda> result = new ArrayList<>();

        // Cálculos intermedios con manejo de nulos
        BigDecimal e = !Objects.isNull(distribuidorTotal)  && !Objects.isNull(daacuota)
                ? BigDecimal.ONE.subtract( this.safeDivide(distribuidorTotal.subtract(daacuota) , distribuidorGastosSubtotal) )
                : null;

        BigDecimal g = !Objects.isNull(distribuidorGastosSubtotal) && !Objects.isNull(distribuidorTotal)  && !Objects.isNull(daacuota)
                ? distribuidorGastosSubtotal.subtract(distribuidorTotal).add(daacuota) : null;

        BigDecimal h = safeDivide(g, distribuidorGastosSubtotal);

        BigDecimal r = !Objects.isNull(precioLista) && !Objects.isNull(financiamientoPrecioPromocional)
                ? precioLista.subtract(financiamientoPrecioPromocional) : null;

        BigDecimal u = safeDivide(precioLista, Constantes.IVA);

        BigDecimal z = !Objects.isNull(distribuidorGastosSubtotal)  && !Objects.isNull(distribuidorTotal) && !Objects.isNull(daacuota )
                ? distribuidorGastosSubtotal.subtract(distribuidorTotal).add(daacuota) : null;

        BigDecimal aa = safeDivide(z, distribuidorGastosSubtotal);

        BigDecimal ac = z;
        BigDecimal ad = safeDivide(ac, distribuidorGastosSubtotal);
        BigDecimal ae = (g != null && ac != null) ? g.subtract(ac)  : null;
        BigDecimal af = r;

        BigDecimal ah = safeDivide(financiamientoPrecioPromocional, Constantes.IVA);

        BigDecimal ao = !Objects.isNull(distribuidorTotal) && !Objects.isNull(daacuota)
                ? distribuidorTotal.subtract(daacuota)  : null;

        BigDecimal ap = !Objects.isNull(contadoReembolso) ? contadoReembolso.negate() : null;
        BigDecimal aq = safeDivide(ap, Constantes.IVA);
        BigDecimal ar = (ao != null && aq != null) ? ao.add(aq)  : null;

        BigDecimal au = !Objects.isNull(distribuidorGastosSubtotal) && !Objects.isNull(ar)
                ? distribuidorGastosSubtotal.subtract(ar) : null;

        BigDecimal av = safeDivide(au, distribuidorGastosSubtotal);
        BigDecimal ax = au;
        BigDecimal ay = safeDivide(ax, distribuidorGastosSubtotal);
        BigDecimal az = !Objects.isNull(ap) ? ap.negate() : null;

        BigDecimal bb = !Objects.isNull(h) && !Objects.isNull(ay) ?
                h.subtract(ay)  : null;
        BigDecimal bc = !Objects.isNull(distribuidorTotal) && !Objects.isNull(bb)
                ? distribuidorTotal.multiply(bb)  : null;
        BigDecimal bd = !Objects.isNull(ax)  && !Objects.isNull(bc)
                ? ax.add(bc)  : null;
        BigDecimal be = safeDivide(bd, distribuidorGastosSubtotal);

        BigDecimal bi = Stream.of(cuotaTraslado, programaPrimeroCliente, seguroTraslado)
                .filter(Objects::nonNull)
                .reduce( BigDecimal::add).orElse(BigDecimal.ZERO);

        BigDecimal bj = !Objects.isNull(sumatoriaDistribuidorGastos)
                ? bi.subtract(sumatoriaDistribuidorGastos) : null;

        BigDecimal bm = !Objects.isNull(precioLista)
                ? precioLista.subtract(precioLista) : null; // Siempre 0

        BigDecimal bo = !Objects.isNull(sumatoriaDistribuidorGastos) && !Objects.isNull(bi)
                ? sumatoriaDistribuidorGastos.subtract(bi)  : null;

        BigDecimal bq = !Objects.isNull(financiamientoPrecioPromocional != null && financiamientoPrecioPromocional != null)
                ? financiamientoPrecioPromocional.subtract(financiamientoPrecioPromocional) : null; // Siempre 0

        BigDecimal bs = !Objects.isNull(distribuidorGastosSubtotal) && !Objects.isNull(distribuidorTotal)
                ? distribuidorGastosSubtotal.multiply(new BigDecimal("0.89")).subtract(distribuidorTotal) : null;

        BigDecimal bt = daacuota;
        BigDecimal bu = !Objects.isNull(bs) && !Objects.isNull(bt)
                ? bs.subtract(bt) : null;

        BigDecimal bz = safeDivide(contadoPrecio, Constantes.IVA);
        BigDecimal ca = !Objects.isNull(bz)
                ? bz.multiply(new BigDecimal("0.16"))  : null;

        BigDecimal cg = !Objects.isNull(distribuidorTotal) && !Objects.isNull(daacuota)
                ? distribuidorTotal.subtract(daacuota)  : null;

        BigDecimal ch = !Objects.isNull(contadoReembolso)
                ? contadoReembolso.negate() : null;
        BigDecimal ci = safeDivide(ch, Constantes.IVA);
        BigDecimal cj = !Objects.isNull(cg) && !Objects.isNull(ci)
                ? cg.add(ci) : null;

        BigDecimal cm = !Objects.isNull(distribuidorGastosSubtotal) && !Objects.isNull(cj)
                ? distribuidorGastosSubtotal.subtract(cj) : null;

        BigDecimal cn = safeDivide(cm, distribuidorGastosSubtotal);
        BigDecimal cp = cm;
        BigDecimal cq = safeDivide(cp, distribuidorGastosSubtotal);

        // Construir lista en orden A → CR (78 elementos)
        result.add(new Celda(versionArchivo, "normal", 1)); // A
        result.add(new Celda(distribuidorTotal, "normal", 1)); // B
        result.add(new Celda(distribuidorGastosSubtotal, "normal", 1)); // C
        result.add(new Celda(daacuota, "normal", 1)); // D
        result.add(new Celda(e, "normal", 1)); // E
        result.add(new Celda(precioLista, "normal", 1)); // F
        result.add(new Celda(g, "normal", 1)); // G
        result.add(new Celda(h, "normal", 1)); // H
        result.add(new Celda(financiamientoPrecioPromocional, "normal", 1)); // I
        result.add(new Celda(ax, "normal", 1)); // J
        result.add(new Celda(ay, "normal", 1)); // K
        result.add(new Celda(ofertaPrincipalReembolso, "normal", 1)); // L
        result.add(new Celda(contadoPrecio, "normal", 1)); // M
        result.add(new Celda(cp, "normal", 1)); // N
        result.add(new Celda(cq, "normal", 1)); // O
        result.add(new Celda(contadoReembolso, "normal", 1)); // P
        result.add(new Celda("", "normal", 1)); // Q (Espacio)
        result.add(new Celda(r, "normal", 1)); // R
        result.add(new Celda("", "normal", 1)); // S (Espacio)
        result.add(new Celda(precioLista, "normal", 1)); // T
        result.add(new Celda(u, "normal", 1)); // U
        result.add(new Celda(distribuidorIva, "normal", 1)); // V
        result.add(new Celda("", "normal", 1)); // W (CALCULO ISAN - pendiente implementación)
        result.add(new Celda(sumatoriaDistribuidorGastos, "normal", 1)); // X
        result.add(new Celda(distribuidorGastosSubtotal, "normal", 1)); // Y
        result.add(new Celda(z, "normal", 1)); // Z
        result.add(new Celda(aa, "normal", 1)); // AA
        result.add(new Celda(precioLista, "normal", 1)); // AB //Calcular
        result.add(new Celda(ac, "normal", 1)); // AC
        result.add(new Celda(ad, "normal", 1)); // AD
        result.add(new Celda(ae, "normal", 1)); // AE
        result.add(new Celda(af, "normal", 1)); // AF
        result.add(new Celda(financiamientoPrecioPromocional, "normal", 1)); // AG
        result.add(new Celda(ah, "normal", 1)); // AH
        result.add(new Celda(distribuidorIva, "normal", 1)); // AI
        result.add(new Celda("", "normal", 1)); // AJ (CALCULO ISAN - pendiente implementación)
        result.add(new Celda(sumatoriaDistribuidorGastos, "normal", 1)); // AK Calcular
        result.add(new Celda(distribuidorGastosSubtotal, "normal", 1)); // AL //Sin valor en ell campo
        result.add(new Celda(distribuidorTotal, "normal", 1)); // AM
        result.add(new Celda(daacuota, "normal", 1)); // AN
        result.add(new Celda(ao, "normal", 1)); // AO
        result.add(new Celda(ap, "normal", 1)); // AP
        result.add(new Celda(aq, "normal", 1)); // AQ
        result.add(new Celda(ar, "normal", 1)); // AR
        result.add(new Celda(distribuidorGastosSubtotal, "normal", 1)); // AS
        result.add(new Celda(ar, "normal", 1)); // AT
        result.add(new Celda(au, "normal", 1)); // AU
        result.add(new Celda(av, "normal", 1)); // AV
        result.add(new Celda(financiamientoPrecioPromocional, "normal", 1)); // AW
        result.add(new Celda(ax, "normal", 1)); // AX
        result.add(new Celda(ay, "normal", 1)); // AY
        result.add(new Celda(az, "normal", 1)); // AZ
        result.add(new Celda(null, "normal", 1)); // BA (null según especificación)
        result.add(new Celda(bb, "normal", 1)); // BB
        result.add(new Celda(bc, "normal", 1)); // BC
        result.add(new Celda(bd, "normal", 1)); // BD
        result.add(new Celda(be, "normal", 1)); // BE
        result.add(new Celda(cuotaTraslado, "normal", 1)); // BF clave
        result.add(new Celda(programaPrimeroCliente, "normal", 1)); // BG Clave
        result.add(new Celda(seguroTraslado, "normal", 1)); // BH Clave
        result.add(new Celda(bi, "normal", 1)); // BI
        result.add(new Celda(bj, "normal", 1)); // BJ
        result.add(new Celda("", "normal", 1)); // BK (Espacio)
        result.add(new Celda(precioLista, "normal", 1)); // BL
        result.add(new Celda(bm, "normal", 1)); // BM
        result.add(new Celda("", "normal", 1)); // BN (Espacio)
        result.add(new Celda(bo, "normal", 1)); // BO
        result.add(new Celda(financiamientoPrecioPromocional, "normal", 1)); // BP
        result.add(new Celda(bq, "normal", 1)); // BQ
        result.add(new Celda("", "normal", 1)); // BR (Espacio)
        result.add(new Celda(bs, "normal", 1)); // BS
        result.add(new Celda(bt, "normal", 1)); // BT
        result.add(new Celda(bu, "normal", 1)); // BU
        result.add(new Celda("", "normal", 1)); // BV (Espacio)
        result.add(new Celda(contadoDescuento, "normal", 1)); // BW
        result.add(new Celda(contadoReembolso, "normal", 1)); // BX
        result.add(new Celda(contadoPrecio, "normal", 1)); // BY
        result.add(new Celda(bz, "normal", 1)); // BZ
        result.add(new Celda(ca, "normal", 1)); // CA
        result.add(new Celda("", "normal", 1)); // CB (CALCULO ISAN - pendiente implementación)
        result.add(new Celda(sumatoriaDistribuidorGastos, "normal", 1)); // CC //calcular
        result.add(new Celda(distribuidorGastosSubtotal, "normal", 1)); // CD
        result.add(new Celda(distribuidorTotal, "normal", 1)); // CE
        result.add(new Celda(daacuota, "normal", 1)); // CF
        result.add(new Celda(cg, "normal", 1)); // CG
        result.add(new Celda(ch, "normal", 1)); // CH
        result.add(new Celda(ci, "normal", 1)); // CI
        result.add(new Celda(cj, "normal", 1)); // CJ
        result.add(new Celda(distribuidorGastosSubtotal, "normal", 1)); // CK
        result.add(new Celda(cj, "normal", 1)); // CL
        result.add(new Celda(cm, "normal", 1)); // CM
        result.add(new Celda(cn, "normal", 1)); // CN
        result.add(new Celda(contadoPrecio, "normal", 1)); // CO
        result.add(new Celda(cp, "normal", 1)); // CP
        result.add(new Celda(cq, "normal", 1)); // CQ
        result.add(new Celda(ch, "normal", 1)); // CR
        result.add(new Celda(null, "normal", 1)); // Último campo (null según especificación)

        return result;
    }

    private BigDecimal safeDivide(BigDecimal numerador, BigDecimal denominador) {
        if (Objects.isNull(numerador) || Objects.isNull(denominador) || denominador.equals(BigDecimal.ZERO)) return null;
        return numerador.divide( denominador, RoundingMode.HALF_UP);
    }
}
