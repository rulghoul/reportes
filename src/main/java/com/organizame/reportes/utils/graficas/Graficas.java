package com.organizame.reportes.utils.graficas;

import com.organizame.reportes.dto.DaoPeriodo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.ui.*;
import org.jfree.chart.axis.*;
import org.jfree.data.category.DefaultCategoryDataset;

import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.springframework.stereotype.Service;

import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;



import java.awt.*;

@Slf4j
@Service
public class Graficas {

    private StandardChartTheme temaEstandar() {
        StandardChartTheme tema = new StandardChartTheme("TemaOrganizame");

        // 🎨 Colores generales
        tema.setChartBackgroundPaint(Color.WHITE);
        tema.setPlotBackgroundPaint(new Color(245, 245, 245));
        tema.setDomainGridlinePaint(Color.LIGHT_GRAY);
        tema.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // 🔠 Fuentes
        tema.setExtraLargeFont(new Font("Tahoma", Font.BOLD, 16)); // título
        tema.setLargeFont(new Font("Tahoma", Font.PLAIN, 14));     // ejes
        tema.setRegularFont(new Font("Tahoma", Font.PLAIN, 12));   // etiquetas

        // 🎯 Colores de las series
        Paint[] coloresSeries = new Paint[]{
                new Color(79, 129, 189),   // Azul
                new Color(192, 80, 77),    // Rojo
                new Color(155, 187, 89),   // Verde
                new Color(128, 100, 162),  // Morado
                new Color(75, 172, 198)    // Turquesa
        };
        tema.setDrawingSupplier(new org.jfree.chart.plot.DefaultDrawingSupplier(
                coloresSeries,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
        ));

        return tema;
    }

    public DefaultCategoryDataset generaDataset(List<DaoPeriodo> datos){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        datos.stream()
                .filter(dato -> !dato.getModelo().equalsIgnoreCase("TOTAL"))
                .filter(dato -> dato.getPorcentaje() > 2)
                .forEach(dato ->
                dataset.addValue(dato.getPorcentaje() , dato.getModelo(), "Participacion")
        );
        return dataset;
    }

    public DefaultCategoryDataset generaDataset2(List<DaoPeriodo> datos){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        datos.stream()
                .filter(dato -> !dato.getModelo().equalsIgnoreCase("TOTAL"))
                .filter(dato -> dato.getPorcentaje() > 2)
                .forEach(dato ->
                        dataset.addValue(dato.getTotal() , dato.getModelo(), "Participacion")
                );
        return dataset;
    }

    public DefaultPieDataset generaDataset3(@NonNull List<DaoPeriodo> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        datos.stream()
                .filter(dato -> !dato.getModelo().equalsIgnoreCase("TOTAL"))
                .filter(dato -> dato.getPorcentaje() > 2)
                .forEach(dato ->
                        dataset.setValue(dato.getModelo(), dato.getPorcentaje())
                );

        return dataset;
    }

    // ========================================================================
    // SECCIÓN 1: GRÁFICAS INDIVIDUALES
    // ========================================================================

    /**
     * 1. GRÁFICA DE LÍNEAS
     * Uso: Mostrar tendencias y evolución temporal
     */
    public JFreeChart graficaLineas(String titulo, String xAxis, String yAxis,
                                    DefaultCategoryDataset datos) {
        var chart = ChartFactory.createLineChart(
                titulo,
                xAxis,
                yAxis,
                datos,
                PlotOrientation.VERTICAL,
                true,  // leyenda
                true,  // tooltips
                false  // urls
        );
        this.temaEstandar().apply(chart);
        // Configurar puntos visibles en la línea
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setDefaultStroke(new BasicStroke(2.5f));
        return chart;
    }

    /**
     * 2. GRÁFICA DE BARRAS VERTICALES
     * Uso: Comparar valores entre diferentes categorías
     */
    public JFreeChart graficaBarras(String titulo, String xAxis, String yAxis,
                                    DefaultCategoryDataset datos) {
        var chart = ChartFactory.createBarChart(
                titulo,
                xAxis,
                yAxis,
                datos,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        this.temaEstandar().apply(chart);

        // Configurar sombras en barras
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.1);

        return chart;
    }

    public JFreeChart graficaBarrasColor(String titulo, String xAxis, String yAxis,
                                    DefaultCategoryDataset datos) {
        var chart = ChartFactory.createBarChart(
                titulo,
                xAxis,
                yAxis,
                datos,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        this.temaEstandar().apply(chart);

        // Configurar sombras en barras
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.1);
        this.temaEstandar().apply(chart);
        return   chart;
    }

    /**
     * 5. GRÁFICA CIRCULAR 3D
     * Uso: Pie chart con efecto tridimensional
     */
    public JFreeChart graficaCircular3D(String titulo, DefaultPieDataset datos) {
        var chart = ChartFactory.createPieChart3D(
                titulo,
                datos,
                true,
                true,
                false
        );

        org.jfree.chart.plot.PiePlot3D plot = (org.jfree.chart.plot.PiePlot3D) chart.getPlot();
        plot.setDepthFactor(0.1);
        plot.setCircular(true);

        this.temaEstandar().apply(chart);
        return chart;
    }

    // ========================================================================
    // SECCIÓN 2: GRÁFICAS COMBINADAS
    // ========================================================================

    /**
     * Genera un gráfico combinado de Barras y Línea con Doble Eje Y,
     * diseñado para el reporte de Ventas vs. Participación.
     *
     * @param titulo Título de la gráfica.
     * @param dataset ÚNICO dataset con las 3 series (Ventas Industria, Ventas Brasil, % Part.).
     * @return El objeto JFreeChart configurado.
     */
    public JFreeChart graficaReporteCombinada(String titulo, DefaultCategoryDataset dataset) {

        // 1. Crear el chart base usando un gráfico de barras simple
        JFreeChart chart = ChartFactory.createBarChart(
                titulo,
                "Periodo", // Eje X
                "Volumen", // Eje Y Principal
                dataset,
                PlotOrientation.VERTICAL,
                true, // Leyenda
                true, // Tooltips
                false // URLs
        );

        CategoryPlot plot = chart.getCategoryPlot();

        // 2. CONFIGURACIÓN DEL EJE Y SECUNDARIO (Derecho - Porcentaje)
        NumberAxis rightAxis = new NumberAxis("Porcentaje");
        DecimalFormat percentFormat = new DecimalFormat("0.0%");
        rightAxis.setNumberFormatOverride(percentFormat);

        // Se recomienda ajustar el rango para que la línea se vea bien.
        // Ejemplo: Si el max % es 15%, ajuste el rango a 0.15.
        // rightAxis.setRange(0, 0.15);

        plot.setRangeAxis(1, rightAxis); // Añadir el Eje Y secundario al plot

        // 3. CONFIGURACIÓN DE RENDERERS Y ASIGNACIÓN DE SERIES

        // El dataset tiene 3 series (asumiendo: 0=Ventas Ind., 1=Ventas Brasil, 2=% Part.)

        // Renderer 0: BarRenderer para todas las series de volumen
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setShadowVisible(false);
        barRenderer.setSeriesPaint(0, Color.BLACK); // Venta Industria (fondo)
        barRenderer.setSeriesPaint(1, Color.BLUE);  // Venta Brasil (frente)

        // *** IMPORTANTE: MOSTRAR VALORES SOBRE LAS BARRAS ***
        barRenderer.setDefaultItemLabelsVisible(true);
        barRenderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());

        // Asignar el renderer a las series 0 y 1 (volumen)
        plot.setRenderer(0, barRenderer);
        plot.mapDatasetToRangeAxis(0, 0); // Mapear series de volumen al Eje Y 0 (Izquierda)
        plot.mapDatasetToRangeAxis(1, 0); // Mapear series de volumen al Eje Y 0 (Izquierda)

        // Renderer 1: LineAndShapeRenderer para la serie de porcentaje (índice 2)
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesPaint(2, Color.RED); // Línea roja para porcentaje
        lineRenderer.setDefaultStroke(new BasicStroke(3.0f));
        lineRenderer.setDefaultShapesVisible(true);

        // *** IMPORTANTE: MOSTRAR VALORES SOBRE LA LÍNEA (en formato %) ***
        lineRenderer.setDefaultItemLabelsVisible(true);
        lineRenderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                "{2}", new DecimalFormat("0.0%")
        ));

        // Asignar el renderer y el eje a la serie 2 (porcentaje)
        plot.setRenderer(1, lineRenderer);
        plot.mapDatasetToRangeAxis(2, 1); // Mapear la serie de % al Eje Y 1 (Derecha)
        //plot.mapDatasetToRenderer(2, 1); // Asignar la serie de % al Renderer 1

        // 4. CONFIGURACIONES ADICIONALES
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD); // Asegura que se dibuje en orden

        // Posicionar Leyenda
        chart.getLegend().setPosition(RectangleEdge.TOP);

        // Opcional: Aplicar un tema (asumiendo que temaEstandar() existe)
        // this.temaEstandar().apply(chart);

        return chart;
    }

    public JFreeChart graficaLineas(String titulo, String xAxis, String yAxis, Collection datos){
        DefaultCategoryDataset datoGrafica = null;
        return ChartFactory.createLineChart(
                titulo,
                xAxis,
                yAxis,
                datoGrafica
        );
    }

}
