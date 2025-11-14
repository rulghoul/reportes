package com.organizame.reportes.utils.graficas;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.VentasPorMes;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.*;
import org.jfree.chart.labels.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;
import org.jfree.chart.axis.NumberAxis;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

@Slf4j
@Service
public class Graficas {

    private StandardChartTheme temaEstandar() {
        StandardChartTheme tema = new StandardChartTheme("TemaOrganizame");
        tema.setChartBackgroundPaint(Color.WHITE);
        tema.setPlotBackgroundPaint(new Color(245, 245, 245));
        tema.setDomainGridlinePaint(Color.LIGHT_GRAY);
        tema.setRangeGridlinePaint(Color.LIGHT_GRAY);
        tema.setExtraLargeFont(new Font("Tahoma", Font.BOLD, 16));
        tema.setLargeFont(new Font("Tahoma", Font.PLAIN, 14));
        tema.setRegularFont(new Font("Tahoma", Font.PLAIN, 12));

        Paint[] coloresSeries = new Paint[]{
                new Color(79, 129, 189),
                new Color(192, 80, 77),
                new Color(155, 187, 89),
                new Color(128, 100, 162),
                new Color(75, 172, 198)
        };
        tema.setDrawingSupplier(new DefaultDrawingSupplier(
                coloresSeries,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
        ));
        return tema;
    }

    // === DATASETS PARA MODELOS ===
    public DefaultCategoryDataset generaDataset(List<DaoPeriodo> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        datos.stream()
                .filter(dato -> !dato.getModelo().equalsIgnoreCase("TOTAL"))
                .filter(dato -> dato.getPorcentaje() > 2)
                .forEach(dato ->
                        dataset.addValue(dato.getPorcentaje(), dato.getModelo(), "Porcentaje")
                );
        return dataset;
    }

    public DefaultCategoryDataset generaDataset2(List<DaoPeriodo> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        datos.stream()
                .filter(dato -> !dato.getModelo().equalsIgnoreCase("TOTAL"))
                .filter(dato -> dato.getPorcentaje() > 2)
                .forEach(dato ->
                        dataset.addValue(dato.getTotal(), dato.getModelo(), "Volumen")
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

    // === DATASET PARA LÍNEAS (MARCAS) ===
    public DefaultCategoryDataset generaDatasetLineasMarcas(List<VentasPorMes> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (VentasPorMes dato : datos) {
            dataset.addValue(dato.getStellantis(), "Stellantis", dato.getMes());
            dataset.addValue(dato.getRenault(), "Renault", dato.getMes());
            dataset.addValue(dato.getVolkswagen(), "Volkswagen", dato.getMes());
            dataset.addValue(dato.getGeneralMotors(), "General Motors", dato.getMes());
            dataset.addValue(dato.getHyundai(), "Hyundai", dato.getMes());
        }
        return dataset;
    }

    // === GRÁFICA DE LÍNEAS (MARCAS) - JFreeChart 1.0.19 ===
    public JFreeChart generarGraficaLineasMarcas(String titulo, List<VentasPorMes> datos) {
        DefaultCategoryDataset dataset = generaDatasetLineasMarcas(datos);

        JFreeChart chart = ChartFactory.createLineChart(
                titulo, "Mes", "Unidades Vendidas", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();

        // ✅ Métodos COMPATIBLES con 1.0.19
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##0"))
        );
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER)
        );

        // ✅ Estilos: Stellantis más gruesa y punteada
        renderer.setSeriesStroke(0, new BasicStroke(
                4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
                new float[]{8.0f, 6.0f}, 0.0f
        ));
        renderer.setSeriesPaint(0, new Color(30, 60, 100)); // Stellantis

        renderer.setSeriesStroke(1, new BasicStroke(4.0f));
        renderer.setSeriesPaint(1, new Color(255, 140, 0)); // Renault

        renderer.setSeriesStroke(2, new BasicStroke(4.0f));
        renderer.setSeriesPaint(2, new Color(0, 128, 0)); // VW

        renderer.setSeriesStroke(3, new BasicStroke(4.0f));
        renderer.setSeriesPaint(3, new Color(0, 100, 200)); // GM

        renderer.setSeriesStroke(4, new BasicStroke(4.0f));
        renderer.setSeriesPaint(4, new Color(128, 0, 128)); // Hyundai

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        this.temaEstandar().apply(chart);
        return chart;
    }

    // === GRÁFICA COMBINADA: 2 BARRAS + 1 LÍNEA ===
    public JFreeChart graficaCombinada2Barras1Linea(String titulo, String ejeX,
                                                    String ejeYVolumen, String ejeYPorcentaje,
                                                    DefaultCategoryDataset dataset,
                                                    String serieBarras1, String serieBarras2, String serieLinea) {
        int idx1 = dataset.getRowIndex(serieBarras1);
        int idx2 = dataset.getRowIndex(serieBarras2);
        int idx3 = dataset.getRowIndex(serieLinea);
        if (idx1 == -1 || idx2 == -1 || idx3 == -1) {
            throw new IllegalArgumentException("Una o más series no existen en el dataset.");
        }

        JFreeChart chart = ChartFactory.createBarChart(
                titulo, ejeX, "", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );
        chart.getLegend().setPosition(RectangleEdge.TOP);

        CategoryPlot plot = chart.getCategoryPlot();

        // ✅ Eje Y derecho: % Participación
        NumberAxis rightAxis = new NumberAxis(ejeYPorcentaje);
        rightAxis.setNumberFormatOverride(new DecimalFormat("0.0%"));
        rightAxis.setRange(0.0, 0.11); // ✅ 0% a 11%
        rightAxis.setLabelPaint(Color.RED);
        plot.setRangeAxis(1, rightAxis);

        // ✅ Renderer para BARRAS
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setSeriesPaint(0, new Color(70, 130, 180)); // Azul claro: "Ventas origen Brasil"
        barRenderer.setSeriesPaint(1, Color.BLACK);              // Negro: "Ventas Industria"
        barRenderer.setShadowVisible(false);
        barRenderer.setItemMargin(0.05);
        barRenderer.setDefaultItemLabelsVisible(true);
        barRenderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,###"))
        );

        // ✅ Renderer para LÍNEA (%)
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, Color.RED); // Rojo: "% Part. Ventas origen Brasil"
        lineRenderer.setDefaultStroke(new BasicStroke(3.0f));
        lineRenderer.setDefaultShapesVisible(true);
        lineRenderer.setDefaultShapesFilled(true);
        lineRenderer.setDefaultItemLabelsVisible(true);
        lineRenderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0.0%"))
        );

        // ✅ Asignar renderers y ejes
        plot.setRenderer(idx1, barRenderer);
        plot.setRenderer(idx2, barRenderer);
        plot.setRenderer(idx3, lineRenderer);
        plot.setRangeAxis(idx1, plot.getRangeAxis(0));
        plot.setRangeAxis(idx2, plot.getRangeAxis(0));
        plot.setRangeAxis(idx3, rightAxis);

        // ✅ Forzar mapeo de la serie de línea al eje derecho
        plot.mapDatasetToRangeAxis(idx3, 1); // ← ¡esto es la clave!

        // ✅ Eliminar título del eje Y izquierdo
        plot.getRangeAxis(0).setLabel("");

        // ✅ Estilo visual
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        return chart;
    }

    /**
     * Gráfica tipo Donut (anillo) con agujero central
     */
    public JFreeChart graficaDonut(String titulo, DefaultPieDataset datos) {
        var chart = ChartFactory.createRingChart(
                titulo,
                datos,
                true,   // leyenda
                true,   // tooltips
                false   // URLs
        );

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setSectionDepth(0.5); // Grosor del anillo (0.5 = 50% del radio)

        // ✅ Etiquetas: valor + porcentaje
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{1}; {2}",
                new DecimalFormat("#,##0"),
                new DecimalFormat("0%")
        ));
        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        plot.setLabelPaint(Color.BLACK);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);

        // ✅ Colores vibrantes
        Paint[] colores = new Paint[]{
                new Color(70, 130, 180),   // Azul
                new Color(192, 80, 77),     // Rojo
                new Color(155, 187, 89),    // Verde
                new Color(255, 215, 0),     // Amarillo
                new Color(128, 0, 128),     // Morado
                new Color(30, 144, 255)     // Azul cielo
        };
        plot.setDrawingSupplier(new DefaultDrawingSupplier(
                colores,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
        ));

        // ✅ Estilo general
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
        chart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        return chart;
    }


    // === GRÁFICAS EXISTENTES (sin cambios) ===
    public JFreeChart graficaLineas(String titulo, String xAxis, String yAxis, DefaultCategoryDataset datos) {
        var chart = ChartFactory.createLineChart(titulo, xAxis, yAxis, datos, PlotOrientation.VERTICAL, true, true, false);
        this.temaEstandar().apply(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setDefaultStroke(new BasicStroke(2.5f));
        return chart;
    }

    public JFreeChart graficaBarras(String titulo, String xAxis, String yAxis, DefaultCategoryDataset datos) {
        var chart = ChartFactory.createBarChart(titulo, xAxis, yAxis, datos, PlotOrientation.VERTICAL, true, true, false);
        this.temaEstandar().apply(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.1);
        return chart;
    }

    public JFreeChart graficaCircular3D(String titulo, DefaultPieDataset datos) {
        var chart = ChartFactory.createPieChart3D(
                titulo,
                datos,
                true,   // leyenda
                true,   // tooltips
                false   // URLs
        );

        org.jfree.chart.plot.PiePlot3D plot = (org.jfree.chart.plot.PiePlot3D) chart.getPlot();

        // ✅ 1. Estilo 3D: ligeramente inclinado
        plot.setDepthFactor(0.2);
        plot.setCircular(true);

        // ✅ 2. Líneas blancas (borde de rebanadas) → CORREGIDO PARA 1.0.19
        for (int i = 0; i < datos.getKeys().size(); i++) {
            plot.setSectionOutlinePaint(datos.getKey(i), Color.WHITE);
            plot.setSectionOutlineStroke(datos.getKey(i), new BasicStroke(1.0f));
        }

        // ✅ 3. Colores vibrantes (paleta personalizada)
        Paint[] colores = new Paint[]{
                new Color(70, 130, 180),   // Azul
                new Color(192, 80, 77),     // Rojo
                new Color(155, 187, 89),    // Verde
                new Color(255, 215, 0),     // Amarillo dorado
                new Color(128, 0, 128),     // Morado
                new Color(30, 144, 255)     // Azul cielo
        };
        plot.setDrawingSupplier(new DefaultDrawingSupplier(
                colores,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
        ));

        // ✅ 4. Etiquetas dentro de las rebanadas: valor + porcentaje
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{1}; {2}",  // {1} = valor, {2} = porcentaje
                new DecimalFormat("#,##0"),      // Formato de número con comas
                new DecimalFormat("0%")          // Formato de porcentaje
        ));
        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 12)); // Fuente más grande
        plot.setLabelPaint(Color.BLACK); // Texto negro para contraste
        plot.setLabelBackgroundPaint(null); // Sin fondo
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);

        // ✅ 5. Leyenda debajo: fuente grande y centrada
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("SansSerif", Font.PLAIN, 14)); // Fuente más grande
            legend.setBackgroundPaint(Color.WHITE);
            legend.setFrame(BlockBorder.NONE);
            legend.setPosition(RectangleEdge.BOTTOM); // Leyenda abajo
        }

        // ✅ 6. Título arriba: centrado, negrita, tamaño grande
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
        chart.getTitle().setPaint(Color.BLACK);
        chart.setTitle(titulo); // Asegurar título

        // ✅ 7. Estilo general: sin fondo, borde blanco
        chart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        return chart;
    }
}