/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: SbgPlausiErrorColumn.java 2654 2012-10-22 06:12:10Z msc $

 */
package ch.admin.bfs.sbg.dhtmlx.table;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.Column;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterList;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * Column for plausi errors.
 * 
 * @author $Author: msc $
 * @version $Revision: 2654 $
 */
public class SbgPlausiErrorColumn extends Column {
    protected static final String ERRORMSG_COLUMN_BASE_NAME = "errorMsg";
    protected static final String MACRONAME_COLUMN_BASE_NAME = "plausiName";

    protected static final String ERRORID_NAME = "errorId";
    protected static final String CONFIRMABLE_NAME = "confirmable";
    protected static final String ISCONFIRMED_NAME = "isConfirmed";
    protected static final String MODUSER_NAME = "modificationUser";
    protected static final String MODDATE_NAME = "modificationDate";

    private static final String COLUMN_RULE_NAME_KEY = "plausierrorTable.column.rule.name";
    private static final String COLUMN_ERRORMSG_NAME_KEY = "plausierrorTable.column.errormsg.name";
    private static final String COLUMN_CONFIRMED_NAME_KEY = "plausierrorTable.column.isconfirmed.name";
    private static final String COLUMN_MODUSER_NAME_KEY = "plausierrerTable.column.modUser.name";
    private static final String COLUMN_MODDATE_NAME_KEY = "plausierrorTable.column.modDate.name";

    protected static final String KEY_SEPARATOR = "&";
    protected static final String KEY_VALUE_SEPARATOR = "=";
    protected static final String ERROR_SEPARATOR = "#";

    /**
     * Map used to store parameters for each row in order according to display.
     * By that means the unique name of a parameter can be retrieved without
     * being stored in the data grid
     */
    protected final HashMap<Long, Object> _errorMap = new HashMap<Long, Object>();

    // Only show the first MAX_NUMBER_ERRORS errors and generate an overflow
    // error message, if more errors exist
    protected static final int MAX_NUMBER_ERRORS = 100;
    protected static final Long OVERFLOW_ERROR_ID = new Long(0);
    protected static final String PLAUSIERROR_OVERFLOW_TEXT = "plausierror.overflow.text";
    protected static final String PLAUSIERROR_OVERFLOW_MESSAGE = "plausierror.overflow.message";

    /**
     * Constructor for a parameter column
     * 
     * @param name
     *            name of the parameter list attribute
     * @param header
     *            dispayed column title
     * @param rowIdName
     *            Id name of the row object
     * @param manager
     *            Language manager to resolve header name
     * @param localeSuffix
     *            langugae dependent suffix for accessing param name
     * @throws DhtmlxException
     */
    public SbgPlausiErrorColumn(String name, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, "", manager);

        setHidden(true);
        setVisible(false);
        setWidth(50);
    }

    @Override
    public boolean hasUserData() {
        return true;
    }

    private String overflowPlausierror() {
        String result = "";
        result += ERRORID_NAME;
        result += KEY_VALUE_SEPARATOR;
        result += OVERFLOW_ERROR_ID;
        result += KEY_SEPARATOR;
        result += MACRONAME_COLUMN_BASE_NAME;
        result += KEY_VALUE_SEPARATOR;
        result += getLocalizationManager().getMessage(PLAUSIERROR_OVERFLOW_MESSAGE);
        result += KEY_SEPARATOR;
        result += ERRORMSG_COLUMN_BASE_NAME;
        result += KEY_VALUE_SEPARATOR;
        result += getLocalizationManager().getMessage(PLAUSIERROR_OVERFLOW_TEXT);
        result += KEY_SEPARATOR;
        result += CONFIRMABLE_NAME;
        result += KEY_VALUE_SEPARATOR;
        result += "false";
        result += KEY_SEPARATOR;
        result += ISCONFIRMED_NAME;
        result += KEY_VALUE_SEPARATOR;
        result += "false";
        result += KEY_SEPARATOR;
        result += MODUSER_NAME;
        result += KEY_VALUE_SEPARATOR;
        result += "";
        result += KEY_SEPARATOR;
        result += MODDATE_NAME;
        result += KEY_VALUE_SEPARATOR;
        result += "";
        return result;
    }

    @Override
    public Map<String, String> getUserData(Object row) throws OgnlException {
        String language = getLocalizationManager().getLanguage();
        String errorTextName = ERRORMSG_COLUMN_BASE_NAME + language;
        String macroTextName = MACRONAME_COLUMN_BASE_NAME + language;

        Object list = ognl.Ognl.getValue(expression, row);

        String value = "";
        HashMap<String, String> resultMap = new HashMap<String, String>();

        if (list instanceof List<?>) {
            List<?> values = (List<?>) list;

            if (!values.isEmpty()) {
                String header = getLocalizationManager().getMessage(COLUMN_RULE_NAME_KEY) + KEY_SEPARATOR
                        + getLocalizationManager().getMessage(COLUMN_ERRORMSG_NAME_KEY) + KEY_SEPARATOR
                        + getLocalizationManager().getMessage(COLUMN_CONFIRMED_NAME_KEY) + KEY_SEPARATOR
                        + getLocalizationManager().getMessage(COLUMN_MODUSER_NAME_KEY) + KEY_SEPARATOR
                        + getLocalizationManager().getMessage(COLUMN_MODDATE_NAME_KEY);
                resultMap.put("plausierror_header", header);

                for (int i = 0; i < Math.min(values.size(), MAX_NUMBER_ERRORS); i++) {
                    Object error = values.get(i);
                    value += ERRORID_NAME;
                    value += KEY_VALUE_SEPARATOR;
                    Long errorId = ((Long) ognl.Ognl.getValue(ERRORID_NAME, error));
                    value += errorId.toString();
                    value += KEY_SEPARATOR;
                    value += MACRONAME_COLUMN_BASE_NAME;
                    value += KEY_VALUE_SEPARATOR;
                    value += (String) ognl.Ognl.getValue(macroTextName, error);
                    value += KEY_SEPARATOR;
                    value += ERRORMSG_COLUMN_BASE_NAME;
                    value += KEY_VALUE_SEPARATOR;
                    value += (String) ognl.Ognl.getValue(errorTextName, error);
                    value += KEY_SEPARATOR;
                    value += CONFIRMABLE_NAME;
                    value += KEY_VALUE_SEPARATOR;
                    value += ((Boolean) ognl.Ognl.getValue(CONFIRMABLE_NAME, error)).toString();
                    value += KEY_SEPARATOR;
                    value += ISCONFIRMED_NAME;
                    value += KEY_VALUE_SEPARATOR;
                    value += ((Boolean) ognl.Ognl.getValue(ISCONFIRMED_NAME, error)).toString();
                    value += KEY_SEPARATOR;
                    value += MODUSER_NAME;
                    value += KEY_VALUE_SEPARATOR;
                    value += (String) ognl.Ognl.getValue(MODUSER_NAME, error);
                    value += KEY_SEPARATOR;
                    value += MODDATE_NAME;
                    value += KEY_VALUE_SEPARATOR;
                    Date modDate = ((XMLGregorianCalendar) ognl.Ognl.getValue(MODDATE_NAME, error)).toGregorianCalendar().getTime();
                    value += new SimpleDateFormat("dd.MM.yyyy").format(modDate);

                    if (i < values.size() - 1) {
                        value += ERROR_SEPARATOR;
                    }

                    _errorMap.put(errorId, error);
                }

                if (values.size() > MAX_NUMBER_ERRORS) {
                    value += overflowPlausierror();
                }

                resultMap.put("plausierror", value.replaceAll("[ ]+", " "));
            }
        }

        return resultMap;
    }

    @Override
    public void userDataToObject(Object object, ParameterList params) throws DhtmlxException {
        try {
            String plausierrorString = params.getPlausierror();
            // List<?> list = (List<?>) ognl.Ognl.getValue (expression, object);

            // parse and create plausierrors
            if (!plausierrorString.equals("")) {
                String[] errorStringList = plausierrorString.split(ERROR_SEPARATOR);
                for (int i = 0; i < errorStringList.length; i++) {
                    Long id = new Long(0);
                    Boolean isConfirmed = false;
                    // Parse Plausierror string
                    String[] keys = errorStringList[i].split(KEY_SEPARATOR);
                    for (int j = 0; j < keys.length; j++) {
                        String[] keyValue = keys[j].split(KEY_VALUE_SEPARATOR);
                        for (int k = 0; k < keyValue.length; k++) {
                            if (keyValue[0].equals(ERRORID_NAME)) {
                                id = new Long(Long.parseLong(keyValue[1]));
                            }
                            if (keyValue[0].equals(ISCONFIRMED_NAME)) {
                                isConfirmed = new Boolean(Boolean.parseBoolean(keyValue[1]));
                            }
                        }
                    }

                    if (!id.equals(OVERFLOW_ERROR_ID)) {
                        // update Plausierror, if necessary
                        Object mapError = _errorMap.get(id);
                        if (!isConfirmed.equals(((Boolean) ognl.Ognl.getValue(ISCONFIRMED_NAME, mapError)))) {
                            ognl.Ognl.setValue(ISCONFIRMED_NAME, mapError, isConfirmed);
                        }
                    }
                }
            }

        } catch (OgnlException e) {
            throw new DhtmlxException("Could not map _value for column " + getName(), e);
        }
    }
}
