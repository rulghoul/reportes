package com.organizame.reportes.utils;

import com.organizame.reportes.exceptions.ColorExcepcion;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utilidades {
    private static final Pattern rgb = Pattern
            .compile("(?i)\\#*(?<r>[0-9|a-f]{2})(?<g>[0-9|a-f]{2})(?<b>[0-9|a-f]{2})");

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
