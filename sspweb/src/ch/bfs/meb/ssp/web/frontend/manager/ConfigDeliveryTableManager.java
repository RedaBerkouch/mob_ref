/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb
 */
package ch.bfs.meb.ssp.web.frontend.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.bfs.meb.util.MebDomain;
import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.ssp.web.frontend.resultmapper.ConfigDeliveryListTableResultMapper;
import ch.bfs.meb.ssp.web.frontend.resultmapper.ConfigDeliveryTableResultMapper;
import ch.bfs.meb.ssp.web.service.IConfigDeliveryService;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.ConfigDelivery;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.ConfigDeliveryListResult;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.ConfigDeliveryResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandDispatcher.EDIT;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.callback.InsertRowCallback.Master;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.COLOR;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.EditType;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ch.bfs.meb.web.commons.util.FilterContextUtility;
import ch.bfs.meb.web.commons.util.IFilterService;

/**
 * This Class represents a ConfigDeliveryTableManager for the init tab and acts
 * as a controller for the ConfigDelivery Table.
 */
@Scope("session")
@Component("configDeliveryTableManager")
public class ConfigDeliveryTableManager extends FilteredTableManagerBase {
    private static final int BUFFERLEN = 100;
    private static final JSNumber BUFFSIZE = new JSNumber(100);

    public static final String COLUMN_DELIVERYID_ID = "deliveryId";
    public static final String COLUMN_DELIVERYID_NAME_KEY = "configDeliveryTable.column.deliveryId.name";

    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "configDeliveryTable.column.canton.name";

    public static final String COLUMN_DELIVERYCODE_ID = "deliveryCode";
    public static final String COLUMN_DELIVERYCODE_NAME_KEY = "configDeliveryTable.column.deliveryCode.name";

    public static final String COLUMN_DEFAULT_ID = "isDefault";
    public static final String COLUMN_DEFAULT_NAME_KEY = "configDeliveryTable.column.default.name";

    public static final String COLUMN_DLUSERS_ID = "dlUsers";
    public static final String COLUMN_DLUSERS_NAME_KEY = "configDeliveryTable.column.dlUsers.name";
    public static final String COLUMN_DLUSERS_PARAMNAME_KEY = "configDeliveryTable.column.dlUsers.param.name";
    public static final String COLUMN_ROUSERS_ID = "roUsers";
    public static final String COLUMN_ROUSERS_NAME_KEY = "configDeliveryTable.column.roUsers.name";
    public static final String COLUMN_ROUSERS_PARAMNAME_KEY = "configDeliveryTable.column.roUsers.param.name";

    public static final String COLUMN_DUEDATE_ID = "dueDate";
    public static final String COLUMN_DUEDATE_NAME_KEY = "configDeliveryTable.column.dueDate.name";
    public static final String COLUMN_REFERENCEDATE_ID = "referenceDate";
    public static final String COLUMN_REFERENCEDATE_NAME_KEY = "configDeliveryTable.column.referenceDate.name";

    public static final String COLUMN_CREATIONDATE_ID = "creationDate";
    public static final String COLUMN_CREATIONDATE_NAME_KEY = "configDeliveryTable.column.creationDate.name";
    public static final String COLUMN_CREATIONUSER_ID = "creationUser";
    public static final String COLUMN_CREATIONUSER_NAME_KEY = "configDeliveryTable.column.creationUser.name";

    public static final String COLUMN_MODIFICATIONDATE_ID = "modificationDate";
    public static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "configDeliveryTable.column.modificationDate.name";
    public static final String COLUMN_MODIFICATIONUSER_ID = "modificationUser";
    public static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "configDeliveryTable.column.modificationUser.name";

    public static final String COLUMN_USERTEXT_ID = "userText";
    public static final String COLUMN_USERTEXT_NAME_KEY = "configDeliveryTable.column.userText.name";

    protected static final String CONFIGDELIVERY_MUSTBEMASTER_MESSAGE = "configdelivery.mustbemaster.message";

    public static final String MANAGER_NAME = "configDelivery";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IConfigDeliveryService _configDeliveryService;

    @Autowired
    private IFilterService _filterService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    private ConfigDeliveryFilterTableManager _filterTableManager;

    @Autowired
    private ConfigDeliveryWhereTableManager _whereTableManager;

    @Autowired
    private CantonTableManager _cantonTableManager;

    @Autowired
    private BurSchoolTableManager _burSchoolTableManager;

    @Autowired
    private CantonInterventionTableManager _interventionTableManager;

    private String _sortCol = COLUMN_CANTON_ID;

    private boolean _ascSort = true;

    public ConfigDeliveryTableManager() throws DhtmlxException {
        super();
    }

    /**
     * Return the name of the manager
     * 
     * @return the managers name
     */
    public String getName() {
        return MANAGER_NAME;
    }

    /**
     * Return the control name of the manager
     * 
     * @return the managers control name
     */
    public String getControlName() {
        return CONTROL_NAME;
    }

    /**
     * @see ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager#getLocalizationManager()
     */
    @Override
    public IWebLocalizationManager getLocalizationManager() {
        return _localizationManager;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.FilteredTableManagerBase#getFilterTableManager()
     */
    @Override
    protected FilterTableManagerBase getFilterTableManager() {
        return _filterTableManager;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.FilteredTableManagerBase#getWhereTableManager()
     */
    @Override
    protected WhereTableManagerBase getWhereTableManager() {
        return _whereTableManager;
    }

    private IDhtmlxControl getSchoolTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return BurSchoolTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return BurSchoolTableManager.MANAGER_NAME;
            }
        };
    }

    /**
     * Initializes a new DeliveryTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    public void create() throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        setFilterVersion(FilterContextUtility.getInitVersion(_filterService, CodegroupUtility.SSP_OBJECTTYPE_CONFIGURATION));
        if (user.getLastFilterCanton() == null) {
            setFilterCanton(getCantonsForActUser().get(0));
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        // This table is initialy master
        setMaster(true);

        addColumn(new IdentityColumn(COLUMN_DELIVERYID_ID, COLUMN_DELIVERYID_NAME_KEY, getLocalizationManager()));

        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 10);
        addColumn(cantonColumn);

        addColumn(new Column(COLUMN_DELIVERYCODE_ID, COLUMN_DELIVERYCODE_NAME_KEY, getLocalizationManager(), 8));
        addColumn(new CheckboxColumn(COLUMN_DEFAULT_ID, COLUMN_DEFAULT_NAME_KEY, getLocalizationManager()));
        addColumn(new MultiValueColumn(COLUMN_DLUSERS_ID, COLUMN_DLUSERS_NAME_KEY, COLUMN_DLUSERS_PARAMNAME_KEY, COLUMN_DELIVERYID_ID, 4,
                getLocalizationManager(), 20));
        addColumn(new MultiValueColumn(COLUMN_ROUSERS_ID, COLUMN_ROUSERS_NAME_KEY, COLUMN_ROUSERS_PARAMNAME_KEY, COLUMN_DELIVERYID_ID, 4,
                getLocalizationManager(), 20));

        DateColumn dateColumn = new DateColumn(COLUMN_DUEDATE_ID, COLUMN_DUEDATE_NAME_KEY, getLocalizationManager());
        addColumn(dateColumn);
        dateColumn = new DateColumn(COLUMN_REFERENCEDATE_ID, COLUMN_REFERENCEDATE_NAME_KEY, getLocalizationManager());
        addColumn(dateColumn);

        dateColumn = new DateColumn(COLUMN_CREATIONDATE_ID, COLUMN_CREATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        addColumn(dateColumn);
        addColumn(new ReadOnlyColumn(COLUMN_CREATIONUSER_ID, COLUMN_CREATIONUSER_NAME_KEY, getLocalizationManager(), 12));

        dateColumn = new DateColumn(COLUMN_MODIFICATIONDATE_ID, COLUMN_MODIFICATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        addColumn(dateColumn);
        addColumn(new ReadOnlyColumn(COLUMN_MODIFICATIONUSER_ID, COLUMN_MODIFICATIONUSER_NAME_KEY, getLocalizationManager(), 12));

        addColumn(new Column(COLUMN_USERTEXT_ID, COLUMN_USERTEXT_NAME_KEY, getLocalizationManager(), 20));

        if (!user.isRoleErhebung(MebDomain.SSP)) {
            for (Column a : _columns) {
                a.setEditType(EditType.readonly);
                a.setColor(COLOR.LIGHTGREY);
            }
        }

        // auto loading
        enableAutoLoading();

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, getSchoolTable(), _maintainglobals);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, getSchoolTable(), onRowSelectCallback, 250, _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, getSchoolTable(), _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, getSchoolTable(), _maintainglobals);
        IJavaScriptFunction refreshButtonsCallback = new RefreshConfigDeliveryButtonsCallback(this, !user.isRoleErhebung(MebDomain.SSP));
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this, getSchoolTable(), _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getSchoolTable(), _cantonTableManager);

        IJavaScriptFunction displayNumbersCallback = new DisplayNumbersCallback(this);

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setMultiselect(JSBoolean.istrue)));
        addBeforeOption(new Option(table.setOnSelectStateChangedHandler(onRowSelectDelay)));
        addBeforeOption(new Option(table.setOnEditCellHandler(onEditCellCallback)));
        addBeforeOption(new Option(table.setOnColumnSort(onColumnSortCallback)));
        addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));
        addBeforeOption(new Option(table.setOnAfterClick(onAfterClickCallback)));
        addBeforeOption(new Option(table.setOnGridReconstructedHandler(onGridReconstructedCallback)));

        // install load error handler
        enableLoadErrorHandling();

        // Data processor
        DataProcessor dataProcessor = new DataProcessor(this, onErrorCallback);
        dataProcessor.setRowMarkFunction(onRowMarkCallback);
        setDataProcessor(dataProcessor);

        // Register callbacks
        registerCallback(onRowSelectDelay);
        registerCallback(onRowSelectCallback);
        registerCallback(onEditCellCallback);
        registerCallback(onColumnSortCallback);
        registerCallback(new ShowSortImgCallback(this));
        registerCallback(onErrorCallback);
        registerCallback(new OnLoadErrorCallback(this, BurSchoolTableManager.MANAGER_NAME, CantonTableManager.MANAGER_NAME));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(refreshButtonsCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertRowCallback(this, getSchoolTable(), _cantonTableManager, Master.MANAGER_MUST_BE_MASTER, _maintainglobals,
                CONFIGDELIVERY_MUSTBEMASTER_MESSAGE));
        registerCallback(new DeleteRowCallback(this, getSchoolTable(), _maintainglobals));
        registerCallback(new SaveCallback(this, getSchoolTable(), _maintainglobals));
        registerCallback(new InitFilterCallback(this, getSchoolTable(), _cantonTableManager, _interventionTableManager, _filterTableManager, _whereTableManager,
                _maintainglobals));
        registerCallback(new SwitchMasterCallback(this, getSchoolTable(), _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(new NotImplementedCallback(this));
        registerCallback(new ExportCsvCallback(this, getSchoolTable(), null, false, _maintainglobals));
        registerCallback(onGridReconstructedCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(displayNumbersCallback);

        _whereTableManager.create(this);
    }

    /**
     * Gets all rows for config delivery Table with maximum buffer rows,
     * starting at start row.
     * 
     * @param start
     *            start from row index
     * @param buffer
     *            maximum number of rows
     * @return requested config delivery rows from start with buffer number of
     *         rows
     */
    public ConfigDeliveryListResult getRows(int start, int buffer) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _configDeliveryService.getConfigDeliveries(start, buffer, sortContext, getFilterContext(), getFilterVersion(),
                getFilterCanton());
    }

    /**
     * Gets all rows depending on the selected schoolIds.
     * 
     * @param selectedRowIds
     *            list with event ids
     * @return List with all config deliveries with given schoolId
     */
    public ConfigDeliveryListResult getRows(List<Long> selectedRowIds) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _configDeliveryService.getConfigDeliveriesOwnedBySchools(selectedRowIds, sortContext, getFilterVersion());
    }

    /**
     * Get rows using the parameters from the request
     * 
     * @param params
     *            Request parameters
     * @return List with persons
     */
    public ConfigDeliveryListResult getRows(ParameterList params) {
        int bufferlen = BUFFERLEN;
        if (params.hasParameter(ParameterConstants.PARAM_COUNT)) {
            bufferlen = params.getCount();
        }
        return getRows(params, params.getRowsLoaded(), bufferlen);
    }

    /**
     * Get rows using the parameters from the request
     * 
     * @param params
     *            Request parameters
     * @param start
     *            Start index 
     * @param buffer
     *            Buffer length
     * @return List with persons
     */
    public ConfigDeliveryListResult getRows(ParameterList params, int start, int buffer) {
        ConfigDeliveryListResult rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            setFilterVersion(_burSchoolTableManager.getVersion());

            if (selectedRowIds.size() == 0) {
                rows = new ConfigDeliveryListResult();
                rows.setState(ResultBase.OK);
            } else {
                rows = getRows(selectedRowIds);
            }
        } else {
            rows = getRows(start, buffer);
        }

        return rows;
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<ConfigDelivery> getExportRows(ParameterList params) {
        return getRows(params, -1, -1).getConfigDeliveries();
    }

    @Override
    protected String getExportFileName() {
        return "ConfigDeliveries.csv";
    }

    /**
     * Intializes the delivery table with all rows.
     * 
     * @param params
     *            contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        if (params.getRowsLoaded() == 0) {
            Long resultSize;

            ConfigDeliveryListResult result = getRows(params);
            resultSize = result.getMaxNrOfConfigDeliveries();
            ConfigDeliveryListTableResultMapper resultMapper = new ConfigDeliveryListTableResultMapper(result, getLocalizationManager(), resultSize,
                    params.getRowsLoaded());

            HashMap<String, String> userdata = new HashMap<String, String>();
            userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
            return toXMLStream(resultMapper, true, true, userdata);
        } else {
            return load(params);
        }
    }

    /**
     * Loads all rows by given parameters.
     * 
     * @param params
     *            contains all parameters
     * @return xml with all selected rows depending on the parent table
     *         selection who is in the param list
     * @throws DhtmlxException
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        Long resultSize;

        ConfigDeliveryListResult result = getRows(params);
        resultSize = result.getMaxNrOfConfigDeliveries();
        ConfigDeliveryListTableResultMapper resultMapper = new ConfigDeliveryListTableResultMapper(result, getLocalizationManager(), resultSize,
                params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        return toXMLStream(resultMapper, params.getRowsLoaded() == 0, false, userdata);
    }

    public DhtmlxTableXML sort(ParameterList params) throws DhtmlxException {
        _sortCol = getColumnIdByIndex(params.getColIndex());
        _ascSort = "asc".equalsIgnoreCase(params.getSortDirection());

        return load(params);
    }

    public IHttpResult filter(ParameterList params) throws DhtmlxException {
        int rowsloaded = params.getRowsLoaded();
        int bufferlen = BUFFERLEN;
        if (params.hasParameter(ParameterConstants.PARAM_COUNT)) {
            bufferlen = params.getCount();
        }

        String filterError = extractFilterParams(params);
        if (filterError != null) {
            return toErrorResponse(filterError);
        }

        if (CallbackConstants.InitVersionCallback.equals(params.getParameter(ParameterConstants.PARAM_FILTERCOMMAND))
                || CallbackConstants.InitVersionNoSyncCallback.equals(params.getParameter(ParameterConstants.PARAM_FILTERCOMMAND))) {
            _cantonTableManager.waitForVersionInitialized();
            _burSchoolTableManager.setInSynchBur();
        }

        Long resultSize;

        ConfigDeliveryListResult result = getRows(rowsloaded, bufferlen);
        resultSize = result.getMaxNrOfConfigDeliveries();
        ConfigDeliveryListTableResultMapper resultMapper = new ConfigDeliveryListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        return toXMLStream(resultMapper, rowsloaded == 0, false, userdata);
    }

    /**
     * Update delivery
     * 
     * @param params
     *            contains all parameters
     * @return XML with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // merge data record with cache
        ConfigDelivery configDelivery = (ConfigDelivery) merge(params);

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        configDelivery.setModificationUser(user.getEmail());

        ConfigDeliveryResult result = _configDeliveryService.updateConfigDelivery(configDelivery);

        // Maps result
        ConfigDeliveryTableResultMapper resultMapper = new ConfigDeliveryTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // Merge with an empty record
        ConfigDelivery configDelivery = (ConfigDelivery) merge(new ConfigDelivery(), params);
        configDelivery.setVersion(getFilterVersion());

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        configDelivery.setCreationUser(user.getEmail());

        ConfigDeliveryResult result = _configDeliveryService.insertConfigDelivery(configDelivery);

        // Maps result
        ConfigDeliveryTableResultMapper resultMapper = new ConfigDeliveryTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        ConfigDeliveryResult result = _configDeliveryService.deleteConfigDelivery((ConfigDelivery) getRowData(selected));

        // Maps result
        ConfigDeliveryTableResultMapper resultMapper = new ConfigDeliveryTableResultMapper(CommandConstants.DELETE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Gets all unmodified rows from DB. This method sets the initial values.
     * 
     * @param params
     * @return
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undo(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        if (!params.getEditorStatus().equals(EDIT.INSERT)) {
            ConfigDeliveryResult result = _configDeliveryService.getConfigDeliveryById(new Long(sid));

            // Maps result
            ConfigDeliveryTableResultMapper resultMapper = new ConfigDeliveryTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            ConfigDeliveryResult result = new ConfigDeliveryResult();
            result.setState(ResultBase.OK);
            result.setConfigDelivery(new ConfigDelivery());
            ConfigDeliveryTableResultMapper resultMapper = new ConfigDeliveryTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public String getExtraHtml(String partName) {
        if (partName.equals(ParameterConstants.PARAM_FILTERVERSION)) {
            return getFilterVersion().toString();
        } else if (partName.equals(ParameterConstants.PARAM_FILTERCANTON)) {
            String cantons = "";
            for (Long canton : getCantonsForActUser()) {
                String selected = "";
                if (canton.equals(getFilterCanton())) {
                    selected = " selected";
                }

                if (canton > 0L) {
                    cantons += "<option value=\"" + canton + "\"" + selected + ">"
                            + getLocalizationManager().getCodeGroupValueById(CodegroupUtility.CANTON, canton) + " (" + canton + ")</option>";
                } else {
                    cantons += "<option value=\"-1\"" + selected + ">&nbsp;</option>";
                }
            }
            return cantons;
        }
        return "";

    }

    public JSNumber getBuffSize() {
        return BUFFSIZE;
    }
}
