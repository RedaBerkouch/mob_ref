/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: ParameterColumn.java 483 2010-01-21 09:46:48Z dzw $

 */
package ch.bfs.meb.sba.web.dhtmlx.table;

import java.util.*;

import ch.bfs.meb.exception.SessionTimeoutException;
import ch.bfs.meb.sba.web.ws.sbaexport.Parameter;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.Column;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * Column for MacroParameter.
 * 
 * @author $Author: dzw $
 * @version $Revision: 483 $
 */
public class ExportParameterColumn extends Column {
    protected static final String PARAM_COLUMN_BASE_NAME = "name";
    protected static final int PARAM_COLUMN_WIDTH = 30;

    protected static final String PARAM_SEPARATOR = ";"; // must be set
    // according to grid
    // editor
    // dhtmlXGrid_excell_paramlist.js
    protected static final String PARAM_VALUE_SEPARATOR = "=";
    protected static final String PARAM_KEY_SEPARATOR = "#";

    protected String _rowIdName;

    /**
     * Map used to store parameters for each row in order according to display.
     * By that means the unique name of a parameter can be retrieved without
     * being stored in the data grid
     */
    protected final HashMap<String, Parameter> _paramMap = new HashMap<String, Parameter>();

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
    public ExportParameterColumn(String name, String header, String rowIdName, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        _rowIdName = rowIdName;

        setEditorType(EDITOR.PARAMLIST);
        setWidth(PARAM_COLUMN_WIDTH);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object toValue(Object raw) throws OgnlException {
        String paramColumnName = PARAM_COLUMN_BASE_NAME + getLocalizationManager().getLanguage();

        Object list = ognl.Ognl.getValue(expression, raw);
        String rowId = String.valueOf(ognl.Ognl.getValue(ognl.Ognl.parseExpression(_rowIdName), raw));

        String value = "";
        List values = null;
        if (list instanceof List) {
            values = (List) list;
        }

        if (values != null) {
            Object _paramNameExpression = ognl.Ognl.parseExpression(paramColumnName);

            values = sort(values);
            for (int i = 0; i < values.size(); ++i) {
                Parameter param = (Parameter) values.get(i);
                String parNameValue = (String) ognl.Ognl.getValue(_paramNameExpression, param);
                value += parNameValue;
                value += PARAM_VALUE_SEPARATOR;
                value += param.getDefaultValue() != null ? param.getDefaultValue() : "";

                if (i < values.size() - 1) {
                    value += PARAM_SEPARATOR + " ";
                }

                _paramMap.put(String.valueOf(rowId) + PARAM_KEY_SEPARATOR + String.valueOf(i), param);
            }
        }

        return value;
    }

    @Override
    public void toObject(Object object, Object value) throws DhtmlxException {
        try {
            ArrayList<Parameter> params = new ArrayList<Parameter>();
            Parameter param;

            // parse and create parameters
            if (value instanceof String) {
                String rowId = String.valueOf(ognl.Ognl.getValue(ognl.Ognl.parseExpression(_rowIdName), object));
                int paramIndex = 0;
                String remainingParams = new String((String) value);
                while (remainingParams.length() > 0) {
                    // Parse Parameter
                    int paramSeparatorIndex = remainingParams.indexOf(PARAM_SEPARATOR);
                    int endParam = (paramSeparatorIndex < 0) ? remainingParams.length() : paramSeparatorIndex;
                    String singleParam = remainingParams.substring(0, endParam);
                    remainingParams = remainingParams.substring((paramSeparatorIndex > 0) ? paramSeparatorIndex + PARAM_SEPARATOR.length() : endParam);

                    // create WebParameter
                    param = new Parameter();
                    int paramValSeparatorIndex = singleParam.indexOf(PARAM_VALUE_SEPARATOR);
                    if (_paramMap.size() == 0) {
                        throw new SessionTimeoutException();
                    }
                    String paramUniqueName = _paramMap.get(rowId + PARAM_KEY_SEPARATOR + String.valueOf(paramIndex)).getUniqueName();
                    param.setUniqueName(paramUniqueName);
                    String paramValue = singleParam.substring(paramValSeparatorIndex + PARAM_VALUE_SEPARATOR.length());
                    param.setDefaultValue(paramValue);
                    params.add(param);
                    paramIndex++;
                }
            }

            super.toObject(object, params);
        } catch (OgnlException e) {
            throw new DhtmlxException("Could not map value for column " + getName(), e);
        }
    }

    private List<Parameter> sort(List<Parameter> values) {
        Collections.sort(values, new Comparator<Parameter>() {
            @Override
            public int compare(Parameter o1, Parameter o2) {
                if (o1.getParameterOrder() == null && o2.getParameterOrder() == null) {
                    return 0;
                } else if (o1.getParameterOrder() == null) {
                    return 1;
                } else if (o2.getParameterOrder() == null) {
                    return -1;
                } else {
                    return o1.getParameterOrder().compareTo(o2.getParameterOrder());
                }
            }
        });
        return values;
    }
}