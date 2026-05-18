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
import java.util.Map;

import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.ssp.web.dhtmlx.callback.RefreshPersonButtonsCallback;
import ch.bfs.meb.ssp.web.frontend.resultmapper.PersonListTableResultMapper;
import ch.bfs.meb.ssp.web.frontend.resultmapper.PersonTableResultMapper;
import ch.bfs.meb.ssp.web.service.IPersonService;
import ch.bfs.meb.ssp.web.ws.sspperson.*;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
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

/**
 * This Class represents a PersonTableManager for the maintain tab and acts
 * as a controller for the Person Table.
 */
@Scope("session")
@Component("personTableManager")
public class PersonTableManager extends FilteredTableManagerBase implements PlausiErrorColumn.IPlausiErrorDataUpdate {
    private final String UPDATE_LOCK = "SspPersonUpdateLock";

    private static final int BUFFERLEN = 100;
    private static final JSNumber BUFFSIZE = new JSNumber(100);

    public static final String COLUMN_PERSONID_ID = "personId";
    public static final String COLUMN_PERSONID_NAME_KEY = "personTable.column.personId.name";
    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_VERSION_NAME_KEY = "personTable.column.version.name";
    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "personTable.column.canton.name";
    public static final String COLUMN_DELIVERYCODE_ID = "deliveryCode";
    public static final String COLUMN_DELIVERYCODE_NAME_KEY = "personTable.column.deliveryCode.name";
    public static final String COLUMN_IDTYPE_ID = "idType";
    public static final String COLUMN_IDTYPE_NAME_KEY = "personTable.column.idtype.name";
    public static final String COLUMN_ID_ID = "id";
    public static final String COLUMN_ID_NAME_KEY = "personTable.column.personId.name";
    public static final String COLUMN_SEX_ID = "sex";
    public static final String COLUMN_SEX_NAME_KEY = "personTable.column.sex.name";
    public static final String COLUMN_BIRTHDATE_ID = "birthdate";
    public static final String COLUMN_BIRTHDATE_NAME_KEY = "personTable.column.birthdate.name";
    public static final String COLUMN_NATIONALITY_ID = "nationality";
    public static final String COLUMN_NATIONALITY_NAME_KEY = "personTable.column.nationality.name";
    public static final String COLUMN_YEARSOFSERVICE_ID = "yearsOfService";
    public static final String COLUMN_YEARSOFSERVICE_NAME_KEY = "personTable.column.yearsOfService.name";
    public static final String COLUMN_DELIVERYSTATUS_ID = "deliveryStatus";
    public static final String COLUMN_DELIVERYSTATUS_NAME_KEY = "personTable.column.deliveryStatus.name";
    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "personTable.column.plausiStatus.name";
    public static final String COLUMN_PLAUSIERROR_ID = "plausierrors";

    public static final String COLUMN_CREATIONUSER_ID = "creationUser";
    public static final String COLUMN_CREATIONUSER_NAME_KEY = "personTable.column.creationUser.name";
    public static final String COLUMN_CREATIONDATE_ID = "creationDate";
    public static final String COLUMN_CREATIONDATE_NAME_KEY = "personTable.column.creationDate.name";

    public static final String COLUMN_MODIFICATIONUSER_ID = "modificationUser";
    public static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "personTable.column.modificationUser.name";
    public static final String COLUMN_MODIFICATIONDATE_ID = "modificationDate";
    public static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "personTable.column.modificationDate.name";

    public static final String COLUMN_PREVELATIONUSER_ID = "prevalidationUser";
    public static final String COLUMN_PREVELATIONUSER_NAME_KEY = "personTable.column.prevalidationUser.name";
    public static final String COLUMN_PREVELATIONDATE_ID = "prevalidationDate";
    public static final String COLUMN_PREVELATIONDATE_NAME_KEY = "personTable.column.prevalidationDate.name";

    public static final String COLUMN_USERTEXT_ID = "userText";
    public static final String COLUMN_USERTEXT_NAME_KEY = "personTable.column.userText.name";

    public static final String COLUMN_ORIGDELIVERYDATA_ID = "origDeliveryData";
    public static final String COLUMN_ORIGDELIVERYDATA_NAME_KEY = "personTable.column.origDeliveryData.name";

    protected static final String ALERT_DELETE_PERSON_MESSAGE = "alert.delete.person.message";
    protected static final String PERSON_MUSTBEMASTER_MESSAGE = "person.mustbemaster.message";

    public static final String MANAGER_NAME = "person";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IPersonService _personService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    private PersonFilterTableManager _filterTableManager;

    @Autowired
    private PersonWhereTableManager _whereTableManager;

    private String _sortCol = COLUMN_PLAUSISTATUS_ID;

    private boolean _ascSort = true;

    protected final HashMap<Long, List<PlausiError>> _loadedPlausiErrors = new HashMap<Long, List<PlausiError>>();

    public PersonTableManager() throws DhtmlxException {
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

    private IDhtmlxControl getActivityTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return ActivityTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return ActivityTableManager.MANAGER_NAME;
            }
        };
    }

    public Long getCanton() {
        return getFilterCanton();
    }

    /**
     * Initializes a new PersonTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    @Override
    public void create() throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        setFilterVersion(user.getLastFilterVersion());
        if (user.getLastFilterCanton() == null || user.getLastFilterCanton() <= 0) {
            List<Long> cantons = getCantonsForActUser();
            setFilterCanton(cantons.get(0) > 0L ? cantons.get(0) : cantons.get(1));
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        // This table is initialy master
        setMaster(true);

        DateColumn dateColumn;

        addColumn(new IdentityColumn(COLUMN_PERSONID_ID, COLUMN_PERSONID_NAME_KEY, getLocalizationManager()));

        Column versionColumn = new Column(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 6);
        versionColumn.setEditType(EditType.editwheninserted);
        addColumn(versionColumn);

        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 10);
        cantonColumn.setEditType(EditType.editwheninserted);
        addColumn(cantonColumn);

        Column deliveryCodeColumn = new Column(COLUMN_DELIVERYCODE_ID, COLUMN_DELIVERYCODE_NAME_KEY, getLocalizationManager(), 8);
        deliveryCodeColumn.setEditType(EditType.editwheninserted);
        addColumn(deliveryCodeColumn);

        addColumn(new Column(COLUMN_IDTYPE_ID, COLUMN_IDTYPE_NAME_KEY, getLocalizationManager(), 8));

        addColumn(new Column(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager(), 8));

        ComboCodeGroupColumn sexColumn = new ComboCodeGroupColumn(COLUMN_SEX_ID, COLUMN_SEX_NAME_KEY, getLocalizationManager(), CodegroupUtility.SEX, 8);
        addColumn(sexColumn);

        dateColumn = new DateColumn(COLUMN_BIRTHDATE_ID, COLUMN_BIRTHDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setDefault("");
        addColumn(dateColumn);

        ComboCodeGroupColumn nationalityColumn = new ComboCodeGroupColumn(COLUMN_NATIONALITY_ID, COLUMN_NATIONALITY_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.NATIONALITY, false, 8);
        addColumn(nationalityColumn);

        addColumn(new Column(COLUMN_YEARSOFSERVICE_ID, COLUMN_YEARSOFSERVICE_NAME_KEY, getLocalizationManager(), 8));

        Column deliveryStatusColumn = new StatusColumn(COLUMN_DELIVERYSTATUS_ID, COLUMN_DELIVERYSTATUS_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MEB_DATASTATUS, 8);
        if (!user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
            deliveryStatusColumn.setEditType(EditType.readonly);
            deliveryStatusColumn.setColor(COLOR.LIGHTGREY);
        }
        addColumn(deliveryStatusColumn);

        Column plausiStatusColumn = new PlausistatusColumn(COLUMN_PLAUSISTATUS_ID, COLUMN_PLAUSISTATUS_NAME_KEY, getLocalizationManager(), 8);
        addColumn(plausiStatusColumn);
        // hidden column for plausierror data
        addColumn(new PlausiErrorColumn(COLUMN_PLAUSIERROR_ID, getLocalizationManager()));

        addColumn(new ReadOnlyColumn(COLUMN_CREATIONUSER_ID, COLUMN_CREATIONUSER_NAME_KEY, getLocalizationManager(), 18));
        dateColumn = new DateColumn(COLUMN_CREATIONDATE_ID, COLUMN_CREATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        addColumn(dateColumn);

        addColumn(new ReadOnlyColumn(COLUMN_MODIFICATIONUSER_ID, COLUMN_MODIFICATIONUSER_NAME_KEY, getLocalizationManager(), 18));
        dateColumn = new DateColumn(COLUMN_MODIFICATIONDATE_ID, COLUMN_MODIFICATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        addColumn(dateColumn);

        addColumn(new ReadOnlyColumn(COLUMN_PREVELATIONUSER_ID, COLUMN_PREVELATIONUSER_NAME_KEY, getLocalizationManager(), 20));
        dateColumn = new DateColumn(COLUMN_PREVELATIONDATE_ID, COLUMN_PREVELATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setDefault("");
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        addColumn(dateColumn);

        addColumn(new Column(COLUMN_USERTEXT_ID, COLUMN_USERTEXT_NAME_KEY, getLocalizationManager(), 20));

        addColumn(new OrigDeliveryDataColumn(COLUMN_ORIGDELIVERYDATA_ID, COLUMN_ORIGDELIVERYDATA_NAME_KEY, getLocalizationManager(), 11));

        // auto loading
        enableAutoLoading();

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, getActivityTable(), _maintainglobals);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, getActivityTable(), onRowSelectCallback, 250, _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, getActivityTable(), _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, getActivityTable(), _maintainglobals);
        IJavaScriptFunction refreshButtonsCallback = new RefreshPersonButtonsCallback(this, COLUMN_DELIVERYSTATUS_ID);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateMaintainCallback(this, getActivityTable());
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this, getActivityTable(), _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getActivityTable());

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
        registerCallback(new OnLoadErrorCallback(this, ActivityTableManager.MANAGER_NAME, null));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertRowCallback(this, getActivityTable(), Master.MANAGER_MUST_BE_MASTER, _maintainglobals, PERSON_MUSTBEMASTER_MESSAGE));
        registerCallback(new DeleteRowCallback(this, getActivityTable(), _maintainglobals, ALERT_DELETE_PERSON_MESSAGE));
        registerCallback(new SaveCallback(this, getActivityTable(), true, _maintainglobals));
        registerCallback(new FilterCallback(this, getActivityTable(), null, _filterTableManager, _whereTableManager, _maintainglobals, true));
        registerCallback(new SwitchMasterCallback(this, getActivityTable(), _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(refreshButtonsCallback);
        registerCallback(new ExportCsvCallback(this, getActivityTable(), null, false, _maintainglobals));
        registerCallback(new ValidateMultipleCallback(this, CodegroupUtility.MEB_APPLICATION_SSP, false));
        registerCallback(new UndoValidateMultipleCallback(this, COLUMN_DELIVERYSTATUS_ID));
        registerCallback(new ReloadSelectedCallback(this));
        registerCallback(new ReloadAllCallback(this));
        registerCallback(onAfterUpdateCallback);
        registerCallback(onGridReconstructedCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(displayNumbersCallback);

        _whereTableManager.create(this);
    }

    /**
     * Gets all rows for person Table with maximum buffer rows,
     * starting at start row.
     * 
     * @param start
     *            start from row index
     * @param buffer
     *            maximum number of rows
     * @return requested person rows from start with buffer number of
     *         rows
     */
    public SspPersonListResult getRows(int start, int buffer) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _personService.getPersons(start, buffer, sortContext, getFilterContext(), getFilterVersion(), getFilterCanton());
    }

    /**
     * Gets all rows depending on the selected personIds.
     * 
     * @param selectedRowIds
     *            list with event ids
     * @return List with all persons with given ???Id
     */
    public SspPersonListResult getRows(List<Long> selectedRowIds) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _personService.getPersonsOwnedByActivities(selectedRowIds, sortContext);
    }

    /**
     * Get rows using the parameters from the request
     * 
     * @param params
     *            Request parameters
     * @return List with persons
     */
    public SspPersonListResult getRows(ParameterList params) {
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
    public SspPersonListResult getRows(ParameterList params, int start, int buffer) {
        SspPersonListResult rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            if (selectedRowIds.size() == 0) {
                rows = new SspPersonListResult();
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
    protected List<SspPerson> getExportRows(ParameterList params) {
        return getRows(params, -1, -1).getPersons();
    }

    @Override
    protected String getExportFileName() {
        return "Persons.csv";
    }

    /**
     * Intializes the person table with all rows.
     * 
     * @param params
     *            contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        if (params.getRowsLoaded() == 0) {
            Long resultSize;

            SspPersonListResult result = getRows(params);
            clearPlausiErrorData(result.getPersons());
            resultSize = result.getMaxNrOfPersons();
            PersonListTableResultMapper resultMapper = new PersonListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

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
     * @param params
     *            contains all parameters
     * @return xml with all selected rows depending on the parent table
     *         selection who is in the param list
     * @throws DhtmlxException
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        Long resultSize;

        SspPersonListResult result = getRows(params);
        clearPlausiErrorData(result.getPersons());
        resultSize = result.getMaxNrOfPersons();
        PersonListTableResultMapper resultMapper = new PersonListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

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

        SspPersonListResult result = getRows(rowsloaded, bufferlen);
        clearPlausiErrorData(result.getPersons());
        resultSize = result.getMaxNrOfPersons();
        PersonListTableResultMapper resultMapper = new PersonListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        return toXMLStream(resultMapper, rowsloaded == 0, false, userdata);
    }

    /**
     * Update person
     * 
     * @param params
     *            contains all parameters
     * @return XML with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            // merge data record with cache
            SspPerson person = (SspPerson) merge(params);
            PlausiErrorColumn.setPlausiErrorData(person.getPersonId(), params, this);

            SspPersonResult result = _personService.updatePerson(person, _loadedPlausiErrors.get(person.getPersonId()),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            // Merge with an empty record
            SspPerson person = (SspPerson) merge(new SspPerson(), params);

            person.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            person.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);

            SspPersonResult result = _personService.insertPerson(person, params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        synchronized (UPDATE_LOCK) {
            String selected = params.getRowId();
            SspPersonResult result = _personService.deletePerson((SspPerson) getRowData(selected),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.DELETE, selected, result, getLocalizationManager());

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
            SspPersonResult result = _personService.getPersonById(new Long(sid));

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            SspPersonResult result = new SspPersonResult();
            result.setState(ResultBase.OK);
            result.setPerson(new SspPerson());
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

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

        SspPersonResult result = _personService.getPersonById(new Long(sid));

        // Maps result
        PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.REFRESH, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * (Pre-)validate list of persons
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML validate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedPersonIds = new ArrayList<Long>();
        for (Long selRowId : selectedRowIds) {
            selectedPersonIds.add(((SspPerson) getRowData(selRowId.toString())).getPersonId());
        }
        SspPersonResult result = _personService.validatePersons(selectedPersonIds, false);

        // Maps result
        PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);

        return toXMLDataStream(resultMapper);
    }

    /**
     * Undo (pre-)validation of persons
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undoValidate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedPersonIds = new ArrayList<Long>();
        for (Long selRowId : selectedRowIds) {
            selectedPersonIds.add(((SspPerson) getRowData(selRowId.toString())).getPersonId());
        }
        SspPersonResult result = _personService.validatePersons(selectedPersonIds, true);

        // Maps result
        PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);

        return toXMLDataStream(resultMapper);
    }

    protected void clearPlausiErrorData(List<SspPerson> persons) {
        for (SspPerson person : persons) {
            _loadedPlausiErrors.remove(person.getPersonId());
        }
    }

    public void setPlausiErrorData(Long personId, Long plausiErrorId, Boolean isConfirmed) {
        List<PlausiError> plausiErrors = _loadedPlausiErrors.get(personId);
        for (PlausiError plausiError : plausiErrors) {
            if (plausiError.getErrorId().equals(plausiErrorId)) {
                plausiError.setIsConfirmed(isConfirmed);
            }
        }
    }

    public IHttpResult plausierrorData(ParameterList params) throws DhtmlxException {
        String sid = params.getRowId();
        PlausiErrorListResult result = _personService.getPlausiErrorsForPerson(new Long(sid));
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

    protected boolean isReadOnly(SspPerson sspPerson) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (sspPerson.getDeliveryStatus() == null) {
            return !user.isInRole(SecurityConstants.ROLE_SSP_DV);
        }

        switch ((int) (long) sspPerson.getDeliveryStatus()) {
        case (int) CodegroupUtility.MEB_DATASTATUS_VALIDATED:
            return !user.isInRole(SecurityConstants.ROLE_SSP_EV);
        case (int) CodegroupUtility.MEB_DATASTATUS_PREVALIDATED:
            return !user.isInRole(SecurityConstants.ROLE_SSP_DV);
        case (int) CodegroupUtility.MEB_DATASTATUS_DELIVERED:
            return !user.isInRole(SecurityConstants.ROLE_SSP_DL);
        default:
            return true;
        }
    }

    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<String, String>();
        SspPerson sspPerson = (SspPerson) row;
        String readOnlyStr;

        // COLUMN_VERSION, COLUMN_CANTON, COLUMN_DELIVERYCODE, COLUMN_IDTYPE, COLUMN_ID, COLUMN_SEX,
        // COLUMN_BIRTHDATE, COLUMN_NATIONALITY, COLUMN_YEARSOFSERVICE, 
        // COLUMN_DELIVERYSTATUS, COLUMN_PLAUSISTATUS, COLUMN_CREATIONUSER, COLUMN_CREATIONDATE
        // COLUMN_MODIFICATIONUSER, COLUMN_MODIFICATIONDATE, COLUMN_PREVELATIONUSER, COLUMN_PREVELATIONDATE
        // COLUMN_USERTEXT, COLUMN_ORIGDELIVERYDATA

        if (isReadOnly(sspPerson)) {
            readOnlyStr = "1111111111111111111";
        } else {
            if (sspPerson.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                    || sspPerson.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_VALIDATED)) {
                readOnlyStr = "0000000000011111101";
            } else {
                readOnlyStr = "0000000001011111101";
            }
        }

        userData.put("readOnlyCells", readOnlyStr);
        userData.put("plausierror", "empty");
        return userData;
    }

    @Override
    protected String getRowStyleClass(Object row) {
        SspPerson sspPerson = (SspPerson) row;

        if (sspPerson.getDeliveryStatus() == null) {
            return ROW_NOT_VALID_STYLE;
        } else if (sspPerson.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_FINALIZED) {
            return ROW_FINALIZED_STYLE;
        } else if (sspPerson.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_VALIDATED) {
            return ROW_VALIDATED_STYLE;
        } else if (sspPerson.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_PREVALIDATED) {
            return ROW_PREVALIDATED_STYLE;
        } else if (sspPerson.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_IMPORTED) {
            return ROW_DATA_IMPORTED_STYLE;
        } else if (sspPerson.getPlausiStatus() >= CodegroupUtility.MEB_PLAUSISTATUS_VALID) {
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
            String cantons = "";
            for (Long canton : getCantonsForActUser()) {
                String selected = "";
                if (canton.equals(getFilterCanton())) {
                    selected = " selected";
                }

                if (canton > 0L) {
                    cantons += "<option value=\"" + canton + "\"" + selected + ">"
                            + getLocalizationManager().getCodeGroupValueById(CodegroupUtility.CANTON, canton) + " (" + canton + ")</option>";
                }
            }
            return cantons;
        }

        return "";
    }

    @Override
    public JSNumber getBuffSize() {
        return BUFFSIZE;
    }
}
