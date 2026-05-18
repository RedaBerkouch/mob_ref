/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb
 */
package ch.bfs.meb.sdl.web.frontend.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.dhtmlx.callback.RefreshSchoolButtonsCallback;
import ch.bfs.meb.sdl.web.frontend.resultmapper.SchoolListTableResultMapper;
import ch.bfs.meb.sdl.web.frontend.resultmapper.SchoolTableResultMapper;
import ch.bfs.meb.sdl.web.service.ICantonService;
import ch.bfs.meb.sdl.web.service.ISchoolService;
import ch.bfs.meb.sdl.web.ws.sdlschool.*;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandDispatcher.EDIT;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
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

/**
 * This Class represents a SchoolTableManager for the maintain tab and acts
 * as a controller for the School Table.
 */
@Scope("session")
@Component("schoolTableManager")
public class SchoolTableManager extends FilteredTableManagerBase implements PlausiErrorColumn.IPlausiErrorDataUpdate {
    public static final String COLUMN_SCHOOLID_ID = "schoolId";

    //	private final static Logger LOGGER = LoggerFactory.getLogger(SchoolTableManager.class);
    public static final String COLUMN_SCHOOLID_NAME_KEY = "schoolTable.column.schoolId.name";
    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_VERSION_NAME_KEY = "schoolTable.column.version.name";
    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "schoolTable.column.canton.name";
    public static final String COLUMN_IDTYPE_ID = "idType";
    public static final String COLUMN_IDTYPE_NAME_KEY = "schoolTable.column.idtype.name";
    public static final String COLUMN_ID_ID = "id";
    public static final String COLUMN_ID_NAME_KEY = "schoolTable.column.schoolId.name";
    public static final String COLUMN_BUR_SCHOOL_LABEL_ID = "burSchoolLabel";
    public static final String COLUMN_BUR_SCHOOL_LABEL_NAME_KEY = "schoolTable.column.label.name";
    public static final String COLUMN_IS_PUBLIC_SCHOOL_ID = "charPublFlg";
    public static final String COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY = "schoolTable.column.charPublFlg.name";
    public static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_ID = "charPrivSubFlg";
    public static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY = "schoolTable.column.charPrivSubFlg.name";
    public static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_ID = "charPrivNoSubFlg";
    public static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY = "schoolTable.column.charPrivNoSubFlg.name";
    public static final String COLUMN_IS_SPECIAL_SCHOOL_ID = "isSpecialSchool";
    public static final String COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY = "schoolTable.column.isSpecialSchool.name";
    public static final String COLUMN_DELIVERYCODE_ID = "deliveryCode";
    public static final String COLUMN_DELIVERYCODE_NAME_KEY = "schoolTable.column.deliveryCode.name";
    public static final String COLUMN_DELIVERYSTATUS_ID = "deliveryStatus";
    public static final String COLUMN_DELIVERYSTATUS_NAME_KEY = "schoolTable.column.deliveryStatus.name";
    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    private String _sortCol = COLUMN_PLAUSISTATUS_ID;
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "schoolTable.column.plausiStatus.name";
    public static final String COLUMN_PLAUSIERROR_ID = "plausierrors";
    public static final String COLUMN_CREATIONUSER_ID = "creationUser";
    public static final String COLUMN_CREATIONUSER_NAME_KEY = "schoolTable.column.creationUser.name";
    public static final String COLUMN_CREATIONDATE_ID = "creationDate";
    public static final String COLUMN_CREATIONDATE_NAME_KEY = "schoolTable.column.creationDate.name";
    public static final String COLUMN_MODIFICATIONUSER_ID = "modificationUser";
    public static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "schoolTable.column.modificationUser.name";
    public static final String COLUMN_MODIFICATIONDATE_ID = "modificationDate";
    public static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "schoolTable.column.modificationDate.name";
    public static final String COLUMN_PREVELATIONUSER_ID = "prevalidationUser";
    public static final String COLUMN_PREVELATIONUSER_NAME_KEY = "schoolTable.column.prevalidationUser.name";
    public static final String COLUMN_PREVELATIONDATE_ID = "prevalidationDate";
    public static final String COLUMN_PREVELATIONDATE_NAME_KEY = "schoolTable.column.prevalidationDate.name";
    public static final String COLUMN_USERTEXT_ID = "userText";
    public static final String COLUMN_USERTEXT_NAME_KEY = "schoolTable.column.userText.name";
    public static final String MANAGER_NAME = "school";
    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;
    protected static final String ALERT_DELETE_SCHOOL_MESSAGE = "alert.delete.school.message";
    protected static final String SCHOOL_MUSTBEMASTER_MESSAGE = "school.mustbemaster.message";
    private static final JSNumber BUFFSIZE = new JSNumber(500);
    private static final int BUFFERLEN = 500;
    private final String UPDATE_LOCK = "SdlSchoolUpdateLock";
    protected final Map<Long, List<PlausiError>> _loadedPlausiErrors = new HashMap<>();
    @Autowired
    private ICantonService _cantonService;
    @Autowired
    private ISchoolService _schoolService;
    @Autowired
    private IWebLocalizationManager _localizationManager;
    @Autowired
    private IGlobalJavaScript _maintainglobals;
    @Autowired
    private SchoolFilterTableManager _filterTableManager;
    @Autowired
    private SchoolWhereTableManager _whereTableManager;
    @Autowired
    private ClassTableManager _classTableManager;
    @Autowired
    private LearnerTableManager _learnerTableManager;
    private boolean _ascSort = true;

    public SchoolTableManager() throws DhtmlxException {
        super();
    }

    /**
     * Return the name of the manager
     *
     * @return the managers name
     */
    @Override
    public String getName() {
        return MANAGER_NAME;
    }

    /**
     * Return the control name of the manager
     *
     * @return the managers control name
     */
    @Override
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

    public Long getCanton() {
        return getFilterCanton();
    }

    /**
     * Initializes a new SchoolTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    @Override
    public void create() throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        setFilterVersion(user.getLastFilterVersion());
        if (user.getLastFilterCanton() == null || user.getLastFilterCanton() <= 0) {
            List<Long> cantons = _cantonService.getFilterCantonsForActUser();
            setFilterCanton(cantons.get(0) > 0L ? cantons.get(0) : (cantons.size() > 1 ? cantons.get(1) : null));
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        // This table is initialy master
        setMaster(true);

        DateColumn dateColumn;

        addColumn(new IdentityColumn(COLUMN_SCHOOLID_ID, COLUMN_SCHOOLID_NAME_KEY, getLocalizationManager()));

        Column versionColumn = new Column(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 3);
        versionColumn.setEditType(EditType.editwheninserted);
        addColumn(versionColumn);

        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 3);
        cantonColumn.setEditType(EditType.readonly);
        cantonColumn.setColor(COLOR.LIGHTGREY);
        addColumn(cantonColumn);

        addColumn(new Column(COLUMN_IDTYPE_ID, COLUMN_IDTYPE_NAME_KEY, getLocalizationManager(), 4));
        addColumn(new Column(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager(), 7  ));
        Column burSchoolLabelColumn = new ReadOnlyColumn(COLUMN_BUR_SCHOOL_LABEL_ID, COLUMN_BUR_SCHOOL_LABEL_NAME_KEY, getLocalizationManager(), 8);
        addColumn(burSchoolLabelColumn);

        CheckboxColumn checkBox = new CheckboxColumn(COLUMN_IS_PUBLIC_SCHOOL_ID, COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY, getLocalizationManager());
        checkBox.setColor(COLOR.LIGHTGREY);
        checkBox.setEditType(EditType.readonly);
        addColumn(checkBox);
        checkBox = new CheckboxColumn(COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_ID, COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY, getLocalizationManager());
        checkBox.setColor(COLOR.LIGHTGREY);
        checkBox.setEditType(EditType.readonly);
        addColumn(checkBox);
        checkBox = new CheckboxColumn(COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_ID, COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY, getLocalizationManager());
        checkBox.setColor(COLOR.LIGHTGREY);
        checkBox.setEditType(EditType.readonly);
        addColumn(checkBox);
        checkBox = new CheckboxColumn(COLUMN_IS_SPECIAL_SCHOOL_ID, COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY, getLocalizationManager());
        checkBox.setColor(COLOR.LIGHTGREY);
        checkBox.setEditType(EditType.readonly);
        addColumn(checkBox);

        Column deliveryCodeColumn = new Column(COLUMN_DELIVERYCODE_ID, COLUMN_DELIVERYCODE_NAME_KEY, getLocalizationManager(), 6);
        deliveryCodeColumn.setEditType(EditType.editwheninserted);
        addColumn(deliveryCodeColumn);

        Column deliveryStatusColumn = new StatusColumn(COLUMN_DELIVERYSTATUS_ID, COLUMN_DELIVERYSTATUS_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MEB_DATASTATUS, 6);
        if (!user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            deliveryStatusColumn.setEditType(EditType.readonly);
            deliveryStatusColumn.setColor(COLOR.LIGHTGREY);
        }
        addColumn(deliveryStatusColumn);

        Column plausiStatusColumn = new PlausistatusColumn(COLUMN_PLAUSISTATUS_ID, COLUMN_PLAUSISTATUS_NAME_KEY, getLocalizationManager(), 8);
        addColumn(plausiStatusColumn);
        // hidden column for plausierror data
        addColumn(new PlausiErrorColumn(COLUMN_PLAUSIERROR_ID, getLocalizationManager()));

        addColumn(new ReadOnlyColumn(COLUMN_CREATIONUSER_ID, COLUMN_CREATIONUSER_NAME_KEY, getLocalizationManager(), 12));
        dateColumn = new DateColumn(COLUMN_CREATIONDATE_ID, COLUMN_CREATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        dateColumn.setWidth(5);
        addColumn(dateColumn);

        addColumn(new ReadOnlyColumn(COLUMN_MODIFICATIONUSER_ID, COLUMN_MODIFICATIONUSER_NAME_KEY, getLocalizationManager(), 12));
        dateColumn = new DateColumn(COLUMN_MODIFICATIONDATE_ID, COLUMN_MODIFICATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        dateColumn.setWidth(5);
        addColumn(dateColumn);

        addColumn(new ReadOnlyColumn(COLUMN_PREVELATIONUSER_ID, COLUMN_PREVELATIONUSER_NAME_KEY, getLocalizationManager(), 12));
        dateColumn = new DateColumn(COLUMN_PREVELATIONDATE_ID, COLUMN_PREVELATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setDefault("");
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        dateColumn.setWidth(5);
        addColumn(dateColumn);

        addColumn(new Column(COLUMN_USERTEXT_ID, COLUMN_USERTEXT_NAME_KEY, getLocalizationManager(), 20));

        // auto loading
        enableAutoLoading();

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, _classTableManager, _learnerTableManager, false, _maintainglobals, true);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, _classTableManager, _learnerTableManager, false, onRowSelectCallback, 250,
                _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, _classTableManager, _learnerTableManager, false, _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, _classTableManager, _learnerTableManager, false, _maintainglobals);
        IJavaScriptFunction refreshButtonsCallback = new RefreshSchoolButtonsCallback(this, COLUMN_DELIVERYSTATUS_ID);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateMaintainCallback(this, _classTableManager, _learnerTableManager);
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this, _classTableManager, _learnerTableManager, false,
                _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, _classTableManager, _learnerTableManager);

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
        registerCallback(new OnLoadErrorCallback(this, ClassTableManager.MANAGER_NAME, LearnerTableManager.MANAGER_NAME));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertRowCallback(this, _classTableManager, _learnerTableManager, Master.MANAGER_MUST_BE_MASTER, _maintainglobals,
                SCHOOL_MUSTBEMASTER_MESSAGE));
        registerCallback(new DeleteRowCallback(this, _classTableManager, _learnerTableManager, _maintainglobals, ALERT_DELETE_SCHOOL_MESSAGE));
        registerCallback(new SaveCallback(this, _classTableManager, _learnerTableManager, false, true, _maintainglobals));
        registerCallback(new FilterCallback(this, _classTableManager, _learnerTableManager, _filterTableManager, _whereTableManager, _maintainglobals, true));
        registerCallback(new SwitchMasterCallback(this, _classTableManager, _learnerTableManager, _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(refreshButtonsCallback);
        registerCallback(new ExportCsvCallback(this, _classTableManager, _learnerTableManager, false, _maintainglobals));
        registerCallback(new ValidateMultipleCallback(this, CodegroupUtility.MEB_APPLICATION_SDL, false));
        registerCallback(new UndoValidateMultipleCallback(this, COLUMN_DELIVERYSTATUS_ID));
        registerCallback(new ReloadSelectedCallback(this));
        registerCallback(new ReloadAllCallback(this));
        registerCallback(onAfterUpdateCallback);
        registerCallback(onGridReconstructedCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(displayNumbersCallback);

        _whereTableManager.create(this);
        _classTableManager.create(this, _learnerTableManager);
        _learnerTableManager.create(this, _classTableManager);
    }

    /**
     * Gets all rows for school Table with maximum buffer rows,
     * starting at start row.
     *
     * @param start  start from row index
     * @param buffer maximum number of rows
     * @return requested school rows from start with buffer number of
     * rows
     */
    public SdlSchoolListResult getRows(int start, int buffer) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _schoolService.getSchools(start, buffer, sortContext, getFilterContext(), getFilterVersion(), getFilterCanton());
    }

    /**
     * Gets all rows depending on the selected schoolIds.
     *
     * @param selectedRowIds list with event ids
     * @return List with all schools with given ???Id
     */
    public SdlSchoolListResult getRows(List<Long> selectedRowIds) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _schoolService.getSchoolsOwnedByClasses(selectedRowIds, sortContext);
    }

    /**
     * Get rows using the parameters from the request
     *
     * @param params Request parameters
     * @return List with persons
     */
    public SdlSchoolListResult getRows(ParameterList params) {
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
    public SdlSchoolListResult getRows(ParameterList params, int start, int buffer) {
        SdlSchoolListResult rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            if (selectedRowIds.size() == 0) {
                rows = new SdlSchoolListResult();
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
    protected List<SdlSchool> getExportRows(ParameterList params) {
        return getRows(params, -1, -1).getSchools();
    }

    @Override
    protected String getExportFileName() {
        return "Schools.csv";
    }

    /**
     * Intializes the school table with all rows.
     *
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        if (params.getRowsLoaded() == 0) {
            Long resultSize;

            SdlSchoolListResult result = getRows(params);
            clearPlausiErrorData(result.getSchools());
            resultSize = result.getMaxNrOfSchools();
            SchoolListTableResultMapper resultMapper = new SchoolListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

            HashMap<String, String> userdata = new HashMap<String, String>();
            userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
            userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
            userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
            return toXMLStream(resultMapper, true, true, userdata);
        } else {
            return load(params);
        }
    }

    /**
     * Loads all rows by given parameters.
     *
     * @param params contains all parameters
     * @return xml with all selected rows depending on the parent table
     * selection who is in the param list
     * @throws DhtmlxException
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        Long resultSize;

        SdlSchoolListResult result = getRows(params);
        clearPlausiErrorData(result.getSchools());
        resultSize = result.getMaxNrOfSchools();
        SchoolListTableResultMapper resultMapper = new SchoolListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
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

        Long resultSize;

        SdlSchoolListResult result = getRows(rowsloaded, bufferlen);
        clearPlausiErrorData(result.getSchools());
        resultSize = result.getMaxNrOfSchools();
        SchoolListTableResultMapper resultMapper = new SchoolListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        return toXMLStream(resultMapper, rowsloaded == 0, false, userdata);
    }

    /**
     * Update school
     *
     * @param params contains all parameters
     * @return XML with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            // merge data record with cache
            SdlSchool school = (SdlSchool) merge(params);
            PlausiErrorColumn.setPlausiErrorData(school.getSchoolId(), params, this);

            SdlSchoolResult result = _schoolService.updateSchool(school, _loadedPlausiErrors.get(school.getSchoolId()),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            SchoolTableResultMapper resultMapper = new SchoolTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            // Merge with an empty record
            SdlSchool school = (SdlSchool) merge(new SdlSchool(), params);

            school.setCanton(getFilterCanton());
            school.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            school.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);

            SdlSchoolResult result = _schoolService.insertSchool(school, params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            SchoolTableResultMapper resultMapper = new SchoolTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        synchronized (UPDATE_LOCK) {
            String selected = params.getRowId();
            SdlSchoolResult result = _schoolService.deleteSchool((SdlSchool) getRowData(selected),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            SchoolTableResultMapper resultMapper = new SchoolTableResultMapper(CommandConstants.DELETE, selected, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
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
            SdlSchoolResult result = _schoolService.getSchoolById(new Long(sid));

            // Maps result
            SchoolTableResultMapper resultMapper = new SchoolTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            SdlSchoolResult result = new SdlSchoolResult();
            result.setState(ResultBase.OK);
            result.setSchool(new SdlSchool());
            SchoolTableResultMapper resultMapper = new SchoolTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    /**
     * Reload unmodified data from DB.
     *
     * @param params
     * @return
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML reload(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        SdlSchoolResult result = _schoolService.getSchoolById(new Long(sid));

        // Maps result
        SchoolTableResultMapper resultMapper = new SchoolTableResultMapper(CommandConstants.REFRESH, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * (Pre-)validate list of schools
     *
     * @param params contains all parameters
     * @return xml with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML validate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedSchoolIds = new ArrayList<Long>();
        for (Long selRowId : selectedRowIds) {
            selectedSchoolIds.add(((SdlSchool) getRowData(selRowId.toString())).getSchoolId());
        }
        SdlSchoolResult result = _schoolService.validateSchools(selectedSchoolIds, false);

        // Maps result
        SchoolTableResultMapper resultMapper = new SchoolTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);
        return toXMLDataStream(resultMapper);
    }

    /**
     * Undo (pre-)validation of schools
     *
     * @param params contains all parameters
     * @return xml with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undoValidate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedSchoolIds = new ArrayList<Long>();
        for (Long selRowId : selectedRowIds) {
            selectedSchoolIds.add(((SdlSchool) getRowData(selRowId.toString())).getSchoolId());
        }
        SdlSchoolResult result = _schoolService.validateSchools(selectedSchoolIds, true);

        // Maps result
        SchoolTableResultMapper resultMapper = new SchoolTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);
        return toXMLDataStream(resultMapper);
    }

    protected void clearPlausiErrorData(List<SdlSchool> schools) {
        for (SdlSchool school : schools) {
            _loadedPlausiErrors.remove(school.getSchoolId());
        }
    }

    public void setPlausiErrorData(Long schoolId, Long plausiErrorId, Boolean isConfirmed) {
        List<PlausiError> plausiErrors = _loadedPlausiErrors.get(schoolId);
        for (PlausiError plausiError : plausiErrors) {
            if (plausiError.getErrorId().equals(plausiErrorId)) {
                plausiError.setIsConfirmed(isConfirmed);
            }
        }
    }

    public IHttpResult plausierrorData(ParameterList params) throws DhtmlxException {
        String sid = params.getRowId();
        PlausiErrorListResult result = _schoolService.getPlausiErrorsForSchool(new Long(sid));
        _loadedPlausiErrors.put(new Long(sid), result.getPlausiErrors());
        String plausiErrorData = "";
        for (int i = 0; i < ResultBase.MAX_NUMBER_ERRORS && i < result.getPlausiErrors().size(); ++i) {
            if (!plausiErrorData.equals("")) {
                plausiErrorData += PlausiErrorColumn.ERROR_SEPARATOR;
            }
            PlausiError plausiError = result.getPlausiErrors().get(i);
            String plausiName;
            String errorMsg;
            if (getLocalizationManager().getLanguage().equals("Fr")) {
                plausiName = plausiError.getPlausiNameFr();
                errorMsg = plausiError.getErrorMsgFr();
            } else if (getLocalizationManager().getLanguage().equals("It")) {
                plausiName = plausiError.getPlausiNameIt();
                errorMsg = plausiError.getErrorMsgIt();
            } else {
                plausiName = plausiError.getPlausiNameDe();
                errorMsg = plausiError.getErrorMsgDe();
            }
            plausiErrorData += PlausiErrorColumn.getErrorUserData(plausiError.getErrorId(), plausiName, errorMsg, plausiError.isConfirmable(),
                    plausiError.isIsConfirmed(), plausiError.getModificationUser(), plausiError.getModificationDate());
        }
        if (result.getPlausiErrors().size() > ResultBase.MAX_NUMBER_ERRORS) {
            plausiErrorData += PlausiErrorColumn.ERROR_SEPARATOR + PlausiErrorColumn.overflowPlausierror(getLocalizationManager());
        }

        return toPlausiErrorResponse(plausiErrorData);
    }

    protected boolean isReadOnly(SdlSchool sdlSchool) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (sdlSchool.getDeliveryStatus() == null) {
            return !user.isInRole(SecurityConstants.ROLE_SDL_DV);
        }

        switch ((int) (long) sdlSchool.getDeliveryStatus()) {
        case (int) CodegroupUtility.MEB_DATASTATUS_VALIDATED:
            return !user.isInRole(SecurityConstants.ROLE_SDL_EV);
        case (int) CodegroupUtility.MEB_DATASTATUS_PREVALIDATED:
            return !user.isInRole(SecurityConstants.ROLE_SDL_DV);
        case (int) CodegroupUtility.MEB_DATASTATUS_DELIVERED:
            return !user.isInRole(SecurityConstants.ROLE_SDL_DL);
        default:
            return true;
        }
    }

    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<String, String>();
        SdlSchool sdlSchool = (SdlSchool) row;
        String readOnlyStr;

        // COLUMN_VERSION, COLUMN_CANTON, COLUMN_IDTYPE, COLUMN_ID, COLUMN_BUR_SCHOOL_LABEL,
        // IS_PUBLIC_SCHOOL, IS_PRIVATE_SUBSIDISED_SCHOOL, IS_PRIVATE_NOT_SUBSIDISED_SCHOOL, IS_SPECIAL_SCHOOL,
        // COLUMN_DELIVERYCODE, COLUMN_DELIVERYSTATUS, COLUMN_PLAUSISTATUS, COLUMN_CREATIONUSER, COLUMN_CREATIONDATE
        // COLUMN_MODIFICATIONUSER, COLUMN_MODIFICATIONDATE, COLUMN_PREVELATIONUSER, COLUMN_PREVELATIONDATE
        // COLUMN_USERTEXT

        if (isReadOnly(sdlSchool)) {
            readOnlyStr = "1111111111111111111";
        } else {
            if (sdlSchool.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                    || sdlSchool.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)) {
                readOnlyStr = "0000111110101111110";
            } else {
                readOnlyStr = "0000111110101111110";
            }
        }

        userData.put("readOnlyCells", readOnlyStr);
        userData.put("plausierror", "empty");
        return userData;
    }

    @Override
    protected String getRowStyleClass(Object row) {
        SdlSchool sdlSchool = (SdlSchool) row;

        if (sdlSchool.getDeliveryStatus() == null) {
            return ROW_NOT_VALID_STYLE;
        } else if (sdlSchool.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_FINALIZED) {
            return ROW_FINALIZED_STYLE;
        } else if (sdlSchool.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_VALIDATED) {
            return ROW_VALIDATED_STYLE;
        } else if (sdlSchool.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_PREVALIDATED) {
            return ROW_PREVALIDATED_STYLE;
        } else if (sdlSchool.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_IMPORTED) {
            return ROW_DATA_IMPORTED_STYLE;
        } else if (sdlSchool.getPlausiStatus() >= CodegroupUtility.MEB_PLAUSISTATUS_VALID) {
            return ROW_VALID_STYLE;
        } else {
            return ROW_NOT_VALID_STYLE;
        }
    }

    @Override
    public String getExtraHtml(String partName) {
        if (partName.equals(ParameterConstants.PARAM_FILTERVERSION)) {
            if (getFilterVersion() == null) {
                return "";
            } else {
                return getFilterVersion().toString();
            }
        } else if (partName.equals(ParameterConstants.PARAM_FILTERCANTON)) {
            StringBuilder cantons = new StringBuilder();
            for (Long canton : _cantonService.getFilterCantonsForActUser()) {
                final String selected;
                if (canton.equals(getFilterCanton())) {
                    selected = " selected";
                } else {
                    selected = "";
                }

                if (canton > 0L) {
                    cantons.append("<option value=\"")
                            .append(canton).append("\"")
                            .append(selected)
                            .append(">")
                            .append(getLocalizationManager().getCodeGroupValueById(CodegroupUtility.CANTON, canton))
                            .append(" (")
                            .append(canton)
                            .append(")</option>");
                }
            }
            return cantons.toString();
        }

        return "";
    }

    @Override
    public JSNumber getBuffSize() {
        return BUFFSIZE;
    }
}
