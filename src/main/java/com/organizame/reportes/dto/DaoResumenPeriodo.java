package com.organizame.reportes.dto;

import com.organizame.reportes.dto.auxiliar.ResumenHelp;
import com.organizame.reportes.persistence.entities.VhcFabricante;
import com.organizame.reportes.persistence.entities.VhcModelo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DaoResumenPeriodo {
    private VhcModelo idModelo;
    private String modelo;
    private Integer cantidad;
    private String mesAnio;
    private LocalDate mesDate;
    private VhcFabricante idMarca;
    private String marca;
    private String fabricante;
    private String segmento;

    public ResumenHelp getResumeHelp(){
        return new ResumenHelp(this.cantidad, this.fabricante);
    }
}
