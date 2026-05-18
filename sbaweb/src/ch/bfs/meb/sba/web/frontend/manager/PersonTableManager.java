/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb
 */
package ch.bfs.meb.sba.web.frontend.manager;

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

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.web.dhtmlx.callback.RefreshPersonButtonsCallback;
import ch.bfs.meb.sba.web.frontend.resultmapper.PersonListTableResultMapper;
import ch.bfs.meb.sba.web.frontend.resultmapper.PersonTableResultMapper;
import ch.bfs.meb.sba.web.service.ICantonService;
import ch.bfs.meb.sba.web.service.IPersonService;
import ch.bfs.meb.sba.web.ws.sbaperson.*;
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
 * This Class represents a PersonTableManager for the maintain tab and acts
 * as a controller for the Person Table.
 */
@Scope("session")
@Component("personTableManager")
public class PersonTableManager extends FilteredTableManagerBase implements PlausiErrorColumn.IPlausiErrorDataUpdate {
    private final String UPDATE_LOCK = "SbaPersonUpdateLock";

    private static final int BUFFERLEN = 500;
    private static final JSNumber BUFFSIZE = new JSNumber(500);

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
    public static final String COLUMN_RESIDENCE_ID = "residence";
    public static final String COLUMN_RESIDENCE_NAME_KEY = "personTable.column.residence.name";
    public static final String COLUMN_HISTORIC_RESIDENCE_ID = "historicResidence";
    public static final String COLUMN_HISTORIC_RESIDENCE_NAME_KEY = "personTable.column.historicResidence.name";
    public static final String COLUMN_COUNTRY_ID = "country";
    public static final String COLUMN_COUNTRY_NAME_KEY = "personTable.column.country.name";
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
    private ICantonService _cantonService;

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

    @Autowired
    private QualificationTableManager _qualificationTableManager;

    private String _sortCol = COLUMN_PLAUSISTATUS_ID;

    private boolean _ascSort = true;

    private MunicipalityDynamicComboColumn _residenceColumn;
    private MunicipalityDynamicComboColumn _historicResidenceColumn;

    protected final HashMap<Long, List<PlausiError>> _loadedPlausiErrors = new HashMap<>();

    public PersonTableManager() {
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
            List<Long> cantons = _cantonService.getFilterCantonsForActUser();
            setFilterCanton(cantons.get(0) > 0L ? cantons.get(0) : (cantons.size() > 1 ? cantons.get(1) : null));
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
                CodegroupUtility.CANTON, 6);
        cantonColumn.setEditType(EditType.editwheninserted);
        addColumn(cantonColumn);

        Column deliveryCodeColumn = new Column(COLUMN_DELIVERYCODE_ID, COLUMN_DELIVERYCODE_NAME_KEY, getLocalizationManager(), 8);
        deliveryCodeColumn.setEditType(EditType.editwheninserted);
        addColumn(deliveryCodeColumn);

        addColumn(new Column(COLUMN_IDTYPE_ID, COLUMN_IDTYPE_NAME_KEY, getLocalizationManager(), 6));

        addColumn(new Column(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager(), 8));

        ComboCodeGroupColumn sexColumn = new ComboCodeGroupColumn(COLUMN_SEX_ID, COLUMN_SEX_NAME_KEY, getLocalizationManager(), CodegroupUtility.SEX, 8);
        addColumn(sexColumn);

        dateColumn = new DateColumn(COLUMN_BIRTHDATE_ID, COLUMN_BIRTHDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setDefault("");
        addColumn(dateColumn);

        _residenceColumn = new MunicipalityDynamicComboColumn(COLUMN_RESIDENCE_ID, COLUMN_RESIDENCE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MUNICIPALITY, getFilterCanton(), 10, getControlName());
        addColumn(_residenceColumn);
        _historicResidenceColumn = new MunicipalityDynamicComboColumn(COLUMN_HISTORIC_RESIDENCE_ID, COLUMN_HISTORIC_RESIDENCE_NAME_KEY,
                getLocalizationManager(), CodegroupUtility.MUNICIPALITY_HIST, getFilterCanton(), 10, getControlName());
        addColumn(_historicResidenceColumn);

        ComboCodeGroupColumn countryColumn = new ComboCodeGroupColumn(COLUMN_COUNTRY_ID, COLUMN_COUNTRY_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.COUNTRY, false, 8, true);
        addColumn(countryColumn);

        Column deliveryStatusColumn = new StatusColumn(COLUMN_DELIVERYSTATUS_ID, COLUMN_DELIVERYSTATUS_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MEB_DATASTATUS, 8);
        if (!user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
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
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, _qualificationTableManager, _maintainglobals);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, _qualificationTableManager, onRowSelectCallback, 250, _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, _qualificationTableManager, _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, _qualificationTableManager, _maintainglobals);
        IJavaScriptFunction refreshButtonsCallback = new RefreshPersonButtonsCallback(this, COLUMN_DELIVERYSTATUS_ID);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateMaintainCallback(this, _qualificationTableManager);
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this, _qualificationTableManager, _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, _qualificationTableManager);

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
        registerCallback(new OnLoadErrorCallback(this, QualificationTableManager.MANAGER_NAME, null));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertRowCallback(this, _qualificationTableManager, Master.MANAGER_MUST_BE_MASTER, _maintainglobals, PERSON_MUSTBEMASTER_MESSAGE));
        registerCallback(new DeleteRowCallback(this, _qualificationTableManager, _maintainglobals, ALERT_DELETE_PERSON_MESSAGE));
        registerCallback(new SaveCallback(this, _qualificationTableManager, true, _maintainglobals));
        registerCallback(new FilterCallback(this, _qualificationTableManager, null, _filterTableManager, _whereTableManager, _maintainglobals, true));
        registerCallback(new SwitchMasterCallback(this, _qualificationTableManager, _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(refreshButtonsCallback);
        registerCallback(new ExportCsvCallback(this, _qualificationTableManager, null, false, _maintainglobals));
        registerCallback(new ValidateMultipleCallback(this, CodegroupUtility.MEB_APPLICATION_SBA, false));
        registerCallback(new UndoValidateMultipleCallback(this, COLUMN_DELIVERYSTATUS_ID));
        registerCallback(new ReloadSelectedCallback(this));
        registerCallback(new ReloadAllCallback(this));
        registerCallback(onAfterUpdateCallback);
        registerCallback(onGridReconstructedCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(displayNumbersCallback);

        _whereTableManager.create(this);
        _qualificationTableManager.create(this);
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
    public SbaPersonListResult getRows(int start, int buffer) {
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
    public SbaPersonListResult getRows(List<Long> selectedRowIds) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _personService.getPersonsOwnedByQualifications(selectedRowIds, sortContext);
    }

    /**
     * Get rows using the parameters from the request
     * 
     * @param params
     *            Request parameters
     * @return List with persons
     */
    public SbaPersonListResult getRows(ParameterList params) {
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
    public SbaPersonListResult getRows(ParameterList params, int start, int buffer) {
        SbaPersonListResult rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            if (!_qualificationTableManager.getCanton().equals(getFilterCanton())) {
                setFilterCanton(_qualificationTableManager.getCanton());

                _residenceColumn.setCanton(getFilterCanton());
                _historicResidenceColumn.setCanton(getFilterCanton());
            }

            if (selectedRowIds.size() == 0) {
                rows = new SbaPersonListResult();
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
    protected List<SbaPerson> getExportRows(ParameterList params) {
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
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        if (params.getRowsLoaded() == 0) {
            Long resultSize;

            SbaPersonListResult result = getRows(params);
            clearPlausiErrorData(result.getPersons());
            resultSize = result.getMaxNrOfPersons();
            PersonListTableResultMapper resultMapper = new PersonListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

            HashMap<String, String> userdata = new HashMap<>();
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
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        Long resultSize;
        Long oldCanton = getFilterCanton();

        SbaPersonListResult result = getRows(params);
        clearPlausiErrorData(result.getPersons());
        resultSize = result.getMaxNrOfPersons();
        PersonListTableResultMapper resultMapper = new PersonListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

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

        _residenceColumn.setCanton(getFilterCanton());
        _historicResidenceColumn.setCanton(getFilterCanton());

        Long resultSize;

        SbaPersonListResult result = getRows(rowsloaded, bufferlen);
        clearPlausiErrorData(result.getPersons());
        resultSize = result.getMaxNrOfPersons();
        PersonListTableResultMapper resultMapper = new PersonListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        return toXMLStream(resultMapper, rowsloaded == 0, userdata, !MebUtils.areEqual(oldCanton, getFilterCanton()));
    }

    /**
     * Update person
     * 
     * @param params
     *            contains all parameters
     * @return XML with updated row
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            // merge data record with cache
            SbaPerson person = (SbaPerson) merge(params);
            PlausiErrorColumn.setPlausiErrorData(person.getPersonId(), params, this);

            SbaPersonResult result = _personService.updatePerson(person, _loadedPlausiErrors.get(person.getPersonId()),
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
            SbaPerson person = (SbaPerson) merge(new SbaPerson(), params);

            person.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            person.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);

            SbaPersonResult result = _personService.insertPerson(person, params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        synchronized (UPDATE_LOCK) {
            String selected = params.getRowId();
            SbaPersonResult result = _personService.deletePerson((SbaPerson) getRowData(selected),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.DELETE, selected, result, getLocalizationManager());

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
            SbaPersonResult result = _personService.getPersonById(new Long(sid));

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            SbaPersonResult result = new SbaPersonResult();
            result.setState(ResultBase.OK);
            result.setPerson(new SbaPerson());
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    /**
     * Reload unmodified data from DB.
     */
    public DhtmlxTableDataXML reload(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        SbaPersonResult result = _personService.getPersonById(new Long(sid));

        // Maps result
        PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.REFRESH, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * (Pre-)validate list of persons
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     */
    public DhtmlxTableDataXML validate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedPersonIds = new ArrayList<>();
        for (Long selRowId : selectedRowIds) {
            selectedPersonIds.add(((SbaPerson) getRowData(selRowId.toString())).getPersonId());
        }
        SbaPersonResult result = _personService.validatePersons(selectedPersonIds, false);

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
     */
    public DhtmlxTableDataXML undoValidate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedPersonIds = new ArrayList<>();
        for (Long selRowId : selectedRowIds) {
            selectedPersonIds.add(((SbaPerson) getRowData(selRowId.toString())).getPersonId());
        }
        SbaPersonResult result = _personService.validatePersons(selectedPersonIds, true);

        // Maps result
        PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);

        return toXMLDataStream(resultMapper);
    }

    /**
     * Returns municipality suggestions as xml.
     */
    public IHttpResult municipalityXml(ParameterList params) {
        //		String pos = params.getParameter("pos");
        String mask = params.getParameter("mask");
        String codeGroup = params.getParameter("codeGroup");
        return MunicipalityDynamicComboColumn.createComboXml(mask, getFilterCanton(), codeGroup, getLocalizationManager());
    }

    protected void clearPlausiErrorData(List<SbaPerson> persons) {
        for (SbaPerson person : persons) {
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

    public IHttpResult plausierrorData(ParameterList params) {
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

    protected boolean isReadOnly(SbaPerson sbaPerson) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (sbaPerson.getDeliveryStatus() == null) {
            return !user.isInRole(SecurityConstants.ROLE_SBA_DV);
        }

        switch ((int) (long) sbaPerson.getDeliveryStatus()) {
        case (int) CodegroupUtility.MEB_DATASTATUS_VALIDATED:
            return !user.isInRole(SecurityConstants.ROLE_SBA_EV);
        case (int) CodegroupUtility.MEB_DATASTATUS_PREVALIDATED:
            return !user.isInRole(SecurityConstants.ROLE_SBA_DV);
        case (int) CodegroupUtility.MEB_DATASTATUS_DELIVERED:
            return !user.isInRole(SecurityConstants.ROLE_SBA_DL);
        default:
            return true;
        }
    }

    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<>();
        SbaPerson sbaPerson = (SbaPerson) row;
        String readOnlyStr;

        if (isReadOnly(sbaPerson)) {
            readOnlyStr = "11111111111111111111";
        } else {
            if (sbaPerson.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                readOnlyStr = "00000000000011111101";
            } else {
                readOnlyStr = "00000000001011111101";
            }
        }

        userData.put("readOnlyCells", readOnlyStr);
        userData.put("plausierror", "empty");
        return userData;
    }

    @Override
    protected String getRowStyleClass(Object row) {
        SbaPerson sbaPerson = (SbaPerson) row;

        if (sbaPerson.getDeliveryStatus() == null) {
            return ROW_NOT_VALID_STYLE;
        } else if (sbaPerson.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_FINALIZED) {
            return ROW_FINALIZED_STYLE;
        } else if (sbaPerson.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_VALIDATED) {
            return ROW_VALIDATED_STYLE;
        } else if (sbaPerson.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_PREVALIDATED) {
            return ROW_PREVALIDATED_STYLE;
        } else if (sbaPerson.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_IMPORTED) {
            return ROW_DATA_IMPORTED_STYLE;
        } else if (sbaPerson.getPlausiStatus() >= CodegroupUtility.MEB_PLAUSISTATUS_VALID) {
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
                String selected = "";
                if (canton.equals(getFilterCanton())) {
                    selected = " selected";
                }

                if (canton > 0L) {
                    cantons.append("<option value=\"").append(canton).append("\"").append(selected).append(">")
                            .append(getLocalizationManager().getCodeGroupValueById(CodegroupUtility.CANTON, canton))
                            .append(" (").append(canton).append(")</option>");
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
