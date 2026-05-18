/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.server.commons.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.bfs.meb.util.StringUtils;

/** 
 * Abstract base class for business objects
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public abstract class BOBase {
    private static final SimpleDateFormat MEB_DATE_FORMAT_XML = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat MEB_DATE_FORMAT_CSV = new SimpleDateFormat("yyyyMMdd");
    static {
        MEB_DATE_FORMAT_XML.setLenient(false);
        MEB_DATE_FORMAT_CSV.setLenient(false);
    }
    protected static final int MAX_DELIVERY_DATA_LENGTH = 4000;

    protected String _confirmRules;
    private List<String> _plausiIdsToConfirm = null;

    /**
     * Basic formatting. Conversion from strings to concrete data types.
     */
    public abstract void format();

    /**
     * @return the confirmRules
     */
    public String getConfirmRules() {
        return _confirmRules;
    }

    public boolean canBeConfirmed(String plausiId) {
        if (!StringUtils.isEmpty(_confirmRules) && _plausiIdsToConfirm == null) {
            _plausiIdsToConfirm = new ArrayList<String>();
            for (String plausi : _confirmRules.split("\\|")) {
                _plausiIdsToConfirm.add(plausi.trim());
            }
        }
        if (_plausiIdsToConfirm != null && plausiId != null) {
            return _plausiIdsToConfirm.contains(plausiId);
        } else {
            return false;
        }
    }

    /**
     * Converts String to Long. If conversion fails, null is returned.
     * 
     * @param 	string	Character string to convert
     * @return			The converted number. Null, if conversion fails
     */
    public static Long verifyLong(String string) {
        if (string == null) {
            return null;
        }

        Long number;
        try {
            number = new Long(string.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        return number;
    }

    /**
     * Converts String to Date. If conversion fails, null is returned.
     * The date format is "yyyy-MM-dd" or "yyyyMMdd", as defined in SdL technical manual.
     * 
     * @param 	string	Character string to convert
     * @return			The converted date. Null, if conversion fails
     */
    public static Date verifyDate(String string) {
        if (string == null) {
            return null;
        }

        Date parsedDate = null;
        try {
            parsedDate = parseXmlDate(string.trim());
        } catch (ParseException e) {}
        if (parsedDate == null) {
            try {
                parsedDate = parseCsvDate(string.trim());
            } catch (ParseException e) {}
        }

        return parsedDate;
    }

    /**
     * Converts Date to String.
     * The date format is "yyyy-MM-dd", as defined in SBG technical manual.
     * 
     * @param 	date	Date to convert
     * @return			The converted string
     */
    public static String dateToString(Date date) {
        return (date != null ? formatXmlDate(date) : null);
    }

    /**
     * Converts String to BigDecimal with a scale of 3. If conversion fails, null is returned.
     * 
     * @param 	string	Character string to convert
     * @return			The converted number. Null, if conversion fails
     */
    public static BigDecimal verifyBigDecimal(String string) {
        if (string == null) {
            return null;
        }

        BigDecimal number;
        try {
            number = new BigDecimal(string);
        } catch (NumberFormatException e) {
            return null;
        }
        if (number.scale() > 3) {
            return null;
        } else {
            return number.setScale(3, RoundingMode.HALF_UP);
        }
    }

    /**
     * Converts BigDecimal to String.
     * 
     * @param 	number	BigDecimal to convert
     * @return			The converted string
     */
    public static String bigDecimalToString(BigDecimal number) {
        return number != null ? number.toPlainString() : null;
    }

    /**
     * Converts strings "0" and "1" to Boolean. If conversion fails, null is returned.
     * 
     * @param 	string	Character string to convert
     * @return			The converted Boolean. Null, if string is neither "0" nor "1"
     */
    public static Boolean verifyBoolean(String string) {
        Boolean flag = null;
        if (string != null) {
            if (string.trim().equals("0")) {
                flag = Boolean.FALSE;
            }
            if (string.trim().equals("1")) {
                flag = Boolean.TRUE;
            }
        }
        return flag;
    }

    public String renderLong(Long value) {
        return value != null ? value.toString() : null;
    }

    /**
     * Trims string to MAX_DELIVERY_DATA_LENGTH length.
     * 
     * @param 	string	Character string to trim
     * @return			The trimmed string
     */
    public static String trimDeliveryData(String string) {
        if (string != null && string.length() > MAX_DELIVERY_DATA_LENGTH) {
            return string.substring(0, MAX_DELIVERY_DATA_LENGTH);
        } else {
            return string;
        }
    }

    protected static Date parseXmlDate(String string) throws ParseException {
        if (string == null) {
            return null;
        }
        return ((SimpleDateFormat) MEB_DATE_FORMAT_XML.clone()).parse(string.trim());
    }

    protected static String formatXmlDate(Date date) {
        if (date == null) {
            return "";
        }
        return ((SimpleDateFormat) MEB_DATE_FORMAT_XML.clone()).format(date);
    }

    protected static Date parseCsvDate(String string) throws ParseException {
        if (string == null) {
            return null;
        }
        return ((SimpleDateFormat) MEB_DATE_FORMAT_CSV.clone()).parse(string.trim());
    }

    protected static String formatCsvDate(Date date) {
        if (date == null) {
            return "";
        }
        return ((SimpleDateFormat) MEB_DATE_FORMAT_CSV.clone()).format(date);
    }
}