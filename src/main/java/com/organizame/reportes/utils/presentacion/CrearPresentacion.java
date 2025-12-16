package com.organizame.reportes.utils.presentacion;

import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.exceptions.ColorExcepcion;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.exceptions.PresentacionException;
import com.organizame.reportes.utils.excel.dto.*;
import com.organizame.reportes.utils.presentacion.dto.ColorPresentacion;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.jfree.chart.JFreeChart;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class CrearPresentacion {

    private static final Integer UNIDAD = 6; // 1 unidad = 1/12 pulgada (6 puntos)
    private final Rectangle posicionFondo = new Rectangle(0,0,793,446);

    private final List<ColorPresentacion> colores;
    private final ColorPresentacion estandar;


    private final XMLSlideShow trabajo;
    private final XSLFPictureData picturePortada;
    private final XSLFPictureData pictureContenido;

    public CrearPresentacion(Resource bgContenido, Resource bgPortada) throws IOException {
        this.trabajo = new XMLSlideShow();
        this.trabajo.setPageSize(new Dimension(793, 446));
        this.colores = new ArrayList<>();
        estandar = new ColorPresentacion("Estandar", "FFFFFF", "000000", 11,true);
        this.picturePortada = trabajo.addPicture(bgPortada.getContentAsByteArray(), PictureData.PictureType.PNG);
        this.pictureContenido = trabajo.addPicture(bgContenido.getContentAsByteArray(), PictureData.PictureType.PNG);

    }

    public CrearPresentacion(Resource bgContenido, Resource bgPortada, List<ColorPresentacion> colores) throws IOException {
        this.trabajo = new XMLSlideShow();
        this.trabajo.setPageSize(new Dimension(793, 446));
        this.colores = Objects.nonNull(colores) ? colores : new ArrayList<>();
        estandar = new ColorPresentacion("Estandar", "FFFFFF", "000000", 11,true);
        this.picturePortada = trabajo.addPicture(bgPortada.getContentAsByteArray(), PictureData.PictureType.PNG);
        this.pictureContenido = trabajo.addPicture(bgContenido.getContentAsByteArray(), PictureData.PictureType.PNG);
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
        XSLFSlide diapositiva = trabajo.createSlide();
        PictureData backGround = tipo.equals(TipoDiapositiva.CONTENIDO)
            ? this.pictureContenido
                : this.picturePortada;
        XSLFPictureShape bg = diapositiva.createPicture(backGround);
        bg.setAnchor(this.posicionFondo); // Ajusta a tamaño de diapositiva
        return diapositiva;
    }

    private Rectangle getRectangle(PosicionGrafica posicion){
        return new Rectangle(
                posicion.getCol() * UNIDAD, // Se multiplica por doce para que cada unidad sea un sexto de pulgada
                posicion.getRow() * UNIDAD,
                posicion.getAncho() * UNIDAD,
                posicion.getAlto() * UNIDAD
        );
    }



    public void creaTexto(XSLFSlide diapositiva, String texto, PosicionGrafica portPos, String colorNombre)  throws PresentacionException, ColorExcepcion {
        var color = this.getColorPresentacion(colorNombre);
        XSLFTextBox textBox = diapositiva.createTextBox();
        textBox.setAnchor(this.getRectangle(portPos));

        XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
        XSLFTextRun run = paragraph.addNewTextRun();
        run.setText(texto);
        run.setFontSize(color.getFontSize());
        run.setFontColor(color.getFontColor());
        run.setFontFamily("Tahoma");
        run.setBold(color.isBold());


    }

    public void creaTablaEstilo(XSLFSlide diapositiva,  List<FilaTabla> acumuladosTabla, PosicionGrafica portPos, List<Integer> anchos) {
        XSLFTable table = diapositiva.createTable(acumuladosTabla.size(), acumuladosTabla.getFirst().getFila().size());
        table.setAnchor(this.getRectangle(portPos));

        for(int row = 0; row < acumuladosTabla.size(); row++){
            var fila =acumuladosTabla.get(row);
            this.aplicaEstiloFila(table, fila, row);
        }

        if (anchos.size() < table.getNumberOfColumns()) {
            log.warn("No se definieron suficientes anchos para todas las columnas");
        } else {
            for (int i = 0; i < table.getNumberOfColumns(); i++) {
                table.setColumnWidth(i, anchos.get(i) * UNIDAD);
            }
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

            if(col != 0){
                paragraph.setTextAlign(TextParagraph.TextAlign.RIGHT);
            }

            // Aplicar bordes negros finos
            cell.setBorderCap(TableCell.BorderEdge.bottom, StrokeStyle.LineCap.FLAT);
            cell.setBorderCap(TableCell.BorderEdge.top, StrokeStyle.LineCap.FLAT);
            cell.setBorderCap(TableCell.BorderEdge.left, StrokeStyle.LineCap.FLAT);
            cell.setBorderCap(TableCell.BorderEdge.right, StrokeStyle.LineCap.FLAT);

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

    public void insertarImagen(XSLFSlide diapositiva, PosicionGrafica posicionGrafica, byte[] byteArray) {
        XSLFPictureData pictureData = diapositiva.getSlideShow().addPicture(byteArray, PictureData.PictureType.PNG);

        var picture = diapositiva.createPicture(pictureData);
        // Definir posición y tamaño
        picture.setAnchor(this.getRectangle(posicionGrafica));
    }

    public void insertarImagen(XSLFSlide diapositiva, PosicionGrafica posicionGrafica, File imagen) throws IOException {
        FileInputStream inputStream = new FileInputStream(imagen);
        XSLFPictureData pictureData = diapositiva.getSlideShow().addPicture(inputStream.readAllBytes(), PictureData.PictureType.PNG);

        var picture = diapositiva.createPicture(pictureData);
        // Definir posición y tamaño
        picture.setAnchor(this.getRectangle(posicionGrafica));
    }

    public void creaLinea(XSLFSlide diapositiva, PosicionGrafica posicionGrafica, String color) {
        var estilo = this.getColorPresentacion(color);
        XSLFAutoShape linea = diapositiva.createAutoShape();
        linea.setShapeType(ShapeType.LINE);

        linea.setAnchor(this.getRectangle(posicionGrafica));

        linea.setLineColor(estilo.getFillColor());
        linea.setLineWidth(2.0);
        linea.setLineTailDecoration(LineDecoration.DecorationShape.ARROW);
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
