/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Helper for string handling.
 *
 * @author $Author$
 * @version $Revision$
 */
public final class StringUtils {
    /**
     * Empty string constant
     */
    public static final String EMPTY_STRING = "";

    /**
     * Returns whether a string is empty, while empty means either
     * a string of zero length, containing only white space or being
     * <tt>null</tt>.
     *
     * @param text the text to check
     * @return <tt>true</tt> if emtpy
     */
    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0 || text.trim().length() == 0;
    }

    /**
     * Returns <tt>null</tt> if given text is empty or the text itself otherwise.
     *
     * @param text the text
     * @return <tt>null</tt> or the text
     */
    public static String nullForEmpty(String text) {
        return isEmpty(text) ? null : text;
    }

    public static String nullForEmptyAndTrim(String text) {
        return isEmpty(text) ? null : text.trim();
    }

    /**
     * Returns an empty string if given text is <tt>null</tt> or the text itself otherwise.
     *
     * @param text the text
     * @return <tt>null</tt> or the text
     */
    public static String emptyForNull(String text) {
        return text == null ? "" : text;
    }

    /**
     * Returns whether two strings are equal or not considering also null values.
     *
     * @param s1 first string
     * @param s2 second string
     * @return true if strings are equal, else false
     */
    public static boolean areEqual(String s1, String s2) {
        return ((isEmpty(s1) && isEmpty(s2)) || (!isEmpty(s1) && !isEmpty(s2) && s1.equals(s2)));
    }

    /**
     * Splits a string with a comma separated list of numbers in a list of Longs.
     *
     * @param list string with the list of numbers
     * @return the list of Longs (empty list if the param is null)
     */
    public static List<Long> splitLongs(String list) {
        List<Long> result = new ArrayList<Long>();
        if (list == null) {
            return result;
        }
        String[] array = list.split(",");
        for (String number : array) {
            try {
                result.add(new Long(number));
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        return result;
    }

    /**
     * Concats a list of Longs into a comma separated string.
     *
     * @param list the list of numbers
     * @return the concatenated string
     */
    public static String concatLongs(List<Long> list) {
        String result = "";
        for (Long number : list) {
            if (!result.equals("")) {
                result += ",";
            }
            result += number;
        }
        return result;
    }

    /**
     * Converts string with underscores to camel case
     *
     * @param str string with underscores
     * @return string in camel case
     */
    public static String asCamelCase(String str) {
        String camelStr = "";
        for (String p : str.split("_")) {
            if (!camelStr.equals("")) {
                p = p.substring(0, 1).toUpperCase() + p.substring(1);
            }
            camelStr += p;
        }
        return camelStr;
    }

    /**
     * Return a not null List containing all codes from a comma separated string
     *
     * @param codes
     * @return list of canton codes (empty list in case of format error)
     */
    public static List<Long> getCodesAsList(String codes) {
        List<Long> codeList = new ArrayList<Long>();
        if (codes != null) {
            // build cantons list
            try {
                StringTokenizer st = new StringTokenizer(codes, ",");
                while (st.hasMoreTokens()) {
                    Long canton = new Long(st.nextToken().trim());
                    codeList.add(canton);
                }
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        return codeList;
    }

    /**
     * Check if the format corresponds to the AHVN13 definition
     *
     * @param ahvNr the string containing the AHV number
     * @return true if the format is correct, false otherwise
     */
    public static boolean verifyAHVN13(String ahvNr) {
        if (ahvNr.length() != 13) {
            return false;
        } else if (!ahvNr.substring(0, 3).equals("756")) {
            return false;
        } else {
            try {
                int a = Integer.parseInt(ahvNr.substring(3, 4));
                int b = Integer.parseInt(ahvNr.substring(4, 5));
                int c = Integer.parseInt(ahvNr.substring(5, 6));
                int d = Integer.parseInt(ahvNr.substring(6, 7));
                int e = Integer.parseInt(ahvNr.substring(7, 8));
                int f = Integer.parseInt(ahvNr.substring(8, 9));
                int g = Integer.parseInt(ahvNr.substring(9, 10));
                int h = Integer.parseInt(ahvNr.substring(10, 11));
                int i = Integer.parseInt(ahvNr.substring(11, 12));
                int j = Integer.parseInt(ahvNr.substring(12));
                if (j != (10 - ((3 * (i + g + e + c + a + 5) + h + f + d + b + 6 + 7) % 10)) % 10) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a String value to a Boolean.<br />
     *
     * @param stringValue Boolean as String representation
     * @return null, if the given String is null or empty.<br />Boolean(true) if the String is equals to "1".<br />Boolean(false) if the String is equals to "0".
     */
    public static Boolean convertToBoolean(String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        }
        if (stringValue.trim().equals("1")) {
            return new Boolean(true);
        }
        if (stringValue.trim().equals("0")) {
            return new Boolean(false);
        }
        throw new IllegalArgumentException("The String '" + stringValue + "' could not be mapped to a Boolean!");
    }
}
