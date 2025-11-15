package com.organizame.reportes.utils;

import com.organizame.reportes.exceptions.PresentacionException;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilidades {
    private static final Pattern rgb = Pattern
            .compile("(?i)\\#*(?<r>[0-9|a-f]{2})(?<g>[0-9|a-f]{2})(?<b>[0-9|a-f]{2})");

    public static Color convierteRGB(String valor) throws PresentacionException {
        try{
            Matcher match = rgb.matcher(valor);
            while (match.find()) {
                int r = Integer.parseInt(match.group("r"), 16);
                int g = Integer.parseInt(match.group("g"), 16);
                int b = Integer.parseInt(match.group("b"), 16);
                return new Color(r, g, b);
            }
            throw new PresentacionException("No se pudo cargar el color " + valor);
        }catch (Exception e) {
            throw new PresentacionException("No se pudo cargar el color " + valor);
        }
    }
}
