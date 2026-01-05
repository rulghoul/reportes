package com.organizame.reportes.controller;

import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.service.ReporteRankingMarca;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/reporte/ranking")
public class ReporteRankingController {

    private final ReporteRankingMarca serviceRanking;


    @Autowired
    public ReporteRankingController(ReporteRankingMarca serviceRanking){
        this.serviceRanking = serviceRanking;
    }


    @Operation(summary = "regresa reporte Ranking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("")
    @ResponseBody
    public ResponseEntity<?> postReporteRanking(@RequestBody RequestRanking request){
        return reporteRanking(request);
    }

    @Operation(summary = "regresa reporte Ranking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getReporteRanking(@RequestParam Integer anio, @RequestParam Integer mes){
        var request = new RequestRanking(mes, anio);
        return reporteRanking(request);
    }


    private ResponseEntity<?> reporteRanking(RequestRanking request){
        try {
            ByteArrayInputStream resultado = serviceRanking.CrearExcelRanking(request);
            var archivo = serviceRanking.getNombreArchivo();
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
