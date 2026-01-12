package com.organizame.reportes.controller;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.VentasPorMes;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.persistence.entities.VhcGrupo;
import com.organizame.reportes.persistence.entities.VhcMarca;
import com.organizame.reportes.persistence.entities.VhcPeriodo;
import com.organizame.reportes.repository.service.GruposService;
import com.organizame.reportes.repository.service.PeriodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    private final PeriodoService service;

    @Autowired
    public TestController(PeriodoService service) {
        this.service = service;
    }

    @Operation(summary = "Prueba periodo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gráfica generada", content = {
                    @Content(mediaType = "image/png")}),
            @ApiResponse(responseCode = "500", description = "Error interno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})
    })
    @GetMapping("/test")
    public ResponseEntity<?> reporteExcel() {

        try {

            var fechaIni = LocalDate.of( 2025,2, 1);
            var fechAnt = fechaIni.minusMonths(1);
            log.info("Fecha Actual {} mes anterior {}", fechaIni, fechAnt);
            var resultado = service.getRegistrosMes(2024, 9);
            var anterior = service.getRegistrosMes(2024, 8);
            var agrupado = service.getMargenes(resultado, anterior);

            return ResponseEntity.ok(service.groupByMarca(agrupado));
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Fallo por: " + e.getMessage());
        }
    }

}