package com.organizame.reportes.controller;

import com.organizame.reportes.dto.request.RequestMargen;
import com.organizame.reportes.dto.request.RequestRanking;
import com.organizame.reportes.repository.service.MargenUtilidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.NotContextException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/utils/margen")
public class MargenUtilidadController {

    private MargenUtilidadService service;

    @Autowired
    public MargenUtilidadController(MargenUtilidadService service){
        this.service = service;
    }

    @Operation(summary = "regresa reporte Financiero")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @PostMapping("fecha")
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
            return ResponseEntity.ok(margendto);
        }catch (NotContextException e){
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }
}
