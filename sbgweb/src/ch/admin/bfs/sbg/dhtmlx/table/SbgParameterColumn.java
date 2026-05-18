/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.admin.bfs.sbg.dhtmlx.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import ch.bfs.meb.exception.SessionTimeoutException;
import ch.bfs.meb.sbg.web.ws.sbgmacro.Parameter;
import ch.bfs.meb.sbg.web.ws.sbgmacro.SbgParameter;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterColumn;
import ch.bfs.meb.web.commons.exception.InputValidationException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.Ognl;
import ognl.OgnlException;

public class SbgParameterColumn extends ParameterColumn {
    /**
     * Map used to store parameters for each row in order according to display.
     * By that means the unique name of a parameter can be retrieved without
     * being stored in the data grid
     */
    protected final HashMap<String, Parameter> _paramMap = new HashMap<String, Parameter>();

    /**
     * Constructor for a parameter column
     *
     * @param name      name of the parameter list attribute
     * @param header    dispayed column title
     * @param rowIdName Id name of the row object
     * @param manager   Language manager to resolve header name
     * @param manager   langugae dependent suffix for accessing param name
     * @throws DhtmlxException
     */
    public SbgParameterColumn(String name, String header, String rowIdName, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, rowIdName, manager);
    }

    //FIXME eliminate this method and use super method, which is nearly identical (using WebParameters instead)
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

            for (int i = 0; i < values.size(); i++) {
                SbgParameter param = (SbgParameter) values.get(i);
                String parNameValue = (String) ognl.Ognl.getValue(_paramNameExpression, param);
                value += parNameValue;
                value += PARAM_VALUE_SEPARATOR;
                value += param.getDefaultValue() != null ? param.getDefaultValue() : "";

                if (i < values.size() - 1) {
                    value += PARAM_SEPARATOR;
                }

                _paramMap.put(String.valueOf(rowId) + PARAM_KEY_SEPARATOR + String.valueOf(i), param);
            }
        }

        return value;
    }

    //FIXME eliminate this method and use super method, which is nearly identical (using WebParameters instead)
    @Override
    public void toObject(Object object, Object value) throws DhtmlxException {
        try {
            List<Parameter> params = new ArrayList<Parameter>();
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

                    // create MacroParameter
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

            superSuperToObject(object, params);

        } catch (OgnlException e) {
            throw new DhtmlxException("Could not map value for column " + getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Deprecated //Use supers super toObject() instead.
    public void superSuperToObject(Object object, Object value) throws DhtmlxException {

        try {
            Object oldValue = Ognl.getValue(getExpression(), object);
            if (oldValue instanceof Collection) {
                Collection oldCollection = (Collection) oldValue;
                oldCollection.clear();
                if (value != null) {
                    oldCollection.addAll((Collection) value);
                }
            } else {
                Ognl.setValue(getExpression(), object, value);
            }
        } catch (OgnlException e) {
            if (e.getReason() != null) {
                throw new InputValidationException(localizationManager.getMessage("invalid.input.error.message", new String[] { getHeaderText() }),
                        e.getReason());
            } else {
                throw new DhtmlxException("Could not map value for column " + getName(), e);
            }
        }
    }
}
