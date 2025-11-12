package com.organizame.reportes.controller;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.utils.graficas.Graficas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;
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

@Slf4j
@RestController
@RequestMapping("/grafica")
public class TestGraficas {

    private Graficas graficas;

    @Autowired
    public TestGraficas(Graficas graficas){
        this.graficas = graficas;
    }

    @Operation(summary = "regresa reporte Excel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo exitoso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})})
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<?> reporteExcel(){
        try {
            List<DaoPeriodo> listaDaoPeriodo = List.of(
                    new DaoPeriodo("Modelo A", 50, 16.6),
                    new DaoPeriodo("Modelo B", 100, 33.3),
                    new DaoPeriodo("Modelo C", 150, 50.0)
            );
            var datos = graficas.generaDataset(listaDaoPeriodo);
            var datos2 = graficas.generaDataset2(listaDaoPeriodo);
            var datos3 = graficas.generaDataset3(listaDaoPeriodo);
            // Aqui se movera la grafica a utilizar
            JFreeChart grafica = graficas.graficaCircular3D("Participación por Modelo", datos3);//            JFreeChart grafica = graficas.graficaBarrasLineaDobleEje("test", "x", "xAxis2", "y", datos, datos2);
//            JFreeChart grafica = graficas.graficaBarrasLineaDobleEje("Participación por Modelo", "","","", datos, datos2);//            JFreeChart grafica = graficas.graficaBarrasLineaDobleEje("test", "x", "xAxis2", "y", datos, datos2);

            // ya se cambio la grafica
            ByteArrayOutputStream resultado = this.GeneraArchivoGrafica(grafica);
            // Create InputStream from ByteArrayOutputStream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(resultado.toByteArray());
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "test_grafica" + ".png")
                    .contentType(MediaType.parseMediaType("image/jpeg"))
                    .body(new InputStreamResource(resource, "reporte"));

        } catch (Exception | GraficaException e) {
            log.error("Error al generar el Excel", e);
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "No se pudo generar la grafica");
            error.put("detalle", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    private ByteArrayOutputStream GeneraArchivoGrafica(JFreeChart chart) throws GraficaException {
        try (ByteArrayOutputStream chartOut = new ByteArrayOutputStream()) {
            //ChartUtils.writeChartAsPNG(chartOut, chart, width, height);
            BufferedImage image = chart.createBufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB, null);
            ImageIO.write(image, "png", chartOut);
            return chartOut;
        } catch (IOException e) {
            log.error("Error al generar gráfica: {}", e.getMessage(), e);
            throw new GraficaException(e);
        }
    }
}
