package com.organizame.reportes.controller;

import com.organizame.reportes.dto.request.RequestOrigen;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.service.ReporteExcelService;
import com.organizame.reportes.service.ReportePresentacionService;
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

    private final ReporteExcelService serviceLibro;
    private final ReportePresentacionService servicePresentacion;

    public ReporteController(ReporteExcelService service, ReportePresentacionService servicePresentacion){
        this.serviceLibro = service;
        this.servicePresentacion = servicePresentacion;
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


    @Operation(summary = "regresa reporte en formato presentacion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("/presentacion")
    @ResponseBody
    public ResponseEntity<?>  reportePresentacion(@RequestBody RequestOrigen request){
        try {
            ByteArrayInputStream resultado = servicePresentacion.CrearPresentacionOrigen(request);
            //Codigo para convertir en base64 String base64String = convertToBase64(resultado);
            var archivo = servicePresentacion.getNombreArchivo();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + archivo + ".pptx")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-powerpoint"))
                    .body(new InputStreamResource(resultado, "reporte"));
        } catch (IOException e) {

            log.error("Error al generar la Presentacion", e);
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "No se pudo generar el archivo de Presentacion");
            error.put("detalle", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }catch (SinDatos e){
            return ResponseEntity.badRequest().body("Error en la petición: " + e.getMessage());
        }
    }

    public String convertToBase64(ByteArrayInputStream inputStream) throws java.io.IOException {
        // Lee todos los bytes del ByteArrayInputStream
        byte[] bytes = inputStream.readAllBytes();

        // Codifica los bytes a Base64
        return Base64.getEncoder().encodeToString(bytes);
    }
}
