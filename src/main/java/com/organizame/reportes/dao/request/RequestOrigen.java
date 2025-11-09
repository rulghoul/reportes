package com.organizame.reportes.dao.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestOrigen {

    private String origen;
    private String segmento;
    private LocalDate mesFinal;
    private Integer mesReporte;
    private String resultado; // json, excel ,pptx

}
