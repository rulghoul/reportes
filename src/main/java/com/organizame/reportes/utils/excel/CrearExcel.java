package com.organizame.reportes.utils.excel;


import com.organizame.reportes.dto.FilaTabla;
import com.organizame.reportes.exceptions.ExcelException;
import com.organizame.reportes.exceptions.GraficaException;
import com.organizame.reportes.utils.SpringContext;
import com.organizame.reportes.utils.excel.dto.Posicion;
import com.organizame.reportes.utils.excel.dto.PosicionGrafica;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.core.env.Environment;

import java.io.*;
import java.util.*;

/**
 *
 * @author raul.perez
 */
@Slf4j
@Getter
@Setter
public class CrearExcel {

    private final XSSFWorkbook wb;
    private final boolean cerrado;


    private final List<EstiloCeldaExcel> estilos;
    private final CrearGrafica graficas;

    private XSSFCellStyle encabezado;


    public CrearExcel() {
        wb = new XSSFWorkbook();
        cerrado = false;
        estilos = new ArrayList<>();
        this.estiloEncabezado();
        this.graficas = new CrearGrafica(wb);
    }

    public XSSFSheet CrearHoja(String hoja){
        if(cerrado){
            throw new ExcelException("El archivo ya se guardo, no se pueden agregar mas Hojas");
        }else{
            return wb.createSheet(hoja);
        }
    }


    public Posicion creaTabla(XSSFSheet hoja, List<List<Object>> datos, Integer columna, Integer  fila){
        Tabla tabla = Tabla.fromList(wb, estilos, encabezado, hoja, datos, columna, fila);
        var resultado = tabla.procesaTabla();
        return resultado;
    }

    public Posicion creaTablaEstilo(XSSFSheet hoja, List<FilaTabla> datos, Integer columna, Integer  fila){
        Tabla tabla = Tabla.fromFila(wb, estilos, encabezado, hoja, datos, columna, fila);
        var resultado = tabla.procesaTablaEstilo();
        return resultado;
    }


    public void InsertarGrafica(XSSFSheet hoja,
                                JFreeChart chart, PosicionGrafica pocicion){
        try {
            graficas.insertarGrafica(hoja, chart, pocicion);
        } catch (GraficaException e) {
            log.info("Fallo al generar la grafica");
        }
    }


    public void estiloEncabezado(){
        Environment env = SpringContext.getContext().getEnvironment();
        String fuenteNombre = env.getProperty("excel.font.name");
        Integer fuenteSize = env.getProperty("excel.font.size", Integer.class);
        //colores
        ColorExcel estandar = new ColorExcel("Estandar", "#FEFEFE", "F5F5F5");
        XSSFColor azulObscuro = estandar.ConvierteRGB("002B7F");
        XSSFColor gris = estandar.ConvierteRGB("96938E");

        CreationHelper createHelper = wb.getCreationHelper();

        encabezado = (XSSFCellStyle) wb.createCellStyle();
        Font resaltar = wb.createFont();
        resaltar.setFontName(fuenteNombre);
        resaltar.setFontHeightInPoints(fuenteSize.shortValue());
        resaltar.setBold(true);
        resaltar.setColor(IndexedColors.WHITE.getIndex());
        encabezado.setFont(resaltar);
        encabezado.setBorderTop(BorderStyle.MEDIUM);
        encabezado.setBorderBottom(BorderStyle.MEDIUM);
        encabezado.setBorderLeft(BorderStyle.MEDIUM);
        encabezado.setBorderRight(BorderStyle.MEDIUM);
        encabezado.setFillForegroundColor(azulObscuro);
        encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        estilos.add(new EstiloCeldaExcel(estandar, wb));
        //Estilo Stellantis
        var colorEstellantis = new ColorExcel("Stellantis", "96938E" ,"#C7C5C2");
        var estiloEstellantis = new EstiloCeldaExcel(colorEstellantis, wb);
        //estiloEstellantis.getOdd().setFont(resaltar);
        estilos.add(estiloEstellantis);

    }

    public void agregaColor(ColorExcel color) throws ExcelException {
        var temp = this.estilos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(color.getNombre()))
                .findFirst();
        if(temp.isPresent()){
            throw new ExcelException("El color ya existe");
        }else{
            this.estilos.add(new EstiloCeldaExcel(color, wb));
        }
    }

    public void TestGrafica(PosicionGrafica posicion,  XSSFSheet hoja) throws GraficaException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(23, "JFreeSVG", "Warm-up");
        dataset.addValue(11, "Batik", "Warm-up");
        dataset.addValue(42, "JFreeSVG", "Test");
        dataset.addValue(21, "Batik", "Test");

        this.agregarGrafica(posicion, "ejemplo", "x", "y", hoja, dataset);
    }

    public void agregarGrafica(PosicionGrafica posicion, String titulo, String xAxis, String yAxis , XSSFSheet hoja, DefaultCategoryDataset datos) throws GraficaException {
        graficas.insertarImagenBarras(hoja, posicion, datos, titulo, xAxis, yAxis );
    }

    public ByteArrayInputStream guardaExcel() throws IOException {
        var out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

}
