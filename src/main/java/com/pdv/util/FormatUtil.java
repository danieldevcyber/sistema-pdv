package com.pdv.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtil {

    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String formatarMoeda(BigDecimal valor) {
        if (valor == null) return "R$ 0,00";
        return CURRENCY_FORMAT.format(valor);
    }

    public static String formatarDataHora(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static BigDecimal parseMoeda(String texto) {
        try {
            String limpo = texto.replaceAll("[^0-9,]", "").replace(",", ".");
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
