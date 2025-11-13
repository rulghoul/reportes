package com.organizame.reportes.utils.graficas;

import com.organizame.reportes.dto.DaoPeriodo;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Collection;
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

    public JFreeChart LineChartFabricantes() {

        DefaultCategoryDataset dataset = createDataset();
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
        ); // Rotar etiquetas para que no se solapen
        return chart;
    }

    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Meses
        String[] meses = {
                "ene-2023", "feb-2023", "mar-2023", "abr-2023", "may-2023", "jun-2023",
                "jul-2023", "ago-2023", "sep-2023", "oct-2023", "nov-2023", "dic-2023",
                "ene-2024", "feb-2024", "mar-2024", "abr-2024", "may-2024", "jun-2024",
                "jul-2024", "ago-2024", "sep-2024", "oct-2024"
        };

        // Datos por fabricante
        String[] fabricantes = {"STELLANTIS", "VW", "RENAULT", "GM", "HYUNDAI", "HONDA"};
        int[][] datos = {
                {1094,2468,3303,3653,2828,2953,2903,3004,3045,1730,2527,4008,2557,2732,3650,2544,2650,1961,1715,2891,2530,2286},
                {1577,1706,1910,1801,1877,2777,3017,2531,2840,2637,2731,2370,2818,2318,1879,1818,1535,2154,1970,2236,2252,1833},
                {723,1000,3488,2810,2768,3382,2678,2411,2187,1978,2297,4612,2670,1377,2205,744,1096,2552,2171,2142,1549,1690},
                {1033,1332,1247,1147,932,822,787,932,1086,1118,1066,1681,1176,1123,3109,3007,2777,2982,2834,2484,2105,2453},
                {527,379,471,523,985,945,732,1178,1248,998,1120,693,703,682,443,511,419,345,465,405,268,395},
                {137,25,425,260,1092,647,447,236,789,555,967,542,580,518,713,780,791,719,1132,954,529,507}
        };

        for (int i = 0; i < fabricantes.length; i++) {
            for (int j = 0; j < meses.length; j++) {
                dataset.addValue(datos[i][j], fabricantes[i], meses[j]);
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
}
