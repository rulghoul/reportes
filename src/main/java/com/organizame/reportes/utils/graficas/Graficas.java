package com.organizame.reportes.utils.graficas;

import com.organizame.reportes.dto.DaoPeriodo;
import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.dto.auxiliar.PortadaTotales;
import com.organizame.reportes.utils.Utilidades;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;

@Slf4j
@Service
public class Graficas {

    private final StandardChartTheme tema;

    public Graficas(){
        this.tema = this.temaEstandar();
    }

    private StandardChartTheme temaEstandar() {
        StandardChartTheme tema = new StandardChartTheme("TemaOrganizame");

        // 🎨 Colores generales
        tema.setChartBackgroundPaint(Color.WHITE);
        tema.setPlotBackgroundPaint(new Color(245, 245, 245));
        tema.setDomainGridlinePaint(Color.LIGHT_GRAY);
        tema.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // 🔠 Fuentes
        tema.setExtraLargeFont(new Font("Tahoma", Font.BOLD, 18)); // título
        tema.setLargeFont(new Font("Tahoma", Font.PLAIN, 16));     // ejes
        tema.setRegularFont(new Font("Tahoma", Font.PLAIN, 14));   // etiquetas

        // 🎯 Colores de las series

        Paint[] coloresSeries = new Paint[]{
                new Color(70, 130, 180),   // Azul
                new Color(192, 80, 77),     // Rojo
                new Color(155, 187, 89),    // Verde
                new Color(255, 215, 0),     // Amarillo
                new Color(128, 0, 128),     // Morado
                new Color(30, 144, 255)     // Azul cielo
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
                dataset.addValue(dato.getPorcentaje() * 100 , dato.getModelo(), "Participacion")
        );
        return dataset;
    }

    public DefaultPieDataset generaPieDataset(List<DaoPeriodo> datos){
        DefaultPieDataset dataset = new DefaultPieDataset();
        datos.stream()
                .filter(dato -> !dato.getModelo().equalsIgnoreCase("TOTAL"))
                .filter(dato -> dato.getPorcentaje() > 0.02)
                .forEach(dato -> {
                        dataset.setValue(dato.getModelo(),  dato.getPorcentaje() * 100);
                       dataset.setValue(dato.getModelo(),  dato.getTotal());
                });
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


        this.temaEstandar().apply(chart);
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.UP_45
        );

        // --- Personalizar grosor de las líneas ---
        CategoryPlot plot = chart.getCategoryPlot();

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        Stroke grosor = new BasicStroke(3.5f);
        renderer.setDefaultStroke(grosor);
        plot.setRenderer(0, renderer);
        return chart;
    }

    public JFreeChart generarGraficaLineasMarcas(String titulo, List<FilaTabla> tabla) {
        DefaultCategoryDataset dataset = createDataset(tabla);

        JFreeChart chart = ChartFactory.createLineChart(
                titulo, "Mes", "Unidades Vendidas", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );

        this.tema.apply(chart);
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.UP_45
        );

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();

        // ✅Se coloca el estilo por default de las lineas
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##0"))
        );
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER)
        );
        renderer.setDefaultStroke(new BasicStroke(5.0f));

        // ✅ Estilos: Stellantis más gruesa y punteada

        int indiceResaltado = dataset.getRowIndex("STELLANTIS");
        // ✅ Aplicar estilo específico a la serie resaltada
        if (Objects.nonNull(indiceResaltado) && indiceResaltado >= 0) {
            renderer.setSeriesStroke(indiceResaltado, new BasicStroke(
                    6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
                    new float[]{10.0f, 6.0f}, 0.0f  // Línea punteada más gruesa
            ));
            renderer.setSeriesPaint(indiceResaltado, Utilidades.convierteRGB("002B7F")); // Azul Stellantis
            renderer.setSeriesShapesVisible(indiceResaltado, true); // Asegurar que los puntos sean visibles
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

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

    public JFreeChart createComboChart(String titulo, List<PortadaTotales> datos, String origen) {
        // Crear datasets separados
        DefaultCategoryDataset datasetBar = createBarDataset(datos, origen);
        DefaultCategoryDataset datasetLine = createLineDataset(datos);

        // Crear gráfico base con barras
        JFreeChart chart = ChartFactory.createBarChart(
                titulo,
                "Periodo",
                "Ventas",
                datasetBar,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        temaEstandar().apply(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        // --- Eje secundario para %
        NumberAxis rangeAxis2 = new NumberAxis("% Market Share");
        rangeAxis2.setNumberFormatOverride(new DecimalFormat("0.00%", DecimalFormatSymbols.getInstance(Locale.of("ES","mx"))));
        plot.setRangeAxis(1, rangeAxis2);
        rangeAxis2.setVisible(false);

        // Asignar el dataset del % al segundo rango
        plot.setDataset(1, datasetLine);
        plot.mapDatasetToRangeAxis(1, 1);

        // --- Renderer de barras
        BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
        barRenderer.setSeriesPaint(0, Utilidades.convierteRGB("4E95D9"));   // Origen
        barRenderer.setSeriesPaint(1, Color.BLACK);  // Industria
        barRenderer.setItemMargin(0.05);

        // --- Configurar labels de barras: VERTICALES (90°)
        barRenderer.setDefaultItemLabelsVisible(true);
        barRenderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##0")));

        // Rotación vertical: 90 grados (de izquierda a derecha)
        TextAnchor textBlockAnchor = TextAnchor.TOP_CENTER; // Etiqueta arriba del punto, girada
        double rotationRadians = Math.toRadians(180.0); // 90 grados en radianes

        // Aplicar rotación específica por serie si lo deseas, o global:
        ItemLabelPosition positionVertical = new ItemLabelPosition(
                ItemLabelAnchor.CENTER,
                TextAnchor.TOP_CENTER,
                textBlockAnchor,
                rotationRadians
        );

        // Aplicar posición con rotación a ambas series de barras
        barRenderer.setSeriesPositiveItemLabelPosition(0, positionVertical);
        //barRenderer.setSeriesPositiveItemLabelPosition(1, positionVertical);

        // --- Renderer de línea
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, Color.RED);
        lineRenderer.setSeriesStroke(0, new BasicStroke(3.0f));
        lineRenderer.setDefaultShapesVisible(true);
        lineRenderer.setDefaultShapesFilled(true);

        // Labels en la línea: sin rotación, sobre el punto
        lineRenderer.setDefaultItemLabelsVisible(true);
        lineRenderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}%", new DecimalFormat("0.00")));
        lineRenderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER)
        );

        plot.setRenderer(1, lineRenderer);

        plot.getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.UP_45);

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
                dataset.addValue(dato.getPorcentaje() *100, "% Part. Ventas origen Brasil", dato.getMes())
        );

        return dataset;
    }


    public  JFreeChart createChart(
            List<DaoPeriodo> ventas,
            String fabricanteDestacado,
            String titulo) {

        // Crear dataset directamente desde la lista ordenada
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (DaoPeriodo d : ventas) {
            dataset.addValue(d.getTotal(), "Ventas", d.getModelo());
        }

        // Crear gráfico
        JFreeChart chart = ChartFactory.createBarChart(
                titulo,
                "Cantidad",
                "Modelo",
                dataset,
                PlotOrientation.HORIZONTAL,
                false, // sin leyenda
                true,
                false
        );

        this.temaEstandar().apply(chart);

        CategoryPlot plot = chart.getCategoryPlot();

        // Colores
        Color colorDestacado = new Color(160, 160, 160);   // gris
        Color colorNormal = new Color(0, 32, 96);          // azul oscuro

        // Renderer con colores condicionales
        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                String modelo = dataset.getColumnKey(column).toString();
                // Buscar el fabricante de este modelo en la lista original
                for (DaoPeriodo v : ventas) {
                    if (v.getModelo().equals(modelo)) {
                        return v.getEstilo().equals(fabricanteDestacado) ? colorDestacado : colorNormal;
                    }
                }
                return colorNormal; // fallback
            }
        };

        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelPaint(Color.WHITE);
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        //renderer.setDrawBarOutline(false);

        plot.setRenderer(renderer);

        return chart;
    }


    public JFreeChart graficaDonut(String titulo, DefaultPieDataset datos) {
        var chart = ChartFactory.createRingChart(
                titulo,
                datos,
                true,   // leyenda
                true,   // tooltips
                false   // URLs
        );
        this.tema.apply(chart);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setSectionDepth(0.5); // Grosor del anillo (0.5 = 50% del radio)

        // ✅ Etiquetas: valor + porcentaje
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{1}; {2}",
                new DecimalFormat("#,##0"),
                new DecimalFormat("0%")
        ));
        plot.setLabelFont(new Font("Tahoma", Font.BOLD, 18));
        plot.setLabelPaint(Color.BLACK);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);


        // ✅ Estilo general
        chart.getTitle().setFont(new Font("Tahoma", Font.BOLD, 26));
        chart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        return chart;
    }

}
