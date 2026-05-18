/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: BOBase.java 505 2008-02-25 22:57:45Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/** 
 * Abstract base class for business objects
 * 
 * @author  $Author: lsc $ 
 * @version $Revision: 505 $ 
 */
public abstract class BOBase {
    protected static final SimpleDateFormat SBG_DATE_FORMAT_XML = new SimpleDateFormat("yyyy-MM-dd");
    protected static final SimpleDateFormat SBG_DATE_FORMAT_CSV = new SimpleDateFormat("yyyyMMdd");
    static {
        SBG_DATE_FORMAT_XML.setLenient(false);
        SBG_DATE_FORMAT_CSV.setLenient(false);
    }

    protected static final String PLAUSIERROR_RESOURCE = "ch.admin.bfs.sbg.business.SbgServer";
    protected static final ResourceBundle resource_de = ResourceBundle.getBundle(PLAUSIERROR_RESOURCE, Locale.GERMAN);
    protected static final ResourceBundle resource_fr = ResourceBundle.getBundle(PLAUSIERROR_RESOURCE, Locale.FRENCH);

    /**
     * Basic formatting. Conversion from strings to concrete data types.
     */
    public abstract void format();

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
     * The date format is "yyyy-MM-dd" or "yyyyMMdd", as defined in SBG technical manual.
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
            parsedDate = ((SimpleDateFormat) SBG_DATE_FORMAT_XML.clone()).parse(string.trim());
        } catch (ParseException e) {}
        if (parsedDate == null) {
            try {
                parsedDate = ((SimpleDateFormat) SBG_DATE_FORMAT_CSV.clone()).parse(string.trim());
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
        return (date != null ? ((SimpleDateFormat) SBG_DATE_FORMAT_XML.clone()).format(date) : null);
    }

    /**
     * Converts strings "0" and "1" to Boolean. If conversion fails, null is returned.
     * 
     * @param 	string	Character string to convert
     * @return			The converted Boolean. Null, if string is neither "0" nor "1"
     */
    public Boolean verifyBoolean(String string) {
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

    public static ResourceBundle getResource_de() {
        return resource_de;
    }

    public static ResourceBundle getResource_fr() {
        return resource_fr;
    }
}