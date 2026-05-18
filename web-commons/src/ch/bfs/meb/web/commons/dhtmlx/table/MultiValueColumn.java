package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.StringTokenizer;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

public class MultiValueColumn extends Column {
    protected static final String PARAM_SEPARATOR = ";";
    protected static final String PARAM_VALUE_SEPARATOR = "=";

    protected String _rowIdName;
    protected int _maxNrOfValues;
    protected String _paramName;

    public MultiValueColumn(String name, String header, String paramName, String rowIdName, int maxNrOfValues, IWebLocalizationManager manager, int width)
            throws DhtmlxException {
        super(name, header, manager);

        _rowIdName = rowIdName;
        _maxNrOfValues = maxNrOfValues;
        _paramName = paramName;

        setEditorType(EDITOR.PARAMLIST);
        setWidth(width);

        String defaultValue = "";
        for (int i = 0; i < _maxNrOfValues; ++i) {
            defaultValue += getLocalizationManager().getMessage(_paramName) + " " + (i + 1);
            defaultValue += PARAM_VALUE_SEPARATOR;
            //value += "";

            if (i < _maxNrOfValues - 1) {
                defaultValue += PARAM_SEPARATOR + " ";
            }
        }
        setDefault(defaultValue);
    }

    @Override
    public Object toValue(Object raw) throws OgnlException {
        String list = (String) ognl.Ognl.getValue(expression, raw);

        String value = "";
        int i = 0;
        StringTokenizer values = new StringTokenizer(list, PARAM_SEPARATOR);
        while (values.hasMoreTokens() && i < _maxNrOfValues) {
            value += getLocalizationManager().getMessage(_paramName) + " " + (i + 1);
            value += PARAM_VALUE_SEPARATOR;
            value += values.nextToken();

            if (i++ < _maxNrOfValues - 1) {
                value += PARAM_SEPARATOR + " ";
            }
        }

        for (; i < _maxNrOfValues; ++i) {
            value += getLocalizationManager().getMessage(_paramName) + " " + (i + 1);
            value += PARAM_VALUE_SEPARATOR;
            //value += "";

            if (i < _maxNrOfValues - 1) {
                value += PARAM_SEPARATOR + " ";
            }
        }

        return value;
    }

    @Override
    public void toObject(Object object, Object value) throws DhtmlxException, OgnlException {
        String values = "";
        boolean first = true;

        if (value instanceof String) {
            String remainingParams = new String((String) value);
            while (remainingParams.length() > 0) {
                int paramSeparatorIndex = remainingParams.indexOf(PARAM_SEPARATOR);
                int endParam = (paramSeparatorIndex < 0) ? remainingParams.length() : paramSeparatorIndex;
                String singleParam = remainingParams.substring(0, endParam);
                remainingParams = remainingParams.substring((paramSeparatorIndex > 0) ? paramSeparatorIndex + PARAM_SEPARATOR.length() : endParam);

                int paramValSeparatorIndex = singleParam.indexOf(PARAM_VALUE_SEPARATOR);
                String paramValue = singleParam.substring(paramValSeparatorIndex + PARAM_VALUE_SEPARATOR.length());
                if (!first) {
                    values += PARAM_SEPARATOR;
                }
                first = false;
                values += paramValue.trim();
            }
        }

        super.toObject(object, values);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.Column#getDisplayString(java.lang.Object)
     */
    @Override
    public String getDisplayString(Object row) throws OgnlException {
        return "\"" + toValue(row).toString() + "\"";
    }
}
