package com.organizame.reportes.utils.graficas;

import com.organizame.reportes.dao.DaoPeriodo;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.util.*;
import java.util.List;

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

    public DefaultPieDataset generaDataset3(List<DaoPeriodo> datos) {
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
     * COMBINACIÓN 1: BARRAS + LÍNEA CON DOBLE EJE Y
     * Uso: Métricas con escalas diferentes (ej: ventas en $ y % crecimiento)
     */
    public JFreeChart graficaBarrasLineaDobleEje(String titulo, String xAxis,
                                                 String yAxisIzq, String yAxisDer,
                                                 DefaultCategoryDataset datosBarras,
                                                 DefaultCategoryDataset datosLinea) {
        // Crear el plot
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(0, datosBarras);
        plot.setDataset(1, datosLinea);

        // Configurar renderer para barras
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setShadowVisible(false);
        plot.setRenderer(0, barRenderer);

        // Configurar renderer para línea
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setDefaultStroke(new BasicStroke(3.0f));
        lineRenderer.setDefaultShapesVisible(true);
        lineRenderer.setDefaultShapesFilled(true);
        plot.setRenderer(1, lineRenderer);

        // Eje Y izquierdo (barras)
        NumberAxis leftAxis = new NumberAxis(yAxisIzq);
        plot.setRangeAxis(0, leftAxis);
        plot.mapDatasetToRangeAxis(0, 0);

        // Eje Y derecho (línea)
        NumberAxis rightAxis = new NumberAxis(yAxisDer);
        plot.setRangeAxis(1, rightAxis);
        plot.mapDatasetToRangeAxis(1, 1);

        // Eje X
        CategoryAxis domainAxis = new CategoryAxis(xAxis);
        plot.setDomainAxis(domainAxis);

        // Crear el chart
        var chart = new JFreeChart(titulo, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        this.temaEstandar().apply(chart);

        return chart;
    }
    public JFreeChart graficaBarrasColor(String titulo, String xAxis, String yAxis, DefaultCategoryDataset datos) {
        JFreeChart grafica = ChartFactory.createBarChart(
                titulo,
                xAxis,
                yAxis,
                datos,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        temaEstandar().apply(grafica);
        return grafica;
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
