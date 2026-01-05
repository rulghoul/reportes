package com.organizame.reportes.controller;


import com.organizame.reportes.dto.request.RequestOrigen;
import com.organizame.reportes.exceptions.SinDatos;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/reporte/excel")
public class ReporteOrigenController {

    private final ReporteExcelService serviceLibro;

    private final DateTimeFormatter fechaFormat = DateTimeFormatter.ISO_DATE;
    private ReporteOrigenController(ReporteExcelService serviceLibro){
        this.serviceLibro = serviceLibro;
    }

    @Operation(summary = "regresa reporte Excel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("")
    @ResponseBody
    public ResponseEntity<?> postReporteExcel(@RequestBody RequestOrigen request){
        return reporteExcel(request);
    }

    @Operation(summary = "regresa reporte Excel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getReporteExcel(@RequestParam String origen,@RequestParam String mesFinal, @RequestParam Integer mesReporte ){
        try {
            LocalDate fecha = LocalDate.from(fechaFormat.parse(mesFinal));
            RequestOrigen request = new RequestOrigen(origen, fecha, mesReporte);
            return reporteExcel(request);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("La fecha se espera en formato yyyy-MM-dd");
        }
    }

    private ResponseEntity<?> reporteExcel(RequestOrigen request){
        try {
            ByteArrayInputStream resultado = serviceLibro.CrearExcelOrigen(request);
            var archivo = serviceLibro.getNombreArchivo();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + archivo + ".xlsx")
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
        }catch (SinDatos e){
            return ResponseEntity.badRequest().body("Error en la petición: " + e.getMessage());
        }
    }
}
