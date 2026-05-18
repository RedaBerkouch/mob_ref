/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: RenderUtils.java  25.02.2010 18:52:39 jfu $

 */
package ch.bfs.meb.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtility {
    private static final SimpleDateFormat MEB_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    static {
        MEB_DATE_FORMAT.setLenient(false);
    }

    public static String formatObject(Object value) {
        if (value == null)
            return "";

        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Long) {
            return formatLong((Long) value);
        } else if (value instanceof Boolean) {
            return formatBoolean((Boolean) value);
        } else if (value instanceof Date) {
            return formatDate((Date) value);
        }

        return "";
    }

    public static String formatDate(Date value) {
        if (value == null)
            return "";

        return ((SimpleDateFormat) MEB_DATE_FORMAT.clone()).format(value);
    }

    public static String formatBoolean(Boolean value) {
        if (value == null)
            return "";

        return value.booleanValue() ? "1" : "0";
    }

    public static String formatBoolean(boolean value) {
        return value ? "1" : "0";
    }

    public static String formatLong(Long value) {
        if (value == null)
            return "";

        return value.toString();
    }

    public static String formatString(String value) {
        if (value == null)
            return "";

        return value;
    }
}
