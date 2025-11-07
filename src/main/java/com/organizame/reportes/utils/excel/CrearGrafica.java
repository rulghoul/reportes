package com.organizame.reportes.utils.excel;

import com.organizame.reportes.exceptions.GraficaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class CrearGrafica {

    private final SXSSFWorkbook wb;

    public CrearGrafica(SXSSFWorkbook wb){
        this.wb = wb;
    }

    public void insertarImagenBarras(SXSSFSheet hoja, int col, int row, DefaultCategoryDataset datos
            , String titulo, String xAxis, String yAxis) throws GraficaException {
        JFreeChart chart = ChartFactory.createBarChart(
                titulo,
                xAxis,
                yAxis,
                datos
        );

        this.insertarGrafica(hoja, col, row, chart, 1200, 800);
    }

    public void insertarGrafica(SXSSFSheet hoja, int col, int row,
                                JFreeChart chart, int width, int height) throws GraficaException {
        try (ByteArrayOutputStream chartOut = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(chartOut, chart, width, height);
            this.insertarImagen(hoja, col, row, chartOut.toByteArray());
        } catch (IOException e) {
            log.error("Error al generar gráfica: {}", e.getMessage(), e);
            throw new GraficaException(e);
        }
    }


    public void insertarImagen(SXSSFSheet hoja, int col, int row, byte[] bytes ){
        log.info("Se insertara la imagen en la columna:{} y fila {}", col, row);
        // Agrega la imagen al libro (Devuelve el índice de la imagen)

        int pictureIdx = wb.addPicture(bytes, wb.PICTURE_TYPE_PNG);

        // Crea el helper y el drawing patriarch (contenedor de imágenes/shapes)
        CreationHelper helper = wb.getCreationHelper();
        SXSSFDrawing drawing = hoja.createDrawingPatriarch();

        // Define la posición de la imagen en la hoja
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(col); // Columna inicial
        anchor.setRow1(row); // Fila inicial
        anchor.setCol2(col + 20); // Columna final (opcional)
        anchor.setRow2(row + 60); // Fila final (opcional)

        // Inserta la imagen
        Picture pict = drawing.createPicture(anchor, pictureIdx);

        // Ajusta la imagen al tamaño de las celdas (opcional)
        //pict.resize();
    }
}
