package com.organizame.reportes.controller;

import com.organizame.reportes.dao.DaoResumenPeriodo;
import com.organizame.reportes.dao.EstructuraExcel;
import com.organizame.reportes.dao.request.RequestOrigen;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.persistence.entities.VhcModeloperiodoindustria;
import com.organizame.reportes.service.ModeloPeriodoService;
import com.organizame.reportes.utils.excel.CrearExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestControler {

    private final ModeloPeriodoService service;

    @Autowired
    public TestControler(ModeloPeriodoService service){
        this.service = service;
    }

    @Operation(summary = "Recupera Origenes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @GetMapping("/origenes")
    public ResponseEntity<List<String>> recuperaOrigenes(){
        return ResponseEntity.ok(service.recuperaOrigenes());
    }


    @Operation(summary = "Recupera Segmentos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @GetMapping("/segmentos")
    public ResponseEntity<List<String>> recuperaSegmentos(){
        return ResponseEntity.ok(service.recuperaSegmento());
    }

    @Operation(summary = "Recupera Base de dato de un pais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @GetMapping("/pais/{pais}")
    public ResponseEntity<Set<VhcModeloperiodoindustria>> recuperaRegistros(@PathVariable String pais){
        return ResponseEntity.ok(service.recuperaOperacionesOrigen(pais));
    }

    @Operation(summary = "Recupera resumen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("/tablas/resumen")
    public ResponseEntity<Collection<DaoResumenPeriodo>> tablaSegmento(@RequestBody RequestOrigen request){
        List<VhcModeloperiodoindustria> resultado = service.recuperaOrigenVeinticuatoMeses(request.getOrigen(), request.getMesReporte());
        Set<DaoResumenPeriodo> filtrado = service.ResumeData(resultado);
        return ResponseEntity.ok(filtrado);
    }


    @Operation(summary = "Recupera tablas excel segmento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("/tablas/segmento")
    public ResponseEntity<List<List<Object>>> tablaExcelSegmento(@RequestBody RequestOrigen request){
        List<VhcModeloperiodoindustria> resultado = service.recuperaOrigenVeinticuatoMeses(request.getOrigen(), request.getMesReporte());
        Set<DaoResumenPeriodo> filtrado = service.ResumeData(resultado);
        var result = service.generaDatosTablaSegmentoMarca(filtrado, request.getMesReporte());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Recupera tablas pivot Segmento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("/pivot/segmento")
    public ResponseEntity<Map<String,List<List<Object>>>> tablaPivotSegmento(@RequestBody RequestOrigen request){
        List<VhcModeloperiodoindustria> resultado = service.recuperaOrigenVeinticuatoMeses(request.getOrigen(), request.getMesReporte());
        Set<DaoResumenPeriodo> filtrado = service.ResumeData(resultado);
        var result = service.generaDatosPivotPorSegmento(filtrado, request.getMesReporte());
        return ResponseEntity.ok( result);
    }

    @Operation(summary = "Recupera tablas pivot Marca")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("/pivot/marca")
    public ResponseEntity<Map<String,List<List<Object>>>> tablaPivotMarca(@RequestBody RequestOrigen request){
        List<VhcModeloperiodoindustria> resultado = service.recuperaOrigenVeinticuatoMeses(request.getOrigen(), request.getMesReporte());
        Set<DaoResumenPeriodo> filtrado = service.ResumeData(resultado);
        var result = service.generaDatosPivotPorMarca(filtrado, request.getMesReporte());
        return ResponseEntity.ok( result);
    }


    @PostMapping("/excel")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getImageDynamicType(@RequestBody EstructuraExcel request) throws IOException {
        CrearExcel excel = new CrearExcel();
        request.getHojas().forEach(
                hoja -> {
                    var sheet = excel.CrearHoja(hoja.getNombreHoja());
                    var col = 2;
                    var row = 2;
                    Map<String, Integer> resultado = new HashMap<>();
                    for (var tabla : hoja.getTablas()) {
                        resultado = excel.creaTabla(sheet, tabla.getDatos(), col, row);
                        row = resultado.get("row") + 2;
                        //col = resultado.get("col") +2;
                    }

                    try {
                        excel.TestGrafica(resultado.get("col") + 2
                                , resultado.get("row") - hoja.getTablas().getLast().getDatos().size()
                                , sheet);
                    } catch (GraficaException e) {
                        log.error("Fallo al crear la grafica de ejemplo");
                    }
                    IntStream.range(2,60).forEach(i ->
                            sheet.autoSizeColumn(i)
                    );


                }
        );
        ByteArrayInputStream resultado = excel.guardaExcel();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(new InputStreamResource(resultado, request.getNombreExcel()));
    }


}
