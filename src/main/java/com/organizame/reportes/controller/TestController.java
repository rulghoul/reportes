package com.organizame.reportes.controller;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.VentasPorMes;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.persistence.entities.VhcGrupo;
import com.organizame.reportes.persistence.entities.VhcMarca;
import com.organizame.reportes.repository.service.GruposService;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    private final GruposService service;

    @Autowired
    public TestController(GruposService service) {
        this.service = service;
    }

    @Operation(summary = "Prueba grupo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gráfica generada", content = {
                    @Content(mediaType = "image/png")}),
            @ApiResponse(responseCode = "500", description = "Error interno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})
    })
    @GetMapping("/test")
    public ResponseEntity<Map<String, List<String>>> reporteExcel() {

        var grupos = service.findAll();
        var nomGrupos = grupos.stream()
                .collect(Collectors.toMap(
                        VhcGrupo::getNombre,
                        p -> p.getVhcgrupomarcaList().stream()
                                .map(marca -> marca.getVhcMarca().getNombre())
                                .toList()  // o .collect(Collectors.toList()) si usas Java <16
                ));

        return ResponseEntity.ok(nomGrupos);
    }

}