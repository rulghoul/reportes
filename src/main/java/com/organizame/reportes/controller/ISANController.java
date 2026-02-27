package com.organizame.reportes.controller;

import com.organizame.reportes.dto.Response.ISANParametrosDto;
import com.organizame.reportes.dto.Response.ISANTarifaDto;
import com.organizame.reportes.dto.auxiliar.CalculoISAN;
import com.organizame.reportes.dto.request.CalculoISANRequest;
import com.organizame.reportes.persistence.entities.ISANParametro;
import com.organizame.reportes.persistence.entities.ISANTarifa;
import com.organizame.reportes.repository.service.ISANService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/isan")
public class ISANController {

    private ISANService service;

    @Autowired
    public ISANController(ISANService service){
        this.service = service;
    }

    @Operation(summary = "Recupera tarifas de ISAN")
    @GetMapping("/parametros")
    public ResponseEntity<List<ISANParametrosDto>> Parametros() {
        return ResponseEntity.ok(service.findAllParametros()
                .stream().map(par -> par.toDto()).toList());
    }

    @Operation(summary = "Recupera Tarifas de ISAN")
    @ApiResponse(responseCode = "200", description = "Lista de parámetros recuperada")
    @GetMapping("/tarifas")
    public ResponseEntity<List<ISANTarifaDto>> Tarifas() {
        return ResponseEntity.ok(service.findAllTarifas()
                .stream().map(tar -> tar.toDto()).toList());
    }

    @Operation(summary = "Limpia cache de valores ISAN")
    @ApiResponse(responseCode = "200", description = "Lista de tarifas recuperada")
    @GetMapping("/limpia")
    public ResponseEntity<Map<String,String>> limpia() {
        service.limpiarCacheISAN();
        return ResponseEntity.ok(Map.of("Mensaje", "Se limpio cahe de ISAN"));
    }

    @Operation(summary = "Calcula el ISAN para un monto y año dados")
    @ApiResponse(responseCode = "200", description = "Cálculo realizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    @PostMapping("/calcular")
    public ResponseEntity<CalculoISAN> calcular(
            @RequestBody @Valid CalculoISANRequest request) {  // ✅ DTO para validación

        log.info("Solicitud de cálculo ISAN: monto={}, anio={}",
                request.getMonto(), request.getAnio());

        CalculoISAN resultado = service.calculaISAN(request.getMonto(), request.getAnio());

        return ResponseEntity.ok(resultado);
    }

}
