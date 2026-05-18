/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.exception.SessionTimeoutException;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.AfterInitDocument.AfterInit;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.BeforeInitDocument.BeforeInit;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.CallDocument.Call;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.CellDocument.Cell;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.ColwidthDocument.Colwidth;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.HeadDocument.Head;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.RowDocument.Row;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.RowsDocument;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.RowsDocument.Rows;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.SettingsDocument.Settings;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.UserdataDocument.Userdata;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.result.DataDocument;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.result.DataDocument.Data;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.result.DataDocument.Data.Action;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.result.MessageDocument.Message;
import ch.bfs.meb.web.commons.exception.MebDhtmlxFileException;
import ch.bfs.meb.web.commons.i18n.ILocalizedCode;
import ognl.OgnlException;

/**
 * Abstract base class for all dhtmlx table managers.
 *
 * @author $Author$
 * @version $Revision$
 */
public abstract class TableManagerBase extends DhtmlxManagerBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TableManagerBase.class);

    public static final String SUFFIX = "TableManager";

    private static final JSNumber BUFFSIZE = new JSNumber(20);

    private static final Pattern XMLFRAGMENT1 = Pattern.compile("<xml-fragment>");
    private static final Pattern XMLFRAGMENT2 = Pattern.compile("</xml-fragment>");
    private static final Pattern XMLFRAGMENT3 = Pattern.compile("<xml-fragment/>");

    protected static final String ROW_DATA_IMPORTED_STYLE = "dhx_row_data_imported";
    protected static final String ROW_DELIVERY_IMPORTED_STYLE = "dhx_row_delivery_imported";
    protected static final String ROW_INITIALIZED_STYLE = "dhx_row_initialized";
    protected static final String ROW_DUMMY_ID = "dhx_row_dummy_id";
    protected static final String ROW_AMENDREPLACECONFIRMATION_STYLE = "dhx_row_arc";
    protected static final String ROW_NOT_VALID_STYLE = "dhx_row_not_valid";
    protected static final String ROW_VALID_STYLE = "dhx_row_valid";
    protected static final String ROW_PREVALIDATED_STYLE = "dhx_row_prevalidated";
    protected static final String ROW_VALIDATED_STYLE = "dhx_row_validated";
    protected static final String ROW_FINALIZED_STYLE = "dhx_row_finalized";

    protected final ArrayList<Column> _columns = new ArrayList<Column>();

    protected Column _identity;

    protected final ArrayList<Option> _beforeOptions = new ArrayList<Option>();
    protected final ArrayList<Option> _afterOptions = new ArrayList<Option>();

    protected DataProcessor _dataProcessor;

    protected boolean _autoLoad = false;
    protected boolean _resizeGrid = false;
    protected boolean _handleLoadError = false;

    private HashMap<String, Object> _data = new HashMap<String, Object>();
    private HashMap<String, Integer> _dataRowPos = new HashMap<String, Integer>();

    private int _dummyId = 0;

    public void addColumn(Column column) {
        _columns.add(column);

        if (column.isIdentity()) {
            _identity = column;
        }
    }

    public ArrayList<Column> getColumns() {
        return _columns;
    }

    public Integer getColumnIndexById(String id) {
        Iterator<Column> iter = _columns.iterator();
        int index = -1;
        while (iter.hasNext()) {
            Column col = iter.next();
            if (col.isVisible()) {
                index++;
            }
            if (col.getName().equals(id)) {
                return new Integer(index);
            }
        }
        return new Integer(index);
    }

    public String getColumnIdByIndex(Integer pIndex) {
        Iterator<Column> iter = _columns.iterator();
        int index = -1;
        while (iter.hasNext()) {
            Column col = iter.next();
            if (col.isVisible()) {
                index++;
            }
            if (pIndex.intValue() == index) {
                return col.getName();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> toArrayList(List<Object> objList) {
        ArrayList<T> destList = new ArrayList<T>();
        for (Object obj : objList) {
            destList.add((T) obj);
        }
        return destList;
    }

    protected void replaceAttributeIdsByName(List<WebWhereFilter> whereFilterList) {
        for (WebWhereFilter wf : whereFilterList) {
            try {
                wf.setAttribute(getColumnIdByIndex(Integer.valueOf(wf.getAttribute())));
            } catch (NumberFormatException e) {
                wf.setAttribute(null);
            }
            wf.setValue(wf.getValue().trim());
        }
    }

    public void addBeforeOption(Option option) {
        _beforeOptions.add(option);
    }

    public void addAfterOption(Option option) {
        _afterOptions.add(option);
    }

    public DataProcessor getDataProcessor() {
        return _dataProcessor;
    }

    public void setDataProcessor(DataProcessor dataProcessor) {
        _dataProcessor = dataProcessor;
    }

    public void enableAutoLoading() {
        _autoLoad = true;
    }

    public boolean isAutoLoadingEnabled() {
        return _autoLoad;
    }

    public void enableResizeGrid() {
        _resizeGrid = true;
    }

    public boolean isResizeGridEnabled() {
        return _resizeGrid;
    }

    public void enableLoadErrorHandling() {
        _handleLoadError = true;
    }

    protected String getRowStyleClass(Object row) {
        return null;
    }

    public Map<String, String> getRowUserData(Object row) {
        return new HashMap<String, String>();
    }

    protected void registerDummyRowSelect() {}

    /**
     * Returns the scripting part that initializes the fhtmris and the callbacks
     *
     * @return Javascript
     */
    public String getScriptingPart() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        buf.append("<script>");

        // Init table
        TableClientWrapper table = new TableClientWrapper(this, buf);

        table.construct();
        table.setImagePath(JSString.byVal("imgs/dhtmlxGrid/"));
        table.setDateFormat(JSString.byVal("%d.%m.%Y"));
        if (_autoLoad) {
            table.setXMLAutoLoading(new Command(CommandConstants.LOAD), getBuffSize());
        }
        if (_resizeGrid) {
            table.setResizeGrid(buf);
        }
        if (_handleLoadError) {
            table.setLoadErrorHandler();
        }
        table.loadXML(new Command(CommandConstants.INIT));

        if (getDataProcessor() != null) {
            buf.append(getDataProcessor().getScriptingPart());
        }

        // Base scripts
        registerDummyRowSelect();
        buf.append(super.getScriptingPart());

        buf.append("</script>");

        return buf.toString();
    }

    protected void addHeader(Rows rows, boolean ignoreEvents) {
        Head head = rows.addNewHead();

        if (!ignoreEvents) {
            // Initialisation part
            if (_beforeOptions.size() > 0) {
                BeforeInit binit = head.addNewBeforeInit();

                for (Option option : _beforeOptions) {
                    Call call = binit.addNewCall();
                    option.createCall(call);
                }
            }

            if (_afterOptions.size() > 0) {
                AfterInit ainit = head.addNewAfterInit();

                for (Option option : _afterOptions) {
                    Call call = ainit.addNewCall();
                    option.createCall(call);
                }
            }
        }

        // Column part
        for (Column col : _columns) {
            if (col.isVisible()) {
                col.createHeader(head.addNewColumn());
            }
        }

        // Set column width in %
        Settings settings = head.addNewSettings();
        Colwidth width = settings.addNewColwidth();
        XmlCursor cursor = width.newCursor();
        cursor.setTextValue("%");
        cursor.dispose();
    }

    private String stripXMLFragment(String raw) {

        String result = raw;

        result = XMLFRAGMENT1.matcher(result).replaceAll("");
        result = XMLFRAGMENT2.matcher(result).replaceAll("");
        result = XMLFRAGMENT3.matcher(result).replaceAll("");

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Object> toObjectList(RowsDocument document, Class clazz) throws DhtmlxException {
        ArrayList<Object> data = new ArrayList<Object>();

        try {
            Rows rowsDocument = document.getRows();

            List<Row> rows = Arrays.asList(rowsDocument.getRowArray());

            for (Row row : rows) {
                // Get id
                String id = row.getId();

                // create a now object that holds the data
                Object typedData = clazz.newInstance();

                // Set the identity column
                _identity.toObject(typedData, id);

                int colPos = 0;

                // set Columns
                for (Column col : _columns) {
                    if (col.isVisible()) {
                        String value = stripXMLFragment(row.getCellArray(colPos).xmlText());
                        col.toObject(typedData, value);
                        colPos++;
                    }
                }

                data.add(typedData);
            }
        } catch (InstantiationException e) {
            throw new DhtmlxException("Could not create object from XML", e);
        } catch (IllegalAccessException e) {
            throw new DhtmlxException("Could not create object from XML", e);
        } catch (OgnlException e) {
            throw new RuntimeException(e);
        }

        return data;
    }

    public DhtmlxTableXML toXMLStream(IListResultMapper result, boolean clear, boolean header) throws DhtmlxException {
        return toXMLStream(result, clear, header, false, null);
    }

    public DhtmlxTableXML toXMLStream(IListResultMapper result, boolean clear, boolean header, HashMap<String, String> tableUserData) throws DhtmlxException {
        return toXMLStream(result, clear, header, false, tableUserData);
    }

    public DhtmlxTableXML toXMLStream(IListResultMapper result, boolean clear, HashMap<String, String> tableUserData, boolean reloadCodeCombos)
            throws DhtmlxException {
        return toXMLStream(result, clear, reloadCodeCombos, reloadCodeCombos, tableUserData);
    }

    protected DhtmlxTableXML toXMLStream(IListResultMapper result, boolean clear, boolean header, boolean ignoreEvents, HashMap<String, String> tableUserData)
            throws DhtmlxException {
        RowsDocument document = RowsDocument.Factory.newInstance();
        Rows rows = document.addNewRows();

        long start = System.currentTimeMillis();

        // no error occured
        if (result.getState() == 1) {
            int rowPos = result.getPosition().intValue();
            rows.setPos(result.getPosition());
            rows.setTotalCount(result.getResultSize());
            if (header) {
                addHeader(rows, ignoreEvents);
            }

            // List is empty, create new data cache
            if (clear) {
                _data = new HashMap<String, Object>();
                _dataRowPos = new HashMap<String, Integer>();
            }

            for (Object row : result.getData()) {
                // Get identity of the dataset
                String pid;

                try {
                    pid = _identity.toValue(row).toString();
                } catch (OgnlException e) {
                    throw new DhtmlxException("Cannot map column '" + _identity.name + "'", e);
                } catch (NullPointerException e) {
                    throw new DhtmlxException("No identity column", e);
                }

                Row crow = rows.addNewRow();

                HashMap<String, String> userData = new HashMap<String, String>();
                userData.putAll(getRowUserData(row));

                if (_dataRowPos.containsKey(pid) && _dataRowPos.get(pid).intValue() != rowPos) {
                    // Set dummy identity
                    crow.setId("d" + (++_dummyId));

                    // set all cells to readOnly
                    String readOnlyCells = "";
                    for (Column col : _columns) {
                        if (!col.isIdentity() && !col.isHidden() && col.isVisible()) {
                            readOnlyCells += "1";
                        }
                    }
                    userData.put("readOnlyCells", readOnlyCells);

                    crow.setClass1(ROW_DUMMY_ID);
                } else {
                    // Set identity
                    crow.setId(pid);

                    // set style class
                    String styleClass = getRowStyleClass(row);
                    if (styleClass != null) {
                        crow.setClass1(styleClass);
                    }
                }

                // Add cells
                for (Column col : _columns) {
                    try {
                        if (col.isVisible()) {
                            Object val = col.toValue(row);
                            Cell cell = crow.addNewCell();
                            if (col.hasTooltip()) {
                                cell.setTitle(col.getTooltip(row));
                            }

                            XmlCursor cellCursor = cell.newCursor();
                            cellCursor.setTextValue(val.toString());
                            cellCursor.dispose();
                        }
                        if (col.hasUserData()) {
                            userData.putAll(col.getUserData(row));
                        }
                    } catch (OgnlException e) {
                        throw new DhtmlxException("Cannot map column '" + col.name + "'", e);
                    }
                }

                // Add userdata section
                if (!userData.isEmpty()) {
                    for (String key : userData.keySet()) {
                        Userdata xmlUserData = crow.addNewUserdata();
                        xmlUserData.setName(key);
                        XmlCursor userDataCursor = xmlUserData.newCursor();
                        userDataCursor.setTextValue(userData.get(key));
                        userDataCursor.dispose();
                    }
                }

                // Save object with identity column as index
                _data.put(pid, row);
                _dataRowPos.put(pid, new Integer(rowPos++));
            }

            // Add global userdata section
            if (tableUserData != null && !tableUserData.isEmpty()) {
                for (String key : tableUserData.keySet()) {
                    Userdata xmlUserData = rows.addNewUserdata();
                    xmlUserData.setName(key);
                    XmlCursor userDataCursor = xmlUserData.newCursor();
                    userDataCursor.setTextValue(tableUserData.get(key));
                    userDataCursor.dispose();
                }
            }
        }
        // Error occured
        else {
            LOGGER.error("Error while retrieving list: " + result.getMessage());

            return new DhtmlxTableXML(result.getMessage()) {
                public String getDocument() {
                    return _document;
                }
            };
        }

        // add message from result
        if ((result.getMessage() != null) && (!result.getMessage().equals(""))) {
            ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.MessageDocument.Message message = rows.addNewMessage();
            XmlCursor messageCursor = message.newCursor();
            messageCursor.setTextValue(result.getMessage());
            messageCursor.dispose();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("XML Gen Time:" + (System.currentTimeMillis() - start));
        }

        if (!validateXml(document)) {
            throw new DhtmlxException("Document not valid");
        }

        return new DhtmlxTableXML(document.xmlText());
    }

    public IHttpResult toErrorResponse(final String errorMessageId) {
        return new IHttpResult() {
            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public String getDocument() {
                return getLocalizationManager().getMessage(errorMessageId, null, getLocalizationManager().getMessage("unknown.error.message"));
            }

            @Override
            public String getContentDisposition() {
                return null;
            }
        };
    }

    public IHttpResult toPlausiErrorResponse(String plausiErrors) {
        plausiErrors = plausiErrors.replaceAll("[ ]+", " ");
        byte[] errorData;
        try {
            errorData = plausiErrors.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            errorData = plausiErrors.getBytes();
        }
        return new FileHttpResult(errorData, "plausierror.txt");
    }

    public IHttpResult toReplaceDataResponse(String rowId, String data1, String data2, String data3) {
        String data = rowId + "#" + data1 + "#" + data2 + "#" + data3;
        byte[] replaceData;
        try {
            replaceData = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            replaceData = data.getBytes();
        }
        return new FileHttpResult(replaceData, "replace.txt");
    }

    public static DhtmlxTableDataXML toXMLDataErrorStream(String errorMessage, String sid) {
        DataDocument document = DataDocument.Factory.newInstance();
        Data data = document.addNewData();
        Action action = data.addNewAction();

        action.setType("error");
        action.setSid(sid);
        Message message = action.addNewMessage();
        XmlCursor messageCursor = message.newCursor();
        messageCursor.setTextValue(errorMessage);
        messageCursor.dispose();

        return new DhtmlxTableDataXML(document.xmlText());
    }

    public DhtmlxTableDataXML toXMLDataStream(ISingleResultMapper result) throws DhtmlxException {
        // TODO dwi cleanup
        DataDocument document = DataDocument.Factory.newInstance();

        Data data = document.addNewData();

        Action action = data.addNewAction();

        String tid = result.getOriginalId();
        String sid = result.getOriginalId();
        action.setSid(sid);

        // no error occured
        if (result.getState() == 1) {
            action.setType(result.getCommand());

            // sid and tid are only different for insert commands
            if (result.getCommand().equals(CommandConstants.INSERT)) {
                try {
                    tid = _identity.toValue(result.getData()).toString();
                } catch (OgnlException e) {
                    throw new DhtmlxException("Cannot map column '" + _identity.name + "'", e);
                } catch (NullPointerException e) {
                    throw new DhtmlxException("No identity column", e);
                }
            }

            action.setTid(tid);

            // Add cells to action record to make an dynamic update possible
            // If a delete was detected, no record will be send
            if (!result.getCommand().equals(CommandConstants.DELETE)) {
                // Update cache
                updateCache(tid, result.getData());

                ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.result.RowDocument.Row crow = action.addNewRow();

                // set style class
                String styleClass = getRowStyleClass(result.getData());
                if (styleClass != null) {
                    crow.setClass1(styleClass);
                }

                HashMap<String, String> userData = new HashMap<String, String>();
                userData.putAll(result.getUserData());
                userData.putAll(getRowUserData(result.getData()));

                // Add cells
                Object obj = result.getData();

                for (Column col : _columns) {
                    try {
                        if (col.isVisible()) {
                            Object val = col.toValue(obj);
                            ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.result.CellDocument.Cell cell = crow.addNewCell();
                            if (col.hasTooltip()) {
                                cell.setTitle(col.getTooltip(obj));
                            }

                            XmlCursor cellCursor = cell.newCursor();
                            cellCursor.setTextValue(val.toString());
                            cellCursor.dispose();
                        }
                        if (col.hasUserData()) {
                            userData.putAll(col.getUserData(obj));
                        }
                    } catch (OgnlException e) {
                        throw new DhtmlxException("Cannot map column '" + col.name + "'", e);
                    }
                }
                // Add userdata section
                if (!userData.isEmpty()) {
                    for (String key : userData.keySet()) {
                        ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.result.UserdataDocument.Userdata xmlUserData = crow.addNewUserdata();
                        xmlUserData.setName(key);
                        XmlCursor userDataCursor = xmlUserData.newCursor();
                        userDataCursor.setTextValue(userData.get(key));
                        userDataCursor.dispose();
                    }
                }
            } else {
                // The row was deleted, delete from cache
                deleteFromCache(sid);

                // Add userdata section
                if (!result.getUserData().isEmpty()) {
                    ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.result.RowDocument.Row crow = action.addNewRow();
                    for (String key : result.getUserData().keySet()) {
                        ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.result.UserdataDocument.Userdata xmlUserData = crow.addNewUserdata();
                        xmlUserData.setName(key);
                        XmlCursor userDataCursor = xmlUserData.newCursor();
                        userDataCursor.setTextValue(result.getUserData().get(key));
                        userDataCursor.dispose();
                    }
                }
            }
        }
        // Error occured
        else {
            action.setType("error");

            LOGGER.error("Could save resultset: " + result.getMessage());
        }

        // add message from result
        if ((result.getMessage() != null) && (!result.getMessage().equals(""))) {
            Message message = action.addNewMessage();
            XmlCursor messageCursor = message.newCursor();
            messageCursor.setTextValue(result.getMessage());
            messageCursor.dispose();
        }

        if (!validateXml(document)) {
            throw new DhtmlxException("Document not valid");
        }

        return new DhtmlxTableDataXML(document.xmlText());
    }

    protected abstract List<? extends Object> getExportRows(ParameterList params);

    protected String getExportFileName() {
        return "Export.csv";
    }

    private void addCsvHeader(StringWriter writer) {
        String rowString = "";
        for (Column column : _columns) {
            if (column.isVisible() && !column.isHidden()) {
                if (rowString.length() > 0) {
                    rowString += ";";
                }
                rowString += column.getHeaderText();
            }
        }
        writer.write(rowString);
        writer.write("\n");
    }

    public IHttpResult exportCsv(ParameterList params) throws DhtmlxException {
        StringWriter writer = new StringWriter();

        try {
            List<? extends Object> rows = getExportRows(params);

            addCsvHeader(writer);

            for (Object row : rows) {
                String rowString = "";
                boolean isFirstColumn = true;

                // Add cells
                for (Column column : _columns) {
                    if (column.isVisible() && !column.isHidden()) {
                        if (!isFirstColumn) {
                            rowString += ";";
                        }

                        try {
                            String s = column.getDisplayString(row);
                            if (s.indexOf('/') >= 0) {
                                s = "\"" + s + "\"";
                            }
                            rowString += s;
                        } catch (OgnlException e) {
                            throw new DhtmlxException("Cannot map column '" + column.name + "'", e);
                        }

                        isFirstColumn = false;
                    }
                }

                writer.write(rowString);
                writer.write("\n");
            }
        } catch (Exception e) {
            throw new MebDhtmlxFileException("Couldn't create export csv!", e);
        } finally {
            writer.flush();
        }

        return new FileHttpResult(writer.toString().getBytes(StandardCharsets.UTF_8), getExportFileName());
    }

    /**
     * Merge object from cache with the column parameters.
     *
     * @param params Column parameters from grid
     * @return merged object or null, when the object was not in cache
     * @throws DhtmlxException Thrown when not all parameters could be mapped
     */
    public Object merge(final ParameterList params) throws DhtmlxException, OgnlException {
        String sid = params.getRowId();

        // get Object from cache
        Object obj = _data.get(sid);

        if (obj == null) {
            throw new SessionTimeoutException();
        }

        // merge
        return merge(obj, params);
    }

    public Object merge(Object obj, ParameterList params) throws DhtmlxException, OgnlException {
        if (obj != null) {

            int i = 0;

            for (Column column : _columns) {
                if (column.isVisible()) {
                    String value = "".equals(params.getColumnValue(i).trim()) ? null : params.getColumnValue(i).trim();
                    column.toObject(obj, value);
                    // column.toObject(obj, params.getColumnValue(i));
                    i++;
                }
                if (column.hasUserData()) {
                    column.userDataToObject(obj, params);
                }
            }

            return obj;
        }

        return null;
    }

    public void updateCache(String id, Object obj) {
        _data.put(id, obj);
    }

    public void deleteFromCache(String id) {
        _data.remove(id);
        _dataRowPos.remove(id);
    }

    public Collection<Object> getData() {
        return _data.values();
    }

    public Object getRowData(String rowId) {
        // get Object from cache
        Object obj = _data.get(rowId);
        if (obj == null) {
            throw new SessionTimeoutException();
        }
        return obj;
    }

    /**
     * Returns the column index for the visibleColumnIndex'th column.
     *
     * @param visibleColumnIndex Column index considering only visible columns
     * @return Column Index including hidden columns
     */
    public int getColumnIndex(int visibleColumnIndex) {
        int nrOfVisibleColumns = 0;
        int colIndex;
        for (colIndex = 0; colIndex < _columns.size(); colIndex++) {
            Column c = _columns.get(colIndex);
            if (c.isVisible()) {
                nrOfVisibleColumns++;
            }
            if (nrOfVisibleColumns - 1 == visibleColumnIndex) {
                break;
            }
        }

        return colIndex;
    }

    public JSNumber getBuffSize() {
        return BUFFSIZE;
    }

    /**
     * Get authorized cantons for current user (all cantons for EV and EA).
     *
     * @return cantons for current user (empty List if none defined)
     */
    public List<Long> getCantonsForActUser() {
        List<Long> allCantons;
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (CodegroupUtility.getCodeForRoleName(user.getRoleName()) < SecurityConstants.ROLE_EV) {
            return user.getCantons();
        } else {
            allCantons = new ArrayList<Long>();
            allCantons.add(new Long(-1));
            Collection<ILocalizedCode> cantonCodes = getLocalizationManager().getCodeGroupAllValues(CodegroupUtility.CANTON, true);
            Iterator<ILocalizedCode> i = cantonCodes.iterator();
            while (i.hasNext()) {
                ILocalizedCode code = (ILocalizedCode) i.next();
                allCantons.add(code.getKey());
            }
        }
        return allCantons;
    }
}
