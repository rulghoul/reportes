package com.organizame.reportes.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstructuraExcel {
    private String nombreExcel;
    private List<HojaExcel> hojas;
}