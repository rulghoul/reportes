package com.organizame.reportes.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ReportePDFService {


    public ByteArrayInputStream ConviertePPTtoPDF(ByteArrayInputStream presentacion) throws IOException {
        XMLSlideShow ppt = new XMLSlideShow(presentacion);
        Dimension pgsize = ppt.getPageSize();

        // Calcular dimensiones del PDF en puntos (72 puntos por pulgada)
        float pdfWidth = (float) pgsize.width / 96f * 72f;  // Asumiendo 96 DPI de las diapositivas
        float pdfHeight = (float) pgsize.height / 96f * 72f;

        PDDocument document = new PDDocument();

        for (XSLFSlide slide : ppt.getSlides()) {
            // Renderizar a alta resolución para mejor calidad
            int scale = 2; // Ajusta según necesidad: 2 = 200% de resolución original
            int imgWidth = pgsize.width * scale;
            int imgHeight = pgsize.height * scale;

            BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();

            // Configurar alta calidad de renderizado
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            graphics.setPaint(Color.WHITE);
            graphics.fillRect(0, 0, imgWidth, imgHeight);

            // Escalar el contexto gráfico para mantener proporciones
            graphics.scale(scale, scale);
            slide.draw(graphics);
            graphics.dispose();

            // Crear página con dimensiones correctas
            PDRectangle pageSize = new PDRectangle(pdfWidth, pdfHeight);
            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            // Insertar imagen en PDF con alta calidad
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, toByteArray(img, "png"), "slide.png");

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            // Dibujar imagen escalada al tamaño de la página
            contentStream.drawImage(pdImage, 0, 0, pdfWidth, pdfHeight);
            contentStream.close();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        ppt.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Método auxiliar para convertir BufferedImage a byte array
    private byte[] toByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
}
