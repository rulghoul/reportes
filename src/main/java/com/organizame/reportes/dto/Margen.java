package com.organizame.reportes.dto;

import com.organizame.reportes.utils.excel.dto.Celda;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Margen {
    private String marca;
    private String modelo;
    private Integer inventarioAnterior;
    private Integer inventarioActual;
    private Integer menudeoFlotilla;
    private Integer inventarioCierre;

    public List<Celda> toCeldas(String estilo){
        List<Celda> celdas = new ArrayList<>();
        celdas.add(new Celda(this.modelo, estilo, 1));
        celdas.add(new Celda("", "Limpio", 1));
        if (Objects.isNull(inventarioAnterior) || inventarioAnterior.equals(0)){
            celdas.add(new Celda("-", estilo, 1));
        }else{
            celdas.add(new Celda(inventarioAnterior, estilo, 1));
        }
        celdas.add(new Celda(inventarioActual, estilo, 1));
        celdas.add(new Celda("", "Limpio", 1));
        var cierre = inventarioCierre.equals(0) ? "" : inventarioCierre;
        celdas.add(new Celda(cierre, estilo, 1));
        celdas.add(new Celda(menudeoFlotilla, "Limpio", 1));
        return celdas;
    }

    public Margen sumarConMarca(Margen otro){
        var resultado = new Margen();
        resultado.setMarca(this.marca);
        resultado.setModelo(this.marca);
        resultado.setInventarioAnterior(this.inventarioAnterior + otro.inventarioAnterior);
        resultado.setInventarioActual(this.inventarioActual + otro.inventarioActual);
        resultado.setMenudeoFlotilla(this.menudeoFlotilla + otro.menudeoFlotilla);
        var cierre = resultado.menudeoFlotilla.equals(0) ? 0
                : resultado.inventarioActual / resultado.menudeoFlotilla * 30;
        resultado.setInventarioCierre(cierre);
        return resultado;
    }
}
