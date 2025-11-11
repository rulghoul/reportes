package com.organizame.reportes.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilaTabla {
    private String nombreEstilo;
    private List<Object> fila;
}