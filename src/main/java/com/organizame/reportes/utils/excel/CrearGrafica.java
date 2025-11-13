package com.organizame.reportes.utils.excel;

import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.utils.excel.dto.PosicionGrafica;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class CrearGrafica {

    private final XSSFWorkbook wb;

    public CrearGrafica(XSSFWorkbook wb){
        this.wb = wb;
    }

    public void insertarImagenBarras(XSSFSheet hoja, PosicionGrafica posicion, DefaultCategoryDataset datos
            , String titulo, String xAxis, String yAxis) throws GraficaException {
        JFreeChart chart = ChartFactory.createBarChart(
                titulo,
                xAxis,
                yAxis,
                datos
        );

        this.insertarGrafica(hoja, chart, posicion);// 1200, 800);
    }

    public void insertarGrafica(XSSFSheet hoja,
                                JFreeChart chart, PosicionGrafica pocicion) throws GraficaException {
        try (ByteArrayOutputStream chartOut = new ByteArrayOutputStream()) {
            //ChartUtils.writeChartAsPNG(chartOut, chart, width, height);
            BufferedImage image = chart.createBufferedImage(pocicion.getAncho(), pocicion.getAlto(), BufferedImage.TYPE_INT_ARGB, null);
            ImageIO.write(image, "png", chartOut);
            this.insertarImagen(hoja, pocicion, chartOut.toByteArray());
        } catch (IOException e) {
            log.error("Error al generar gráfica: {}", e.getMessage(), e);
            throw new GraficaException(e);
        }
    }


    public void insertarImagen(XSSFSheet hoja, PosicionGrafica posicion, byte[] bytes ){

        int pictureIdx = wb.addPicture(bytes, wb.PICTURE_TYPE_PNG);

        CreationHelper helper = wb.getCreationHelper();
        XSSFDrawing drawing = hoja.createDrawingPatriarch();

        int colsOcupadas = Math.max(5, posicion.getAncho() / 110);
        int rowsOcupadas = Math.max(15, posicion.getAlto() / 35);

        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(posicion.getCol());
        anchor.setRow1(posicion.getRow());
        anchor.setCol2(posicion.getCol() + colsOcupadas);
        anchor.setRow2(posicion.getRow() + rowsOcupadas);

        // Inserta la imagen
        Picture pict = drawing.createPicture(anchor, pictureIdx);

        // Ajusta la imagen al tamaño de las celdas (opcional)
        //pict.resize();
    }
}
