package com.organizame.reportes.utils.graficas;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.dto.auxiliar.PortadaTotales;
import jakarta.validation.OverridesAttribute;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;

@Slf4j
@Service
public class graficas2 {

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
        tema.setDrawingSupplier(new DefaultDrawingSupplier(
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
                .filter(dato -> dato.getPorcentaje() > 0.02)
                .forEach(dato ->
                dataset.addValue(dato.getPorcentaje() , dato.getModelo(), "Participacion")
        );
        return dataset;
    }

    public JFreeChart LineChartFabricantes(List<FilaTabla> tabla) {

        DefaultCategoryDataset dataset = createDataset(tabla);
        JFreeChart chart = ChartFactory.createLineChart(
                "Ventas Mensuales por Fabricante (ene-2023 a oct-2024)",
                "Mes",           // X
                "Cantidad",      // Y
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.UP_45
        );

        this.temaEstandar().apply(chart);
        return chart;
    }



    private DefaultCategoryDataset createDataset(List<FilaTabla> tabla) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();


        // Meses
        List<String> meses = tabla.getFirst().getFila()
                .subList(1, (tabla.getFirst().getFila().size()-1))
                .stream().map(e -> e.toString()).toList();

        // Datos por fabricante
        List<String> fabricantes = tabla
                .subList(1, tabla.size())
                .stream().map(fila -> fila.getFila().getFirst().toString())
                .toList();

        List<List<Integer>> datos = tabla
                .subList(1, tabla.size())
                .stream().map(fila -> fila.getFila().subList(1, fila.getFila().size()-1))
                .map(fila -> fila.stream().map(f -> (Integer) f).toList())
                .toList();

        for (int i = 0; i < fabricantes.size(); i++) {
            for (int j = 0; j < meses.size(); j++) {
                dataset.addValue( datos.get(i).get(j), fabricantes.get(i), meses.get(j));
            }
        }

        return dataset;
    }


    public JFreeChart graficaBarras(String titulo, String xAxis, String yAxis, DefaultCategoryDataset datos){

        return ChartFactory.createBarChart(
                titulo,
                xAxis,
                yAxis,
                datos
        );
    }


    public JFreeChart graficaLineasColor(String titulo, String xAxis, String yAxis, DefaultCategoryDataset datos){

        var cart= ChartFactory.createLineChart(
                titulo,
                xAxis,
                yAxis,
                datos
        );
        this.temaEstandar().apply(cart);
        return cart;
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

    public  JFreeChart createComboChart(String titulo, List<PortadaTotales> datos, String origen) {
        // Crear datasets separados
        DefaultCategoryDataset datasetBar = createBarDataset(datos, origen );
        DefaultCategoryDataset datasetLine = createLineDataset( datos );

        // Crear gráfico base con barras
        JFreeChart chart = ChartFactory.createBarChart(
                titulo,
                "Periodo",
                "Ventas",
                datasetBar, // solo las barras aquí
                PlotOrientation.VERTICAL,
                true, true, false
        );
        temaEstandar().apply(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        // --- Eje secundario para %
        NumberAxis rangeAxis2 = new NumberAxis("% Market Share");
        rangeAxis2.setNumberFormatOverride(new DecimalFormat("0.00%", DecimalFormatSymbols.getInstance(Locale.US)));
        plot.setRangeAxis(1, rangeAxis2);

        // Asignar el dataset del % al segundo rango
        plot.setDataset(1, datasetLine);
        plot.mapDatasetToRangeAxis(1, 1);

        // --- Renderer de barras (usa dataset 0)
        BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
        barRenderer.setSeriesPaint(0, Color.BLUE);   // Origen
        barRenderer.setSeriesPaint(1, Color.BLACK);  // Industria
        barRenderer.setItemMargin(0.05);

        // --- Renderer de línea (usa dataset 1)
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, Color.RED);
        lineRenderer.setSeriesStroke(0, new BasicStroke(2.5f));
        lineRenderer.setDefaultShapesVisible(true);
        lineRenderer.setDefaultShapesFilled(true);
        plot.setRenderer(1, lineRenderer);

        // Rotar etiquetas
        plot.getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.UP_45);

        //chart.getLegend().setItemFont(new Font("SansSerif", Font.BOLD, 12));

        return chart;
    }

    private  DefaultCategoryDataset createBarDataset(List<PortadaTotales> datos, String origen) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        datos.forEach(dato -> {
                    dataset.addValue(dato.getVolumen(), "Ventas origen " + origen, dato.getMes());
                    dataset.addValue(dato.getTotales(), "Ventas Industria", dato.getMes());
                });
        return dataset;
    }

    private  DefaultCategoryDataset createLineDataset(List<PortadaTotales> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        datos.forEach(dato ->
                dataset.addValue(dato.getPorcentaje(), "% Part. Ventas origen Brasil", dato.getMes())
        );

        return dataset;
    }

}
