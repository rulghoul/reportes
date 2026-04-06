package com.organizame.reportes.controller;

import com.organizame.reportes.dto.request.RequestMargen;
import com.organizame.reportes.repository.service.MargenUtilidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.NotContextException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/utils/margen/fecha")
public class MargenUtilidadController {

    private MargenUtilidadService service;

    @Autowired
    public MargenUtilidadController(MargenUtilidadService service){
        this.service = service;
    }

    @Operation(summary = "Recupera los margenes de utilidad para una fecha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @GetMapping()
    @ResponseBody
    public ResponseEntity<?> getMargenUtilidad(@RequestParam LocalDate fecha){
        try{
            var margenes = service.getMargenUtilidad(fecha);
            var margendto = margenes.stream()
                    .map(marg -> marg.toDto())
                    .toList();
            if(margenes.isEmpty()){
                throw new NotContextException();
            }
            return ResponseEntity.ok(Map.of("Encontrados", margendto));
        }catch (NotContextException e){
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "Inserta Margenes de Utilidad para una fecha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping()
    @ResponseBody
    public ResponseEntity<?> postReporteFinanciero(@RequestBody RequestMargen request){
        try{
            var registros = service.getMargenes(request.getFecha());
            log.info("Se recuperaron {} registros", registros.size());
            var margenes = registros.stream()
                    .map(reg -> reg.getMargenUtilidad())
                    .toList();
            var margendto = margenes.stream()
                    .map(marg -> marg.toDto())
                    .toList();
            if(margenes.isEmpty()){
                throw new NotContextException();
            }
            var salvados = service.saveMargenes(margenes);
            return ResponseEntity.ok(Map.of("Encontrados", margendto, "Guardados", salvados.size()));
        }catch (NotContextException e){
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "Actualiza margenes de utiilidad para una fecha dada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PatchMapping()
    @ResponseBody
    public ResponseEntity<?> updateReporteFinanciero(@RequestBody RequestMargen request){
        try{
            var registros = service.getMargenes(request.getFecha());
            log.info("Se recuperaron {} registros", registros.size());
            var margenes = registros.stream()
                    .map(reg -> reg.getMargenUtilidad())
                    .toList();
            var margendto = margenes.stream()
                    .map(marg -> marg.toDto())
                    .toList();
            if(margenes.isEmpty()){
                throw new NotContextException();
            }
            var salvados = service.updateMargenes(margenes);
            return ResponseEntity.ok(Map.of("Encontrados", margendto, "Guardados", salvados.size()));
        }catch (NotContextException e){
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "Inserta nuevos valores ya actualiza anteriores Margenes de utilidad para una fecha dada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PutMapping()
    @ResponseBody
    public ResponseEntity<?> upsertReporteFinanciero(@RequestBody RequestMargen request){
        try{
            var registros = service.getMargenes(request.getFecha());
            log.info("Se recuperaron {} registros", registros.size());
            var margenes = registros.stream()
                    .map(reg -> reg.getMargenUtilidad())
                    .toList();
            var margendto = margenes.stream()
                    .map(marg -> marg.toDto())
                    .toList();
            if(margenes.isEmpty()){
                throw new NotContextException();
            }
            var salvados = service.updateMargenes(margenes);
            return ResponseEntity.ok(Map.of("Encontrados", margendto, "Guardados", salvados.size()));
        }catch (NotContextException e){
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "Recupera los margenes de utilidad para una fecha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @DeleteMapping()
    @ResponseBody
    public ResponseEntity<?> deleteMargenUtilidad(@RequestBody RequestMargen request){
        try{
            var margenes = service.getMargenUtilidad(request.getFecha());
            var margendto = margenes.stream()
                    .map(marg -> marg.toDto())
                    .toList();
            if(margenes.isEmpty()){
                throw new NotContextException();
            }
            service.deleteMargenUtilidad(margenes);
            return ResponseEntity.ok(Map.of("Se borraron", margendto));
        }catch (NotContextException e){
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }
}
