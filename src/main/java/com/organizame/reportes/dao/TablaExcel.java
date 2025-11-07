package com.organizame.reportes.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TablaExcel {
    private String nombreTabla;
    private List<List<Object>> datos;
}