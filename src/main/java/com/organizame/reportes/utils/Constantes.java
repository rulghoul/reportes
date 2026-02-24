package com.organizame.reportes.utils;

import com.organizame.reportes.dto.Margen;

import java.math.BigDecimal;
import java.util.Locale;

public final class Constantes {
    public static Locale LOCALE_MX = new Locale("es", "MX");
    public static BigDecimal IVA =new BigDecimal("1.16");

    public static void calculaCierre(Margen margen){
        var resultado = margen.getMenudeoFlotilla() == 0
                ? 0
                : (Float.valueOf(margen.getInventarioActual())/Float.valueOf(margen.getMenudeoFlotilla()));
        Double cierre = resultado * 30.0;
        margen.setInventarioCierre(cierre.intValue());
    }
}
