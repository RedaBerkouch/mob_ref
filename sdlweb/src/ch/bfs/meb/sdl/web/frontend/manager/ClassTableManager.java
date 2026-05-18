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

import ch.bfs.meb.util.MebUtils;
import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.exception.SessionTimeoutException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.dhtmlx.callback.RefreshClassButtonsCallback;
import ch.bfs.meb.sdl.web.frontend.resultmapper.ClassListTableResultMapper;
import ch.bfs.meb.sdl.web.frontend.resultmapper.ClassTableResultMapper;
import ch.bfs.meb.sdl.web.service.ICantonService;
import ch.bfs.meb.sdl.web.service.IClassService;
import ch.bfs.meb.sdl.web.service.ISchoolService;
import ch.bfs.meb.sdl.web.ws.sdlclass.*;
import ch.bfs.meb.sdl.web.ws.sdlschool.SdlSchoolResult;
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
 * This Class represents a ClassTableManager for the maintain tab and acts
 * as a controller for the Class Table.
 */
@Scope("session")
@Component("classTableManager")
public class ClassTableManager extends FilteredTableManagerBase implements PlausiErrorColumn.IPlausiErrorDataUpdate {
    public static final String COLUMN_CLASSID_ID = "classId";
    public static final String COLUMN_CLASSID_NAME_KEY = "classTable.column.classId.name";
    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "classTable.column.canton.name";
    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_VERSION_NAME_KEY = "classTable.column.version.name";
    public static final String COLUMN_ID_ID = "id";
    public static final String COLUMN_ID_NAME_KEY = "classTable.column.id.name";
    public static final String COLUMN_SCHOOLTYPE_ID = "schoolType";
    public static final String COLUMN_SCHOOLTYPE_NAME_KEY = "classTable.column.schoolType.name";
    public static final String COLUMN_DELIVERYSTATUS_ID = "deliveryStatus";
    public static final String COLUMN_DELIVERYSTATUS_NAME_KEY = "classTable.column.deliveryStatus.name";
    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    private String _sortCol = COLUMN_PLAUSISTATUS_ID;
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "classTable.column.plausiStatus.name";
    public static final String COLUMN_PLAUSIERROR_ID = "plausierrors";
    public static final String COLUMN_CREATIONUSER_ID = "creationUser";
    public static final String COLUMN_CREATIONUSER_NAME_KEY = "classTable.column.creationUser.name";
    public static final String COLUMN_CREATIONDATE_ID = "creationDate";
    public static final String COLUMN_CREATIONDATE_NAME_KEY = "classTable.column.creationDate.name";
    public static final String COLUMN_MODIFICATIONUSER_ID = "modificationUser";
    public static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "classTable.column.modificationUser.name";
    public static final String COLUMN_MODIFICATIONDATE_ID = "modificationDate";
    public static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "classTable.column.modificationDate.name";
    public static final String COLUMN_PREVELATIONUSER_ID = "prevalidationUser";
    public static final String COLUMN_PREVELATIONUSER_NAME_KEY = "classTable.column.prevalidationUser.name";
    public static final String COLUMN_PREVELATIONDATE_ID = "prevalidationDate";
    public static final String COLUMN_PREVELATIONDATE_NAME_KEY = "classTable.column.prevalidationDate.name";
    public static final String COLUMN_USERTEXT_ID = "userText";
    public static final String COLUMN_USERTEXT_NAME_KEY = "classTable.column.userText.name";
    public static final String MANAGER_NAME = "class";
    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;
    protected static final String ALERT_DELETE_CLASS_MESSAGE = "alert.delete.class.message";
    protected static final String INSERT_CLASS_UNIQUE_MESSAGE = "insert.class.unique.message";
    protected static final String CLASS_NOMASTER_MESSAGE = "class.nomaster.message";
    private static final JSNumber BUFFSIZE = new JSNumber(500);
    private static final int BUFFERLEN = 500;
    private final String UPDATE_LOCK = "SdlClassUpdateLock";
    protected final HashMap<Long, List<PlausiError>> _loadedPlausiErrors = new HashMap<>();
    @Autowired
    private ICantonService _cantonService;
    @Autowired
    private ISchoolService _schoolService;
    @Autowired
    private IClassService _classService;
    @Autowired
    private IWebLocalizationManager _localizationManager;
    @Autowired
    private IGlobalJavaScript _maintainglobals;
    @Autowired
    private ClassFilterTableManager _filterTableManager;
    @Autowired
    private ClassWhereTableManager _whereTableManager;
    private SchoolTableManager _schoolTableManager;
    private LearnerTableManager _learnerTableManager;
    private boolean _ascSort = true;
    private ComboCodeGroupColumn _schoolTypeColumn;

    public ClassTableManager() {
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
     * Initializes a new ClassTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    @Override
    public void create() {}

    public void create(SchoolTableManager schoolTableManager, LearnerTableManager learnerTableManager) throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        setFilterVersion(user.getLastFilterVersion());
        if (user.getLastFilterCanton() == null || user.getLastFilterCanton() <= 0) {
            List<Long> cantons = _cantonService.getFilterCantonsForActUser();
            setFilterCanton(cantons.get(0) > 0L ? cantons.get(0) : (cantons.size() > 1 ? cantons.get(1) : null));
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        _schoolTableManager = schoolTableManager;
        _learnerTableManager = learnerTableManager;

        DateColumn dateColumn;

        addColumn(new IdentityColumn(COLUMN_CLASSID_ID, COLUMN_CLASSID_NAME_KEY, getLocalizationManager()));

        addColumn(new ReadOnlyColumn(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 3));

        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 3);
        cantonColumn.setEditType(EditType.readonly);
        cantonColumn.setColor(COLOR.LIGHTGREY);
        addColumn(cantonColumn);

        addColumn(new Column(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager(), 7));

        _schoolTypeColumn = new ComboCodeGroupColumn(COLUMN_SCHOOLTYPE_ID, COLUMN_SCHOOLTYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.SCHOOL_DEP_TYPE, getFilterCanton(), false, 28);
        addColumn(_schoolTypeColumn);

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
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, _schoolTableManager, _learnerTableManager, true, _maintainglobals, true);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, _schoolTableManager, _learnerTableManager, true, onRowSelectCallback, 250,
                _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, _schoolTableManager, _learnerTableManager, true, _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, _schoolTableManager, _learnerTableManager, true, _maintainglobals);
        IJavaScriptFunction refreshButtonsCallback = new RefreshClassButtonsCallback(this, _schoolTableManager, COLUMN_DELIVERYSTATUS_ID,
                SchoolTableManager.COLUMN_DELIVERYSTATUS_ID);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateMaintainCallback(this, _learnerTableManager, null, _schoolTableManager, null);
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this, _schoolTableManager, _learnerTableManager, true,
                _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, _schoolTableManager, _learnerTableManager);

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
        registerCallback(new OnLoadErrorCallback(this, SchoolTableManager.MANAGER_NAME, LearnerTableManager.MANAGER_NAME));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(
                new InsertRowCallback(this, _schoolTableManager, _learnerTableManager, Master.OTHER_MUST_BE_MASTER, _maintainglobals, CLASS_NOMASTER_MESSAGE));
        registerCallback(new DeleteRowCallback(this, _schoolTableManager, _learnerTableManager, _maintainglobals, ALERT_DELETE_CLASS_MESSAGE));
        registerCallback(new SaveCallback(this, _schoolTableManager, _learnerTableManager, true, true, _maintainglobals));
        registerCallback(new FilterCallback(this, _schoolTableManager, _learnerTableManager, _filterTableManager, _whereTableManager, _maintainglobals, true));
        registerCallback(new SwitchMasterCallback(this, _schoolTableManager, _learnerTableManager, _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(refreshButtonsCallback);
        registerCallback(new ExportCsvCallback(this, _schoolTableManager, _learnerTableManager, true, _maintainglobals));
        registerCallback(new ValidateMultipleCallback(this, CodegroupUtility.MEB_APPLICATION_SDL, true)); // prevalidateOnly
        registerCallback(new UndoValidateMultipleCallback(this));
        registerCallback(new ReloadSelectedCallback(this));
        registerCallback(new ReloadAllCallback(this));
        registerCallback(onAfterUpdateCallback);
        registerCallback(onGridReconstructedCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(displayNumbersCallback);

        _whereTableManager.create(this);
    }

    /**
     * Gets all rows for class Table with maximum buffer rows,
     * starting at start row.
     *
     * @param start  start from row index
     * @param buffer maximum number of rows
     * @return requested class rows from start with buffer number of
     * rows
     */
    public SdlClassListResult getRows(int start, int buffer) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        SdlClassListResult classes;

        if (getFilterContext() == null || isInInit()) {
            setInInit(false);
            classes = new SdlClassListResult();
            classes.setState(ResultBase.OK);
        } else {
            classes = _classService.getClasses(start, buffer, sortContext, getFilterContext(), getFilterVersion(), getFilterCanton());
        }

        return classes;
    }

    /**
     * Gets all rows depending on the selected schoolIds or learnerIds
     *
     * @param selectedRowIds list with school ids or learner ids
     * @return List with all classes with given schoolIds or learnerIds
     */
    public SdlClassListResult getRows(List<Long> selectedRowIds, String selectedMaster) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        SdlClassListResult classes;
        if (selectedMaster.equals(SchoolTableManager.MANAGER_NAME)) {
            classes = _classService.getClassesOwnedBySchools(selectedRowIds, sortContext);
        } else {
            classes = _classService.getClassesOwnedByLearners(selectedRowIds, sortContext);
        }

        return classes;
    }

    /**
     * Get rows using the parameters from the request
     *
     * @param params Request parameters
     * @return List with persons
     */
    public SdlClassListResult getRows(ParameterList params) {
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
    public SdlClassListResult getRows(ParameterList params, int start, int buffer) {
        SdlClassListResult rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            if (_schoolTableManager == null) {
                throw new SessionTimeoutException();
            }

            Long canton;
            if (params.getSelectedMaster().equals(SchoolTableManager.MANAGER_NAME)) {
                canton = _schoolTableManager.getCanton();
            } else {
                canton = _learnerTableManager.getCanton();
            }
            if (!canton.equals(getFilterCanton())) {
                setFilterCanton(canton);
                _schoolTypeColumn.setCanton(getFilterCanton());
            }

            if (selectedRowIds.size() == 0) {
                rows = new SdlClassListResult();
                rows.setState(ResultBase.OK);
            } else {
                rows = getRows(selectedRowIds, params.getSelectedMaster());
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
    protected List<SdlClass> getExportRows(ParameterList params) {
        return getRows(params, -1, -1).getClasses();
    }

    @Override
    protected String getExportFileName() {
        return "Classes.csv";
    }

    /**
     * Intializes the class table with all rows.
     *
     * @param params contains all parameters
     * @return xml with all rows
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        Long resultSize;

        setInInit(true);

        SdlClassListResult result = getRows(params);
        clearPlausiErrorData(result.getClasses());
        resultSize = result.getMaxNrOfClasses();
        ClassListTableResultMapper resultMapper = new ClassListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
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
        Long oldCanton = getFilterCanton();

        SdlClassListResult result = getRows(params);
        clearPlausiErrorData(result.getClasses());
        resultSize = result.getMaxNrOfClasses();
        ClassListTableResultMapper resultMapper = new ClassListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        return toXMLStream(resultMapper, params.getRowsLoaded() == 0, userdata, !MebUtils.areEqual(oldCanton, getFilterCanton()));
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

        Long oldCanton = getFilterCanton();

        String filterError = extractFilterParams(params);
        if (filterError != null) {
            return toErrorResponse(filterError);
        }

        _schoolTypeColumn.setCanton(getFilterCanton());

        Long resultSize;

        SdlClassListResult result = getRows(rowsloaded, bufferlen);
        clearPlausiErrorData(result.getClasses());
        resultSize = result.getMaxNrOfClasses();
        ClassListTableResultMapper resultMapper = new ClassListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        return toXMLStream(resultMapper, rowsloaded == 0, userdata, !MebUtils.areEqual(oldCanton, getFilterCanton()));
    }

    /**
     * Update class
     *
     * @param params contains all parameters
     * @return XML with updated row
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            // merge data record with cache
            SdlClass sdlClass = (SdlClass) merge(params);
            PlausiErrorColumn.setPlausiErrorData(sdlClass.getClassId(), params, this);

            SdlClassResult result = _classService.updateClass(sdlClass, _loadedPlausiErrors.get(sdlClass.getClassId()),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            ClassTableResultMapper resultMapper = new ClassTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);
            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            List<Long> selIds = params.getSelectedRows();
            if (selIds.size() != 1 || !params.getSelectedMaster().equals(SchoolTableManager.MANAGER_NAME)) {
                return toXMLDataErrorStream(getLocalizationManager().getMessage(INSERT_CLASS_UNIQUE_MESSAGE), sid);
            }
            SdlSchoolResult schoolResult = _schoolService.getSchoolById(selIds.get(0));
            if (schoolResult.getSchool() == null) {
                return toXMLDataErrorStream(getLocalizationManager().getMessage(schoolResult.getMessage()), sid);
            }

            // Merge with an empty record
            SdlClass sdlClass = (SdlClass) merge(new SdlClass(), params);

            sdlClass.setSchoolId(schoolResult.getSchool().getSchoolId());
            sdlClass.setCanton(schoolResult.getSchool().getCanton());
            sdlClass.setVersion(schoolResult.getSchool().getVersion());
            sdlClass.setDeliveryCode(schoolResult.getSchool().getDeliveryCode());
            sdlClass.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            sdlClass.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);

            SdlClassResult result = _classService.insertClass(sdlClass, params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            ClassTableResultMapper resultMapper = new ClassTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);
            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        synchronized (UPDATE_LOCK) {
            String selected = params.getRowId();
            SdlClassResult result = _classService.deleteClass((SdlClass) getRowData(selected),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            ClassTableResultMapper resultMapper = new ClassTableResultMapper(CommandConstants.DELETE, selected, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);
            return toXMLDataStream(resultMapper);
        }
    }

    /**
     * Gets all unmodified rows from DB. This method sets the initial values.
     */
    public DhtmlxTableDataXML undo(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        if (!params.getEditorStatus().equals(EDIT.INSERT)) {
            SdlClassResult result = _classService.getClassById(new Long(sid));

            // Maps result
            ClassTableResultMapper resultMapper = new ClassTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            SdlClassResult result = new SdlClassResult();
            result.setState(ResultBase.OK);
            result.setSdlClass(new SdlClass());
            ClassTableResultMapper resultMapper = new ClassTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    /**
     * Reload unmodified data from DB.
     */
    public DhtmlxTableDataXML reload(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        SdlClassResult result = _classService.getClassById(new Long(sid));

        // Maps result
        ClassTableResultMapper resultMapper = new ClassTableResultMapper(CommandConstants.REFRESH, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Prevalidate list of classes
     *
     * @param params contains all parameters
     * @return xml with updated row
     */
    public DhtmlxTableDataXML validate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedClassIds = new ArrayList<>();
        for (Long selRowId : selectedRowIds) {
            selectedClassIds.add(((SdlClass) getRowData(selRowId.toString())).getClassId());
        }
        SdlClassResult result = _classService.validateClasses(selectedClassIds, false);

        // Maps result
        ClassTableResultMapper resultMapper = new ClassTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);
        return toXMLDataStream(resultMapper);
    }

    /**
     * Undo prevalidation of classes
     *
     * @param params contains all parameters
     * @return xml with updated row
     */
    public DhtmlxTableDataXML undoValidate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedClassIds = new ArrayList<>();
        for (Long selRowId : selectedRowIds) {
            selectedClassIds.add(((SdlClass) getRowData(selRowId.toString())).getClassId());
        }
        SdlClassResult result = _classService.validateClasses(selectedClassIds, true);

        // Maps result
        ClassTableResultMapper resultMapper = new ClassTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);
        return toXMLDataStream(resultMapper);
    }

    protected void clearPlausiErrorData(List<SdlClass> classes) {
        for (SdlClass sdlClass : classes) {
            _loadedPlausiErrors.remove(sdlClass.getClassId());
        }
    }

    public void setPlausiErrorData(Long classId, Long plausiErrorId, Boolean isConfirmed) {
        List<PlausiError> plausiErrors = _loadedPlausiErrors.get(classId);
        for (PlausiError plausiError : plausiErrors) {
            if (plausiError.getErrorId().equals(plausiErrorId)) {
                plausiError.setIsConfirmed(isConfirmed);
            }
        }
    }

    public IHttpResult plausierrorData(ParameterList params) {
        String sid = params.getRowId();
        PlausiErrorListResult result = _classService.getPlausiErrorsForClass(new Long(sid));
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

    protected boolean isReadOnly(SdlClass sdlClass) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (sdlClass.getDeliveryStatus() == null) {
            return !user.isInRole(SecurityConstants.ROLE_SDL_DV);
        }

        switch ((int) (long) sdlClass.getDeliveryStatus()) {
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
        HashMap<String, String> userData = new HashMap<>();
        SdlClass sdlClass = (SdlClass) row;
        String readOnlyStr;

        if (isReadOnly(sdlClass)) {
            readOnlyStr = "1111111111111";
        } else {
            if (sdlClass.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                readOnlyStr = "0000001111110";
            } else {
                readOnlyStr = "0000101111110";
            }
        }

        userData.put("readOnlyCells", readOnlyStr);
        userData.put("plausierror", "empty");
        return userData;
    }

    @Override
    protected String getRowStyleClass(Object row) {
        SdlClass sdlClass = (SdlClass) row;

        if (sdlClass.getDeliveryStatus() == null) {
            return ROW_NOT_VALID_STYLE;
        } else if (sdlClass.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_FINALIZED) {
            return ROW_FINALIZED_STYLE;
        } else if (sdlClass.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_VALIDATED) {
            return ROW_VALIDATED_STYLE;
        } else if (sdlClass.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_PREVALIDATED) {
            return ROW_PREVALIDATED_STYLE;
        } else if (sdlClass.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_IMPORTED) {
            return ROW_DATA_IMPORTED_STYLE;
        } else if (sdlClass.getPlausiStatus() >= CodegroupUtility.MEB_PLAUSISTATUS_VALID) {
            return ROW_VALID_STYLE;
        } else {
            return ROW_NOT_VALID_STYLE;
        }
    }

    @Override
    public JSNumber getBuffSize() {
        return BUFFSIZE;
    }
}
