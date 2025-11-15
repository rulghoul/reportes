package com.organizame.reportes.controller;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.VentasPorMes;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.utils.graficas.Graficas;
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

@Slf4j
@RestController
@RequestMapping("/grafica")
public class TestGraficas {

    private final Graficas graficas;

    @Autowired
    public TestGraficas(Graficas graficas) {
        this.graficas = graficas;
    }

    @Operation(summary = "Regresa gráfica en PNG")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gráfica generada", content = {
                    @Content(mediaType = "image/png")}),
            @ApiResponse(responseCode = "500", description = "Error interno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))})
    })
    @GetMapping("/test")
    public ResponseEntity<?> reporteExcel() {
        try {
            // =================================================================
            // DATOS: UNO POR CADA TIPO DE GRÁFICA
            // =================================================================
            var datosLineas = datosParaLineas();             // ✅ para líneas (marcas)
            var datasetCombinado = datosParaCombinada();     // ✅ para combinada (2B+1L)
            var datosBarras = datosParaBarras();             // ✅ para barras (modelos)

            var datasetPorcentaje = graficas.generaDataset(datosBarras);   // % por modelo
            var datasetVolumen = graficas.generaDataset2(datosBarras);     // unidades por modelo
            var datasetPie = graficas.generaDataset3(datosBarras);         // para pastel/donut

            // =================================================================
            // 🔴 GRÁFICAS — DESCOMENTA LA QUE QUIERAS EJECUTAR
            // =================================================================

            // ✅ ACTIVA POR DEFECTO: Gráfica de líneas (Stellantis, Renault, etc.)
            //JFreeChart grafica = graficas.generarGraficaLineasMarcas(
            //        "Top 5 de marcas que comercializan modelos provenientes de Brasil (enero 2023-octubre 2024)",
            //        datosLineas
            //);

            // 🟢 Combinada: 2 barras + 1 línea (datos exactos)
            //JFreeChart grafica = graficas.graficaCombinada2Barras1Linea(
            //        "Ventas Industria vs Brasil y Participación",
            //        "Mes", "", "% Part. Ventas origen Brasil", // ejeY izquierdo vacío
            //        datasetCombinado,
            //        "Ventas origen Brasil", "Ventas Industria", "% Part. Ventas origen Brasil"
            //);

            // 🟢 Barras: participación por modelo
            //JFreeChart grafica = graficas.graficaBarras(
            //        "Participación por Modelo", "Modelo", "%", datasetPorcentaje
            //);

            // 🟢 Pastel 3D
            //JFreeChart grafica = graficas.graficaCircular3D("Participación por Modelo", datasetPie);

            // 🟢 Donut
            //JFreeChart grafica = graficas.graficaDonut("Participación por Modelo", datasetPie);

            // 🟢 Volumen por segmento
            //JFreeChart grafica = graficas.graficaVolumenPorSegmento(
            //        "Volumen de ventas por segmento, origen E.U.",
            //        "Top 5 ventas por segmento Lujo-origen Estadounidense: 2022-noviembre 2024",
            //        datosParaVolumen()
            //);

            // 🟢 Pick Ups Brasil
            JFreeChart grafica = graficas.graficaPickUpsBrasil(
                    "Top 5 ventas por segmento Pick ups-origen brasileño: 2022-octubre 2024",
                    "Modelo", "% Participación",
                    datosParaPickUps(),
                    "Total Verde", "Total Azul", "% Participación"
            );

            // =================================================================
            // EXPORTACIÓN (sin caché)
            // =================================================================
            ByteArrayOutputStream resultado = this.GeneraArchivoGrafica(grafica);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(resultado.toByteArray());
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=grafica_" + System.currentTimeMillis() + ".png")
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);

        } catch (Exception | GraficaException e) {
            log.error("Error al generar la gráfica", e);
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "No se pudo generar la gráfica");
            error.put("detalle", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    private ByteArrayOutputStream GeneraArchivoGrafica(JFreeChart chart) throws GraficaException {
        try (ByteArrayOutputStream chartOut = new ByteArrayOutputStream()) {
            BufferedImage image = chart.createBufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB, null);
            ImageIO.write(image, "png", chartOut);
            return chartOut;
        } catch (IOException e) {
            log.error("Error al generar gráfica", e);
            throw new GraficaException(e);
        }
    }

    // =================================================================
    // DATOS PARA CADA GRÁFICA (separados y sin conflictos)
    // =================================================================

    /**
     * Datos para gráfica de LÍNEAS: marcas por mes
     */
    private List<VentasPorMes> datosParaLineas() {
        return Arrays.asList(
                new VentasPorMes("ene", 1572, 723, 1094, 1332, 527),
                new VentasPorMes("feb", 2468, 1000, 1705, 1247, 379),
                new VentasPorMes("mar", 3488, 3303, 1910, 1147, 471),
                new VentasPorMes("abr", 3653, 2810, 1799, 932, 523),
                new VentasPorMes("may", 2828, 2768, 1876, 832, 985),
                new VentasPorMes("jun", 3389, 2777, 2731, 787, 945),
                new VentasPorMes("jul", 3017, 2531, 2637, 732, 932),
                new VentasPorMes("ago", 3004, 2187, 2840, 1086, 1178),
                new VentasPorMes("sep", 3045, 2000, 2527, 1118, 1248),
                new VentasPorMes("oct", 2732, 1790, 2370, 1066, 998)
        );
    }

    /**
     * Datos para gráfica COMBINADA: 2 barras + 1 línea
     */
    private DefaultCategoryDataset datosParaCombinada() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        String[] meses = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct"};

        // Datos reales de la imagen
        double[] industria = {101911, 118801, 125432, 119000, 132000, 142000, 124000, 126000, 123000, 125000};
        double[] brasil = {6909, 10844, 12500, 11000, 12500, 13000, 11800, 12200, 12000, 12300};

        for (int i = 0; i < meses.length; i++) {
            ds.addValue(industria[i], "Ventas Industria", meses[i]);
            ds.addValue(brasil[i], "Ventas origen Brasil", meses[i]); // ← nombre exacto como en la leyenda
            // Calcula el porcentaje: brasil / industria
            double porcentaje = industria[i] > 0 ? brasil[i] / industria[i] : 0.0;
            ds.addValue(porcentaje, "% Part. Ventas origen Brasil", meses[i]);
        }
        return ds;
    }

    /**
     * Datos para gráfica de BARRAS: participación por modelo
     */
    private List<DaoPeriodo> datosParaBarras() {
        return List.of(
                new DaoPeriodo("Modelo A", 50, 6.0),
                new DaoPeriodo("Modelo B", 100, 23.0),
                new DaoPeriodo("Modelo C", 150, 40.0),
                new DaoPeriodo("Modelo D", 50, 10.0),
                new DaoPeriodo("Modelo E", 100, 10.0),
                new DaoPeriodo("Modelo F", 150, 10.0)
        );
    }

    private DefaultCategoryDataset datosParaVolumen() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        // Solo una fila (porque es apilado horizontal)
        ds.addValue(9409, "Camry", "Ventas");
        ds.addValue(2507, "Accord", "Ventas");
        ds.addValue(1557, "Altima", "Ventas");
        ds.addValue(1457, "Nautilus", "Ventas");
        ds.addValue(933, "LEXUS ES", "Ventas");
        return ds;
    }

    private DefaultCategoryDataset datosParaPickUps() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        String[] modelos = {"Saveiro", "RAM 700", "S10", "Oroch", "Promaster Rapid", "Montana Crew Cab", "Partner Rapid", "Tornado Pickup"};

        // Total (barras verdes) — modelos NO Stellantis
        double[] totalVerde = {0, 0, 0, 0, 0, 0, 1168, 2}; // Partner Rapid, Tornado Pickup

        // Total (barras azules) — modelos Stellantis
        double[] totalAzul = {43099, 30821, 13105, 10495, 7105, 3782, 0, 0}; // Saveiro, RAM 700, S10, Oroch, Promaster Rapid, Montana Crew Cab

        // % Participación (línea azul oscuro)
        double[] participacion = {39, 28, 12, 10, 6, 3, 1, 1};

        for (int i = 0; i < modelos.length; i++) {
            ds.addValue(totalVerde[i], "Total Verde", modelos[i]);
            ds.addValue(totalAzul[i], "Total Azul", modelos[i]);
            ds.addValue(participacion[i], "% Participación", modelos[i]);
        }
        return ds;
    }

}