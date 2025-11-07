package com.organizame.reportes.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HojaExcel {
    private String nombreHoja;
    private List<TablaExcel> tablas;
}