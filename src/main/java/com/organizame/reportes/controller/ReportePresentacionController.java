package com.organizame.reportes.controller;

import com.organizame.reportes.dto.request.RequestOrigen;
import com.organizame.reportes.exceptions.SinDatos;
import com.organizame.reportes.service.ReportePDFService;
import com.organizame.reportes.service.ReportePresentacionService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Slf4j
@RestController
@RequestMapping("/reporte/presentacion")
public class ReportePresentacionController {
    private final ReportePresentacionService servicePresentacion;
    private final ReportePDFService pdfService;
    private final DateTimeFormatter fechaFormat = DateTimeFormatter.ISO_DATE;

    @Autowired
    public ReportePresentacionController(ReportePresentacionService servicePresentacion, ReportePDFService pdfService) {
        this.servicePresentacion = servicePresentacion;
        this.pdfService = pdfService;
    }


    @Operation(summary = "regresa reporte en formato presentacion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("")
    @ResponseBody
    public ResponseEntity<?> postReportePresentacion(@RequestBody RequestOrigen request) {
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
        } catch (SinDatos e) {
            return ResponseEntity.badRequest().body("Error en la petición: " + e.getMessage());
        }
    }

    @Operation(summary = "regresa reporte en formato presentacion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getReportePresentacion(@RequestParam String origen,@RequestParam String mesFinal, @RequestParam Integer mesReporte ) {
        try {
            LocalDate fecha = LocalDate.from(fechaFormat.parse(mesFinal));
            RequestOrigen request = new RequestOrigen(origen, fecha, mesReporte);
            return reportePresentacion(request);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("La fecha se espera en formato yyyy-MM-dd");
        }
    }

    @Operation(summary = "regresa reporte en formato PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("/pdf")
    @ResponseBody
    public ResponseEntity<?>  postreportePDF(@RequestBody RequestOrigen request){
        return reportePDF(request);
    }

    @Operation(summary = "regresa reporte en formato PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @GetMapping("/pdf")
    @ResponseBody
    public ResponseEntity<?>  getReportePDF(@RequestParam String origen,@RequestParam String mesFinal, @RequestParam Integer mesReporte ){
        try {
            LocalDate fecha = LocalDate.from(fechaFormat.parse(mesFinal));
            RequestOrigen request = new RequestOrigen(origen, fecha, mesReporte);
            return reportePDF(request);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("La fecha se espera en formato yyyy-MM-dd");
        }
    }


    private ResponseEntity<?> reportePresentacion(RequestOrigen request){
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
        } catch (SinDatos e) {
            return ResponseEntity.badRequest().body("Error en la petición: " + e.getMessage());
        }
    }

    private ResponseEntity<?> reportePDF(RequestOrigen request){
        try {
            ByteArrayInputStream presentacion = servicePresentacion.CrearPresentacionOrigen(request);
            var resultado = pdfService.ConviertePPTtoPDF(presentacion);
            //Codigo para convertir en base64 String base64String = convertToBase64(resultado);
            var archivo = servicePresentacion.getNombreArchivo();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + archivo + ".pdf")
                    .contentType(MediaType.parseMediaType("application/pdf"))
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
}
