package com.organizame.reportes.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestMargen {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

}
