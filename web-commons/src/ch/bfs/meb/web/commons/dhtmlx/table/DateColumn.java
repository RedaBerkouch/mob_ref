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
import ch.bfs.meb.web.commons.exception.InputValidationException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class describes a dhtmlxGrid column with a calendar control
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DateColumn extends Column {
    private static final Logger logger = LoggerFactory.getLogger(DateColumn.class);
    private static final SimpleDateFormat MEB_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    static {
        MEB_DATE_FORMAT.setLenient(false);
    }

    public static final int DEFAULT_DATE_COLUMN_WIDTH = 7;

    public DateColumn(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager, DEFAULT_DATE_COLUMN_WIDTH);

        setEditorType(EDITOR.CALENDAR);
        setSort(SORT.DATE);
        setDefault(formatDate(new Date()));
    }

    public Object toValue(Object raw) throws OgnlException {

        Object value = ognl.Ognl.getValue(expression, raw);

        if (value instanceof XMLGregorianCalendar) {
            Date date = ((XMLGregorianCalendar) value).toGregorianCalendar().getTime();

            return formatDate(date);
        }

        if (value == null) {
            return "";
        }

        return value;
    }

    public void toObject(Object object, Object value) throws DhtmlxException, OgnlException {
        XMLGregorianCalendar cal = null;
        String safeHeader = getHeaderText() != null ? getHeaderText() : "inconnue";

        if (value != null) {
            try {
                String dayString = (String) value;
                Date parsedDate = parseDate(dayString);
                GregorianCalendar gregCal = new GregorianCalendar(getLocalizationManager().getLocale());
                gregCal.setTime(parsedDate);
                cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal);
            } catch (ParseException e) {
                logger.warn("Erreur de parsing de date '{}' pour l'entête '{}'. Objet: {}", value, safeHeader, object.getClass().getName(), e);
                throw new InputValidationException(
                        localizationManager.getMessage("invalid.input.error.message", new String[] { getHeaderText() }),
                        e
                );
            } catch (DatatypeConfigurationException e) {
                logger.error("Erreur de création XMLGregorianCalendar pour '{}'. Objet: {}", value, object.getClass().getName(), e);
            }
        }

        super.toObject(object, cal);
    }


    private static Date parseDate(String string) throws ParseException {
        if (string == null) {
            return null;
        }
        return ((SimpleDateFormat) MEB_DATE_FORMAT.clone()).parse(string.trim());
    }

    private static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return ((SimpleDateFormat) MEB_DATE_FORMAT.clone()).format(date);
    }
}
