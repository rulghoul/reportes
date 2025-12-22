package com.organizame.reportes.utils;

import com.organizame.reportes.exceptions.ColorExcepcion;
import com.organizame.reportes.utils.excel.EstiloCeldaExcel;
import com.organizame.reportes.utils.excel.dto.LinkExcel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utilidades {
    private static final Pattern rgb = Pattern
            .compile("(?i)\\#*(?<r>[0-9|a-f]{2})(?<g>[0-9|a-f]{2})(?<b>[0-9|a-f]{2})");


    public static void trasnforma(XSSFWorkbook wb,Cell cell, Object valor, boolean par, EstiloCeldaExcel estilo, EstiloCeldaExcel rojo) {
        try {
            switch (valor) {
                case String s -> cell.setCellValue(s);
                case Double d -> {
                    cell.setCellValue(d);
                    if(Utilidades.evaluaNumero(d) == -1){
                        cell.setCellStyle(par ? rojo.getOddPorciento() : rojo.getNormalPorciento());
                    }else {
                        cell.setCellStyle(par ? estilo.getOddPorciento() : estilo.getNormalPorciento());
                    }
                }
                case Date d -> {
                    cell.setCellValue(d);
                    cell.setCellStyle(par ? estilo.getOddDate() : estilo.getNormalDate());
                }
                case BigDecimal bd -> {
                    cell.setCellValue(bd.doubleValue());
                    if(Utilidades.evaluaNumero(bd.doubleValue()) == -1){
                        cell.setCellStyle(par ? rojo.getOddPorciento() : rojo.getNormalPorciento());
                    }else {
                        cell.setCellStyle(par ? estilo.getOddPorciento() : estilo.getNormalPorciento());
                    }
                }
                case Integer i -> {
                    if(Utilidades.evaluaNumero(i) == -1){
                        cell.setCellStyle(par ? rojo.getOdd() : rojo.getNormal());
                    }else {
                        cell.setCellStyle(par ? estilo.getOddEntero() : estilo.getNormalEntero());
                    }
                    cell.setCellValue(i);
                }
                case Boolean b -> cell.setCellValue(b ? "VERDADERO" : "FALSO");
                case LinkExcel temp -> manejarArrayList(wb, cell, temp);
                case null, default -> {
                    System.out.println("No ESTIPULADO: " +
                            (valor != null ? valor.getClass().getTypeName() : "null"));
                }
            }
        } catch (Exception e) {
            log.warn("Fallo al colocar el valor por {}", e.getMessage());
            cell.setCellValue("");
        }
    }

    private static void manejarArrayList(XSSFWorkbook wb, Cell cell, LinkExcel temp) {
        if (!Objects.isNull(temp) && !Objects.isNull(temp.getUrl())
                && !Objects.isNull(temp.getLabel())) {
            cell.setCellValue(temp.getLabel());
            Hyperlink href = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
            href.setAddress(temp.getUrl());
            cell.setHyperlink(href);
        }
    }

    public static Color convierteRGB(String valor) throws ColorExcepcion {
        try{
            Matcher match = rgb.matcher(valor);
            while (match.find()) {
                int r = Integer.parseInt(match.group("r"), 16);
                int g = Integer.parseInt(match.group("g"), 16);
                int b = Integer.parseInt(match.group("b"), 16);
                return new Color(r, g, b);
            }
            throw new ColorExcepcion("No se pudo cargar el color " + valor);
        }catch (Exception e) {
            throw new ColorExcepcion("No se pudo cargar el color " + valor);
        }
    }


    public static String convertirNumeroAOrdinal(int numero) {
        return switch (numero) {
            case 1 -> "Primera";
            case 2 -> "Segunda";
            case 3 -> "Tercera";
            case 4 -> "Cuarta";
            case 5 -> "Quinta";
            case 6 -> "Sexta";
            case 7 -> "Séptima";
            case 8 -> "Octava";
            case 9 -> "Novena";
            case 10 -> "Décima";
            default -> numero + "ª";
        };
    }

    public static String convertirNumeroAOrdinalShort(int numero) {
        return switch (numero) {
            case 1 -> "1ro";
            case 2 -> "2do";
            case 3 -> "3ro";
            case 4 -> "4to";
            case 5 -> "5to";
            case 6 -> "6to";
            case 7 -> "7mo";
            case 8 -> "8vo";
            case 9 -> "9no";
            case 10 -> "10mo";
            default -> numero + "ª";
        };
    }

    public static String sanitazeName(String originalName){
        if (originalName == null) {
            return "Sheet";
        }

        // Replace invalid characters with a valid alternative
        String sanitized = originalName
                .replace('/', '_')    // Replace forward slash
                .replace('\\', '_')   // Replace backslash
                .replace('*', '_')    // Replace asterisk
                .replace('[', '_')    // Replace opening bracket
                .replace(']', '_')    // Replace closing bracket
                .replace(':', '_')    // Replace colon
                .replace('?', '_')    // Replace question mark
                .trim();              // Remove leading/trailing spaces

        // Handle periods at beginning or end
        if (sanitized.startsWith(".") || sanitized.endsWith(".")) {
            sanitized = sanitized.replaceFirst("^\\.", "").replaceFirst("\\.$", "");
        }

        return sanitized;
    }

    public static String sanitizeSheetName(String originalName) {

        var sanitized = Utilidades.sanitazeName(originalName);

        // Limit length to 31 characters (Excel's limit)
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }

        // If the result is empty, provide a default name
        if (sanitized.isEmpty()) {
            sanitized = "Sheet";
        }

        return sanitized;
    }

    public static int evaluaNumero(Number numero){
        if(numero.doubleValue() > 0d){
            return 1;
        }
        if(numero.doubleValue() == 0d){
            return 0;
        }
        return -1;
    }

}
