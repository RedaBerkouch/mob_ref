/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * This class describes a dhtmlxGrid column with a calendar control
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DateTimeColumn extends Column {
    private static final SimpleDateFormat MEB_DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    static {
        MEB_DATE_TIME_FORMAT.setLenient(false);
    }

    public static final int DEFAULT_DATE_COLUMN_WIDTH = 9;

    public DateTimeColumn(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager, DEFAULT_DATE_COLUMN_WIDTH);

        setEditorType(EDITOR.READ_ONLY);
        setColor(COLOR.LIGHTGREY);
        setSort(SORT.STRING);
        setDefault(formatDateTime(new Date()));
    }

    public Object toValue(Object raw) throws OgnlException {

        Object value = ognl.Ognl.getValue(expression, raw);

        if (value instanceof XMLGregorianCalendar) {
            Date date = ((XMLGregorianCalendar) value).toGregorianCalendar().getTime();

            return formatDateTime(date);
        }

        if (value == null) {
            return "";
        }

        return value;
    }

    public void toObject(Object object, Object value) throws DhtmlxException, OgnlException {
        XMLGregorianCalendar cal = null;

        if (value != null) {
            try {
                String dayString = (String) value;
                Date parsedDate = parseDateTime(dayString);
                GregorianCalendar gregCal = new GregorianCalendar(getLocalizationManager().getLocale());
                gregCal.setTime(parsedDate);
                cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal);
            } catch (ParseException e) {} catch (DatatypeConfigurationException e) {}
        }

        super.toObject(object, cal);
    }

    private static Date parseDateTime(String string) throws ParseException {
        if (string == null) {
            return null;
        }
        return ((SimpleDateFormat) MEB_DATE_TIME_FORMAT.clone()).parse(string.trim());
    }

    private static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return ((SimpleDateFormat) MEB_DATE_TIME_FORMAT.clone()).format(date);
    }
}
