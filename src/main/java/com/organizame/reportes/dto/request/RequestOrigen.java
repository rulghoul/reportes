package com.organizame.reportes.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestOrigen {

    private String origen;
    private LocalDate mesFinal;
    private Integer mesReporte;

}
