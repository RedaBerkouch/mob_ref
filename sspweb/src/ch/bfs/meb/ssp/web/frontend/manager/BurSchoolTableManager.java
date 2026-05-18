/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb
 */
package ch.bfs.meb.ssp.web.frontend.manager;

import java.util.*;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.MebDomain;
import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.ssp.web.frontend.resultmapper.BurSchoolListTableResultMapper;
import ch.bfs.meb.ssp.web.frontend.resultmapper.BurSchoolTableResultMapper;
import ch.bfs.meb.ssp.web.service.IBurSchoolService;
import ch.bfs.meb.ssp.web.ws.sspburschool.BurSchool;
import ch.bfs.meb.ssp.web.ws.sspburschool.BurSchoolListResult;
import ch.bfs.meb.ssp.web.ws.sspburschool.BurSchoolResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandDispatcher.EDIT;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.COLOR;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * This Class represents a BurSchoolTableManager for the init tab and acts
 * as a controller for the ConfigDelivery Table.
 */
@Scope("session")
@Component("burSchoolTableManager")
public class BurSchoolTableManager extends FilteredTableManagerBase {
    private static final int BUFFERLEN = 100;
    private static final JSNumber BUFFSIZE = new JSNumber(100);

    public static final String COLUMN_SCHOOLID_ID = "schoolId";
    public static final String COLUMN_SCHOOLID_NAME_KEY = "burSchoolTable.column.schoolId.name";

    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "burSchoolTable.column.canton.name";

    public static final String COLUMN_BURNR_ID = "burNr";
    public static final String COLUMN_BURNR_NAME_KEY = "burSchoolTable.column.burNr.name";

    public static final String COLUMN_LABEL_ID = "label";
    public static final String COLUMN_LABEL_NAME_KEY = "burSchoolTable.column.label.name";
    public static final String COLUMN_IS_PUBLIC_SCHOOL_ID = "charPublFlg";
    public static final String COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY = "burSchoolTable.column.charPublFlg.name";
    public static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_ID = "charPrivSubFlg";
    public static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY = "burSchoolTable.column.charPrivSubFlg.name";
    public static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_ID = "charPrivNoSubFlg";
    public static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY = "burSchoolTable.column.charPrivNoSubFlg.name";
    public static final String COLUMN_IS_SPECIAL_SCHOOL_ID = "isSpecialSchool";
    public static final String COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY = "burSchoolTable.column.isSpecialSchool.name";
    public static final String COLUMN_DELIVERY_CODE_ID = "deliveryCode";
    public static final String COLUMN_DELIVERY_CODE_NAME_KEY = "burSchoolTable.column.deliveryCode.name";

    public static final String COLUMN_MUNICIPALITY_ID = "municipality";
    public static final String COLUMN_MUNICIPALITY_NAME_KEY = "burSchoolTable.column.municipality.name";

    public static final String COLUMN_SYNCHSTATUSBUR_ID = "synchStatusBur";
    public static final String COLUMN_SYNCHSTATUSBUR_NAME_KEY = "burSchoolTable.column.synchStatusBur.name";

    public static final String COLUMN_NAME_BUR_ID = "nameBur";
    public static final String COLUMN_NAME_BUR_NAME_KEY = "burSchoolTable.column.nameBur.name";

    public static final String COLUMN_IS_PUBLIC_SCHOOL_BUR_ID = "burCharPublFlg";
    public static final String COLUMN_IS_PUBLIC_SCHOOL_BUR_NAME_KEY = "burSchoolTable.column.burCharPublFlg.name";
    public static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_BUR_ID = "burCharPrivSubFlg";
    public static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_BUR_NAME_KEY = "burSchoolTable.column.burCharPrivSubFlg.name";
    public static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR_ID = "burCharPrivNoSubFlg";
    public static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR_NAME_KEY = "burSchoolTable.column.burCharPrivNoSubFlg.name";
    public static final String COLUMN_IS_SPECIAL_SCHOOL_BUR_ID = "isSpecialSchoolBur";
    public static final String COLUMN_IS_SPECIAL_SCHOOL_BUR_NAME_KEY = "burSchoolTable.column.isSpecialSchoolBur.name";

    public static final String COLUMN_CANTON_BUR_ID = "cantonBur";
    public static final String COLUMN_CANTON_BUR_NAME_KEY = "burSchoolTable.column.cantonBur.name";

    public static final String COLUMN_MUNICIPALITY_BUR_ID = "municipalityBur";
    public static final String COLUMN_MUNICIPALITY_BUR_NAME_KEY = "burSchoolTable.column.municipalityBur.name";

    public static final String COLUMN_VALIDFROM_BUR_ID = "validFromBur";
    public static final String COLUMN_VALIDFROM_BUR_NAME_KEY = "burSchoolTable.column.validFromBur.name";
    public static final String COLUMN_VALIDTO_BUR_ID = "validToBur";
    public static final String COLUMN_VALIDTO_BUR_NAME_KEY = "burSchoolTable.column.validToBur.name";

    public static final String COLUMN_USERTEXT_ID = "userText";
    public static final String COLUMN_USERTEXT_NAME_KEY = "burSchoolTable.column.userText.name";

    public static final String MANAGER_NAME = "burSchool";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IBurSchoolService _burSchoolService;
    @Autowired
    private IWebLocalizationManager _localizationManager;
    @Autowired
    private IGlobalJavaScript _maintainglobals;
    @Autowired
    private BurSchoolFilterTableManager _filterTableManager;
    @Autowired
    private BurSchoolWhereTableManager _whereTableManager;
    @Autowired
    private CantonTableManager _cantonTableManager;
    @Autowired
    private CantonInterventionTableManager _interventionTableManager;

    private String _sortCol = COLUMN_CANTON_ID;

    private boolean _ascSort = true;

    private BurFlagColumn isPublicSchoolBurColumn;
    private BurFlagColumn isPrivateSubsidisedSchoolBurColumn;
    private BurFlagColumn isPrivateNotSubsidisedSchoolBurColumn;
    private BurFlagColumn isSpecialSchoolBurColumn;
    private boolean _synchBur = false;
    private boolean _lastSynchBur = false;

    public BurSchoolTableManager() {
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

    private IDhtmlxControl getConfigDeliveryTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return ConfigDeliveryTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return ConfigDeliveryTableManager.MANAGER_NAME;
            }
        };
    }

    public Long getVersion() {
        return getFilterVersion();
    }

    /**
     * Initializes a new DeliveryTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    public void create() throws DhtmlxException {
        addColumn(new IdentityColumn(COLUMN_SCHOOLID_ID, COLUMN_SCHOOLID_NAME_KEY, getLocalizationManager()));

        Column codeColumn = new CodeColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(), CodegroupUtility.CANTON, 10);
        addColumn(codeColumn);

        addColumn(new ReadOnlyColumn(COLUMN_BURNR_ID, COLUMN_BURNR_NAME_KEY, getLocalizationManager(), 8));
        addColumn(new ReadOnlyColumn(COLUMN_LABEL_ID, COLUMN_LABEL_NAME_KEY, getLocalizationManager(), 10));

        CheckboxColumn checkBox = new CheckboxColumn(COLUMN_IS_PUBLIC_SCHOOL_ID, COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY, getLocalizationManager());
        checkBox.setColor(COLOR.LIGHTGREY);
        addColumn(checkBox);
        checkBox = new CheckboxColumn(COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_ID, COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY, getLocalizationManager());
        checkBox.setColor(COLOR.LIGHTGREY);
        addColumn(checkBox);
        checkBox = new CheckboxColumn(COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_ID, COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY, getLocalizationManager());
        checkBox.setColor(COLOR.LIGHTGREY);
        addColumn(checkBox);
        checkBox = new CheckboxColumn(COLUMN_IS_SPECIAL_SCHOOL_ID, COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY, getLocalizationManager());
        checkBox.setColor(COLOR.LIGHTGREY);
        addColumn(checkBox);

        addColumn(new Column(COLUMN_DELIVERY_CODE_ID, COLUMN_DELIVERY_CODE_NAME_KEY, getLocalizationManager(), 8));
        codeColumn = new MunicipalityCodeColumn(COLUMN_MUNICIPALITY_ID, COLUMN_MUNICIPALITY_NAME_KEY, getLocalizationManager(), CodegroupUtility.MUNICIPALITY,
                10);
        addColumn(codeColumn);
        codeColumn = new CodeColumn(COLUMN_SYNCHSTATUSBUR_ID, COLUMN_SYNCHSTATUSBUR_NAME_KEY, getLocalizationManager(), CodegroupUtility.MEB_SYNCHSTATUS, 8);
        addColumn(codeColumn);
        addColumn(new ReadOnlyColumn(COLUMN_NAME_BUR_ID, COLUMN_NAME_BUR_NAME_KEY, getLocalizationManager(), 10));

        isPublicSchoolBurColumn = new BurFlagColumn(COLUMN_IS_PUBLIC_SCHOOL_BUR_ID, COLUMN_IS_PUBLIC_SCHOOL_BUR_NAME_KEY, getLocalizationManager());
        addColumn(isPublicSchoolBurColumn);
        isPrivateSubsidisedSchoolBurColumn = new BurFlagColumn(COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_BUR_ID, COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_BUR_NAME_KEY,
                getLocalizationManager());
        addColumn(isPrivateSubsidisedSchoolBurColumn);
        isPrivateNotSubsidisedSchoolBurColumn = new BurFlagColumn(COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR_ID,
                COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR_NAME_KEY, getLocalizationManager());
        addColumn(isPrivateNotSubsidisedSchoolBurColumn);
        isSpecialSchoolBurColumn = new BurFlagColumn(COLUMN_IS_SPECIAL_SCHOOL_BUR_ID, COLUMN_IS_SPECIAL_SCHOOL_BUR_NAME_KEY, getLocalizationManager());
        addColumn(isSpecialSchoolBurColumn);

        codeColumn = new CodeColumn(COLUMN_CANTON_BUR_ID, COLUMN_CANTON_BUR_NAME_KEY, getLocalizationManager(), CodegroupUtility.CANTON, 10);
        addColumn(codeColumn);

        codeColumn = new MunicipalityCodeColumn(COLUMN_MUNICIPALITY_BUR_ID, COLUMN_MUNICIPALITY_BUR_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MUNICIPALITY, 10);
        addColumn(codeColumn);

        addColumn(new ReadOnlyColumn(COLUMN_VALIDFROM_BUR_ID, COLUMN_VALIDFROM_BUR_NAME_KEY, getLocalizationManager(), 8));
        addColumn(new ReadOnlyColumn(COLUMN_VALIDTO_BUR_ID, COLUMN_VALIDTO_BUR_NAME_KEY, getLocalizationManager(), 8));

        addColumn(new Column(COLUMN_USERTEXT_ID, COLUMN_USERTEXT_NAME_KEY, getLocalizationManager(), 20));

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isRoleErhebung(MebDomain.SSP)) {
            for (Column a : _columns) {
                a.setEditType(Column.EditType.readonly);
                a.setColor(COLOR.LIGHTGREY);
            }
        }

        // auto loading
        enableAutoLoading();

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, getConfigDeliveryTable(), null, false, _maintainglobals, false);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, getConfigDeliveryTable(), onRowSelectCallback, 250, _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, getConfigDeliveryTable(), _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, getConfigDeliveryTable(), _maintainglobals);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateCallback(this, getConfigDeliveryTable());
        IJavaScriptFunction refreshButtonsCallback = new RefreshBurSchoolButtonsCallback(this);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getConfigDeliveryTable(), _cantonTableManager);

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

        // Data processor
        DataProcessor dataProcessor = new DataProcessor(this, onErrorCallback);
        dataProcessor.setAfterUpdateFunction(onAfterUpdateCallback);
        dataProcessor.setRowMarkFunction(onRowMarkCallback);
        setDataProcessor(dataProcessor);

        // Register callbacks
        registerCallback(onRowSelectDelay);
        registerCallback(onRowSelectCallback);
        registerCallback(onEditCellCallback);
        registerCallback(onColumnSortCallback);
        registerCallback(new ShowSortImgCallback(this));
        registerCallback(onErrorCallback);
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(refreshButtonsCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new SaveCallback(this, getConfigDeliveryTable(), _maintainglobals));
        registerCallback(new InitFilterCallback(this, getConfigDeliveryTable(), _cantonTableManager, _interventionTableManager, _filterTableManager,
                _whereTableManager, _maintainglobals));
        registerCallback(new SwitchMasterCallback(this, getConfigDeliveryTable(), _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(new SynchronizeBurCallback(this, _maintainglobals));
        registerCallback(new GetAllBurDataCallback(this, _maintainglobals));
        registerCallback(new GetBurDataCallback(this));
        registerCallback(new NotImplementedCallback(this));
        registerCallback(onAfterUpdateCallback);
        registerCallback(new ExportCsvCallback(this, getConfigDeliveryTable(), null, true, _maintainglobals));
        registerCallback(onRowMarkCallback);

        registerCallback(displayNumbersCallback);

        _whereTableManager.create(this);
        _cantonTableManager.setBurSchoolTableManager(this);
    }

    /**
     * Gets all rows for config delivery Table with maximum buffer rows,
     * starting at start row.
     *
     * @param start  start from row index
     * @param buffer maximum number of rows
     * @return requested config delivery rows from start with buffer number of
     * rows
     */
    private BurSchoolListResult getRows(int start, int buffer) {
        setMaster(true);

        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        BurSchoolListResult schools;

        if (getFilterContext() == null || isInInit()) {
            setInInit(false);
            schools = new BurSchoolListResult();
            schools.setState(ResultBase.OK);
        } else {
            schools = _burSchoolService.getBurSchools(start, buffer, sortContext, getFilterContext(), getFilterVersion(), getFilterCanton(), _synchBur);
        }

        return schools;
    }

    /**
     * Gets all rows depending on the selected schoolIds.
     *
     * @param selectedRowIds list with event ids
     * @return List with all config deliveries with given schoolId
     */
    private BurSchoolListResult getRows(List<Long> selectedRowIds) {
        setMaster(false);

        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _burSchoolService.getBurSchoolsOwnedByConfigDeliveries(selectedRowIds, sortContext, _synchBur);
    }

    /**
     * Get rows using the parameters from the request
     *
     * @param params Request parameters
     * @return List with persons
     */
    private BurSchoolListResult getRows(ParameterList params) {
        int bufferlen = BUFFERLEN;
        if (params.hasParameter(ParameterConstants.PARAM_COUNT)) {
            bufferlen = params.getCount();
        }
        return getRows(params, params.getRowsLoaded(), bufferlen);
    }

    /**
     * Get rows using the parameters from the request
     *
     * @param params Request parameters
     * @param start  Start index
     * @param buffer Buffer length
     * @return List with persons
     */
    private BurSchoolListResult getRows(ParameterList params, int start, int buffer) {
        BurSchoolListResult rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            if (selectedRowIds.size() == 0) {
                rows = new BurSchoolListResult();
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
    protected List<BurSchool> getExportRows(ParameterList params) {
        return getRows(params, -1, -1).getSchools();
    }

    @Override
    protected String getExportFileName() {
        return "BurSchools.csv";
    }

    /**
     * Intializes the delivery table with all rows.
     *
     * @param params contains all parameters
     * @return xml with all rows
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        Long resultSize;

        setInInit(true);

        setInSynchBur(false);

        BurSchoolListResult result = getRows(params);
        resultSize = result.getMaxNrOfSchools();
        BurSchoolListTableResultMapper resultMapper = new BurSchoolListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("synchBur", _synchBur ? "1" : "0");
        _lastSynchBur = _synchBur;
        return toXMLStream(resultMapper, true, true, userdata);
    }

    /**
     * Loads all rows by given parameters.
     *
     * @param params contains all parameters
     * @return xml with all selected rows depending on the parent table
     * selection who is in the param list
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        Long resultSize;

        BurSchoolListResult result = getRows(params);
        resultSize = result.getMaxNrOfSchools();
        BurSchoolListTableResultMapper resultMapper = new BurSchoolListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("synchBur", _synchBur ? "1" : "0");
        boolean changeSynchBur = _lastSynchBur != _synchBur;
        _lastSynchBur = _synchBur;
        return toXMLStream(resultMapper, params.getRowsLoaded() == 0, changeSynchBur, userdata);
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

        String message = null;
        String filterCommand = params.getParameter(ParameterConstants.PARAM_FILTERCOMMAND);
        if (CallbackConstants.InitVersionCallback.equals(filterCommand) || CallbackConstants.InitVersionNoSyncCallback.equals(filterCommand)) {
            setInSynchBur(false);
            _cantonTableManager.waitForVersionInitialized();
        } else if (CallbackConstants.SynchronizeBurCallback.equals(filterCommand)) {
            setInSynchBur(false);
            BurSchoolListResult result = _burSchoolService.synchronizeSchools();
            if (result.getState() == ResultBase.OK) {
                setInSynchBur(true);
            }
            _sortCol = COLUMN_SYNCHSTATUSBUR_ID;
            _ascSort = false;
            message = result.getMessage();
        } else if (CallbackConstants.GetAllBurDataCallback.equals(filterCommand)) {
            setInSynchBur(true);
            BurSchoolListResult result = _burSchoolService.importBurSchools(getFilterCanton());
            _sortCol = COLUMN_SYNCHSTATUSBUR_ID;
            _ascSort = false;
            message = result.getMessage();
        }

        Long resultSize;

        BurSchoolListResult result = getRows(rowsloaded, bufferlen);
        if (message != null && !message.equals("")) {
            result.setMessage(message);
        }

        resultSize = result.getMaxNrOfSchools();
        BurSchoolListTableResultMapper resultMapper = new BurSchoolListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("synchBur", _synchBur ? "1" : "0");
        boolean changeSynchBur = _lastSynchBur != _synchBur;
        _lastSynchBur = _synchBur;
        return toXMLStream(resultMapper, rowsloaded == 0, changeSynchBur, userdata);
    }

    /**
     * Update delivery
     *
     * @param params contains all parameters
     * @return XML with updated row
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // merge data record with cache
        BurSchool burSchool = (BurSchool) merge(params);

        if (getFilterVersion() == null) {
            burSchool.setVersion(_cantonTableManager.getVersion());
        } else {
            burSchool.setVersion(getFilterVersion());
        }

        BurSchoolResult result;
        if (params.hasParameter(GetBurDataCallback.SYNCH_PARAMETER) && params.getParameter(GetBurDataCallback.SYNCH_PARAMETER).equals("1")) {
            result = _burSchoolService.importBurSchool(burSchool);
        } else {
            result = _burSchoolService.updateBurSchool(burSchool, _synchBur);
        }

        // Maps result
        BurSchoolTableResultMapper resultMapper;
        if (result.getState() == 1 && result.getSchool() == null) {
            resultMapper = new BurSchoolTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());
        } else {
            resultMapper = new BurSchoolTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        }

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        String sid = params.getRowId();
        BurSchool burSchool = (BurSchool) getRowData(sid);
        if (getFilterVersion() == null) {
            burSchool.setVersion(_cantonTableManager.getVersion());
        } else {
            burSchool.setVersion(getFilterVersion());
        }
        BurSchoolResult result = _burSchoolService.importBurSchool(burSchool);

        // Maps result
        BurSchoolTableResultMapper resultMapper = new BurSchoolTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Gets all unmodified rows from DB. This method sets the initial values.
     */
    public DhtmlxTableDataXML undo(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        if (!params.getEditorStatus().equals(EDIT.INSERT)) {
            BurSchoolResult result = _burSchoolService.getBurSchoolById(new Long(sid), _synchBur, getFilterVersion());

            // Maps result
            BurSchoolTableResultMapper resultMapper = new BurSchoolTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            BurSchoolResult result = new BurSchoolResult();
            result.setState(ResultBase.OK);
            result.setSchool(new BurSchool());
            BurSchoolTableResultMapper resultMapper = new BurSchoolTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    private void setInSynchBur(boolean synchBur) {
        if (_synchBur != synchBur) {
            _synchBur = synchBur;
            isPublicSchoolBurColumn.setSynchBur(_synchBur);
            isPrivateSubsidisedSchoolBurColumn.setSynchBur(_synchBur);
            isPrivateNotSubsidisedSchoolBurColumn.setSynchBur(_synchBur);
            isSpecialSchoolBurColumn.setSynchBur(_synchBur);
        }
    }

    public void setInSynchBur() {
        setInSynchBur(true);
    }

    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<>();
        BurSchool burSchool = (BurSchool) row;
        String readOnlyStr;

        if (isMaster() && (burSchool.getSynchStatusBur() == null || burSchool.getSynchStatusBur() != CodegroupUtility.MEB_SYNCHSTATUS_NEW)) {
            readOnlyStr = "1111111011111111110";
        } else {
            readOnlyStr = "1111111111111111110";
        }

        long version = (new GregorianCalendar()).get(Calendar.YEAR);
        boolean toDelete = !burSchool.isBurIsSsp() || burSchool.getBurActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_DELETED
                || burSchool.getBurActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_INACTIVE
                || burSchool.getBurActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_TRANSFERRED
                || (burSchool.getBurValidFromSsp() != null && burSchool.getBurValidFromSsp() > version)
                || (burSchool.getBurValidToSsp() != null && burSchool.getBurValidToSsp() < version);

        userData.put("readOnlyCells", readOnlyStr);
        userData.put("del", toDelete ? "1" : "0");
        return userData;
    }

    public JSNumber getBuffSize() {
        return BUFFSIZE;
    }
}
