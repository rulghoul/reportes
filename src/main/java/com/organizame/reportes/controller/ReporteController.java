package com.organizame.reportes.controller;

import com.organizame.reportes.dto.request.RequestOrigen;
import com.organizame.reportes.service.ReporteExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/reporte")
public class ReporteController {

    private ReporteExcelService service;

    public ReporteController(ReporteExcelService service){
        this.service = service;
    }

    @Operation(summary = "regresa reporte Excel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("/excel")
    @ResponseBody
    public ResponseEntity<?>  reporteExcel(@RequestBody RequestOrigen request){
        try {
            ByteArrayInputStream resultado = service.CrearExcelOrigen(request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "reporte" + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(resultado, "reporte"));
        } catch (IOException e) {

            log.error("Error al generar el Excel", e);
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "No se pudo generar el archivo Excel");
            error.put("detalle", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }
}
