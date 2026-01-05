package com.organizame.reportes.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestRanking {

    @NonNull
    @Min(value = 1, message = "Los meses del reporte deben ser al menos 1")
    @Max(value = 12, message = "Los meses del reporte no puede ser mas de 12")
    private Integer mes; // mes solicitado

    @Min(value = 2024, message = "Los meses del reporte deben ser al menos 2024")
    @Max(value = 2100, message = "Los meses del reporte no puede ser mas de 2100")
    private Integer anio; // anio solicitado

}
