/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.RowsDocument;

/**
 * TODO Describe this class
 *
 * @author $Author$
 * @version $Revision$
 */
public class ParameterList {

    private static final Log LOGGER = LogFactory.getLog(ParameterList.class);

    protected final Map<String, String[]> _parameters;

    @SuppressWarnings("unchecked")
    public ParameterList(HttpServletRequest request) {

        _parameters = (Map<String, String[]>) request.getParameterMap();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Parameter: " + toString());
        }
    }

    public String getColumnValue(int column) {

        return getParameter("c" + column);
    }

    public String getRowId() {

        return getParameter(ParameterConstants.PARAM_ROWID);
    }

    public int getRowsLoaded() {
        return hasParameter(ParameterConstants.PARAM_ROWS_LOADED) ? Integer.valueOf(getParameter(ParameterConstants.PARAM_ROWS_LOADED)).intValue() : 0;
    }

    public int getCount() {
        return hasParameter(ParameterConstants.PARAM_COUNT) ? Integer.valueOf(getParameter(ParameterConstants.PARAM_COUNT)).intValue() : 0;
    }

    public int getColIndex() {

        String sortState = getParameter(ParameterConstants.PARAM_SORT_STATE);

        if (sortState == null || sortState.length() == 0) {
            return -1;
        }

        return Integer.parseInt(sortState.substring(0, sortState.indexOf(',')).trim());
    }

    public String getSortDirection() {

        String sortState = getParameter(ParameterConstants.PARAM_SORT_STATE);

        if (sortState == null || sortState.length() == 0) {
            return "ASC";
        }

        return sortState.substring(sortState.indexOf(',') + 1, sortState.length()).trim();
    }

    public String getCommand() {
        return getParameter(ParameterConstants.PARAM_COMMAND);
    }

    public ArrayList<Long> getSelectedRows() {
        ArrayList<Long> rows = new ArrayList<Long>();

        try {
            StringTokenizer tokenizer = new StringTokenizer(getParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS), ",");
            while (tokenizer.hasMoreElements()) {
                String row = tokenizer.nextToken();
                if (row != null && !row.equals("") && !row.equals("null")) {
                    rows.add(new Long(row));
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Cannot read row ids from url", e);
        }

        return rows;
    }

    public String getSelectedMaster() {
        return getParameter(ParameterConstants.PARAM_SELECTED_MASTER);
    }

    public String getEditorStatus() {
        return getParameter(ParameterConstants.PARAM_NATIVE_EDITOR_STATUS);
    }

    public String getRawData() {

        return getParameter(ParameterConstants.PARAM_RAW_DATA);
    }

    public RowsDocument getData() throws DhtmlxException {

        RowsDocument document;
        try {
            document = RowsDocument.Factory.parse(getRawData());
        } catch (XmlException e) {

            throw new DhtmlxException("Could not parse stream from control", e);
        }

        return document;
    }

    public RowsDocument getData(String dataName) throws DhtmlxException {

        RowsDocument document;
        try {
            document = RowsDocument.Factory.parse(getParameter(dataName));
        } catch (XmlException e) {

            throw new DhtmlxException("Could not parse stream from control", e);
        }

        return document;
    }

    public String getFileUploadName() {
        return getParameter(ParameterConstants.PARAM_FILENAME);
    }

    public String getPlausierror() {
        return getParameter(ParameterConstants.PARAM_PLAUSIERROR);
    }

    public Long getFilterVersion() {
        try {
            if (!hasParameter(ParameterConstants.PARAM_FILTERVERSION)) {
                return null;
            }
            return Long.parseLong(getParameter(ParameterConstants.PARAM_FILTERVERSION));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getFilterCanton() {
        try {
            if (!hasParameter(ParameterConstants.PARAM_FILTERCANTON)) {
                return null;
            }
            return Long.parseLong(getParameter(ParameterConstants.PARAM_FILTERCANTON));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Checks if the given parameter was transfered
     *
     * @param parameter The parameter to check
     * @return true if the parameter is available
     */
    public boolean hasParameter(String parameter) {
        return _parameters.containsKey(parameter);
    }

    public String getParameter(String parameter) {
        String result = "";
        if (_parameters.containsKey(parameter)) {
            String[] string = _parameters.get(parameter);
            if (string.length > 0) {
                result = string[0];
            }
        }
        return result;
    }

    @Override
    public String toString() {

        String paramList = "";

        for (Object par : _parameters.keySet()) {
            paramList += (par + "=" + ((String[]) _parameters.get(par))[0]) + ";";
        }

        return paramList;
    }
}
