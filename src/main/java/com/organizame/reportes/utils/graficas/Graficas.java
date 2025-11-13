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
                .filter(dato -> dato.getPorcentaje() > 0.02)
                .forEach(dato ->
                dataset.addValue(dato.getPorcentaje() , dato.getModelo(), "Participacion")
        );
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
