package com.organizame.reportes.utils.presentacion;

import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.exceptions.PresentacionException;
import com.organizame.reportes.utils.Utilidades;
import com.organizame.reportes.utils.excel.dto.*;
import com.organizame.reportes.utils.presentacion.dto.ColorPresentacion;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.usermodel.*;
import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class CrearPresentacion {

    private static final Integer UNIDAD = 12; // 1 unidad = 1/6 pulgada (12 puntos)

    private final List<ColorPresentacion> colores;
    private final ColorPresentacion estandar;

    private final XMLSlideShow plantilla;
    private final XMLSlideShow trabajo;

    public CrearPresentacion(XMLSlideShow plantilla){
        this.trabajo = plantilla;
        this.plantilla = plantilla;
        this.colores = new ArrayList<>();
        estandar = new ColorPresentacion("Estandar", "FFFFFF", "000000", 11,true);
    }

    public CrearPresentacion(XMLSlideShow plantilla, List<ColorPresentacion> colores){
        this.trabajo = plantilla;
        this.plantilla = plantilla;
        this.colores = Objects.nonNull(colores) ? colores : new ArrayList<>();
        estandar = new ColorPresentacion("Estandar", "FFFFFF", "000000", 11,true);
    }

    public void agregarColor(ColorPresentacion color){
        this.colores.add(color);
    }

    public void cambiarColores(List<ColorPresentacion> colores){
        if(Objects.nonNull(colores) && !colores.isEmpty()) {
            this.colores.clear();
            this.colores.addAll(colores);
        }
    }

    public XSLFSlide crearDiapositiva(TipoDiapositiva tipo) {
        var diapositiva = tipo.equals(TipoDiapositiva.PORTADA)
                ? plantilla.getSlides().get(0)
                : plantilla.getSlides().get(1);
        return trabajo.createSlide(diapositiva.getSlideLayout());
    }

    private Rectangle getRectangle(PosicionGrafica posicion){
        return new Rectangle(
                posicion.getCol() * UNIDAD, // Se multiplica por doce para que cada unidad sea un sexto de pulgada
                posicion.getRow() * UNIDAD,
                posicion.getAncho() * UNIDAD,
                posicion.getAlto() * UNIDAD
        );
    }



    public void creaTexto(XSLFSlide diapositiva, String texto, PosicionGrafica portPos, String color)  throws PresentacionException{
        XSLFTextBox textBox = diapositiva.createTextBox();
        textBox.setAnchor(this.getRectangle(portPos));

        XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
        XSLFTextRun run = paragraph.addNewTextRun();
        run.setText(texto);
        run.setFontSize(14.0);
        run.setFontColor(Utilidades.convierteRGB(color));
        run.setFontFamily("Tahoma");
        run.setBold(true);


    }

    public void creaTablaEstilo(XSLFSlide diapositiva,  List<FilaTabla> acumuladosTabla, PosicionGrafica portPos) {
        XSLFTable table = diapositiva.createTable(acumuladosTabla.size(), acumuladosTabla.getFirst().getFila().size());
        table.setAnchor(this.getRectangle(portPos));

        for(int row = 0; row < acumuladosTabla.size(); row++){
            var fila =acumuladosTabla.get(row);
            this.aplicaEstiloFila(table, fila, row);
        }
    }

    private void aplicaEstiloFila(XSLFTable tabla, FilaTabla fila,  int row){
        var estilo = this.getColorPresentacion(fila.getNombreEstilo());
        for (int col = 0; col < tabla.getNumberOfColumns(); col++) {

            XSLFTableCell cell = tabla.getCell(row, col);
            cell.setFillColor(estilo.getFillColor());
            // Aplicar estilo de texto
            XSLFTextParagraph paragraph = cell.addNewTextParagraph();
            XSLFTextRun run = paragraph.addNewTextRun();
            run.setText(fila.getFila().get(col).toString());
            run.setFontSize(estilo.getFontSize());
            run.setFontColor(estilo.getFontColor());
            run.setBold(estilo.isBold());
        }
    }

    private ColorPresentacion  getColorPresentacion(String estilo){
        return this.colores.stream()
                .filter(color -> color.getNombre().equalsIgnoreCase(estilo))
                .findFirst().orElse(this.estandar);
    }

    public void insertarGrafica(XSLFSlide diapositiva, JFreeChart comboChart, PosicionGrafica posicionImagen, PosicionGrafica chart) throws GraficaException{
        try (ByteArrayOutputStream chartOut = new ByteArrayOutputStream()) {
            //la posicionGrafica chat nos da las medidas con que se genera la imagen de la grafica
            BufferedImage image = comboChart.createBufferedImage(chart.getAncho(), chart.getAlto(), BufferedImage.TYPE_INT_ARGB, null);
            ImageIO.write(image, "png", chartOut);
            // Pocision imagen nos direa las caracteristicas de la imagen en la presentacion
            this.insertarImagen(diapositiva, posicionImagen, chartOut.toByteArray());
        } catch (IOException e) {
            log.error("Error al generar gráfica: {}", e.getMessage(), e);
            throw new GraficaException(e);
        }
    }

    private void insertarImagen(XSLFSlide diapositiva, PosicionGrafica posicionGrafica, byte[] byteArray) {
        XSLFPictureData pictureData = diapositiva.getSlideShow().addPicture(byteArray, PictureData.PictureType.PNG);
        var picture = diapositiva.createPicture(pictureData);

        // Definir posición y tamaño
        picture.setAnchor(this.getRectangle(posicionGrafica));
    }


    public ByteArrayInputStream guardaPresentacion(){
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            trabajo.write(out);
            out.flush();
            return new ByteArrayInputStream(out.toByteArray());
        }catch (Exception e){
            throw new PresentacionException("Fallo el guardado de la presentacion por: " + e.getMessage());
        }
    }
}
