package com.organizame.reportes.dto;

import com.organizame.reportes.dto.auxiliar.ResumenHelp;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DaoResumenPeriodo {
    private String modelo;
    private Integer cantidad;
    private String mesAnio;
    private String marca;
    private String fabricante;
    private String segmento;

    public ResumenHelp getResumeHelp(){
        return new ResumenHelp(this.cantidad, this.fabricante);
    }
}
