/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PersonTableManager.java 637 2010-11-24 11:59:00Z msc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ognl.OgnlException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.admin.bfs.sbg.dhtmlx.CommandDispatcher.EDIT;
import ch.admin.bfs.sbg.dhtmlx.callback.RefreshPersonButtonsCallback;
import ch.admin.bfs.sbg.dhtmlx.callback.ValidateCallback;
import ch.admin.bfs.sbg.dhtmlx.table.SbgPlausiErrorColumn;
import ch.bfs.meb.exception.SessionTimeoutException;
import ch.bfs.meb.sbg.web.resultmapper.PersonListTableResultMapper;
import ch.bfs.meb.sbg.web.resultmapper.PersonTableResultMapper;
import ch.bfs.meb.sbg.web.service.IDeliveryService;
import ch.bfs.meb.sbg.web.service.IPersonService;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDelivery;
import ch.bfs.meb.sbg.web.ws.sbgevent.Event;
import ch.bfs.meb.sbg.web.ws.sbgperson.Person;
import ch.bfs.meb.sbg.web.ws.sbgperson.PersonList;
import ch.bfs.meb.sbg.web.ws.sbgperson.PersonResult;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
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
import ch.bfs.meb.web.commons.dhtmlx.table.Column.EDITOR;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.EditType;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ch.bfs.meb.web.commons.util.IFilterService;

/**
 * This Class represents a PersonTableManager and acts as a controller for the
 * Person Table.
 *
 * @author $Author: msc $
 * @version $Revision: 637 $
 */
@Scope("session")
@Component("personTableManager")
public class PersonTableManager extends FilteredTableManagerBase {
    public static final String COLUMN_PID_ID = "pid";
    public static final String COLUMN_PID_NAME_KEY = "personTable.column.pid.name";
    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "personTable.column.canton.name";
    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_VERSION_NAME_KEY = "personTable.column.version.name";
    public static final String COLUMN_IDTYPE_ID = "idType";
    public static final String COLUMN_IDTYPE_NAME_KEY = "personTable.column.idType.name";
    public static final String COLUMN_ID_ID = "id";
    private String _sortCol = COLUMN_ID_ID;
    public static final String COLUMN_ID_NAME_KEY = "personTable.column.id.name";
    public static final String COLUMN_SEX_ID = "sex";
    public static final String COLUMN_SEX_NAME_KEY = "personTable.column.sex.name";
    public static final String COLUMN_BIRTHDATE_ID = "birthDate";
    public static final String COLUMN_BIRTHDATE_NAME_KEY = "personTable.column.birthDate.name";
    public static final String COLUMN_NEWBIRTHDATE_ID = "newBirthDate";
    public static final String COLUMN_NEWBIRTHDATE_NAME_KEY = "personTable.column.newBirthDate.name";
    public static final String COLUMN_USERCOMMENT_ID = "userComment";
    public static final String COLUMN_USERCOMMENT_NAME_KEY = "personTable.column.userComment.name";
    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "personTable.column.plausistatus.name";
    public static final String COLUMN_STATUS_ID = "status";
    public static final String COLUMN_STATUS_NAME_KEY = "personTable.column.status.name";
    public static final String COLUMN_MODUSER_ID = "modUser";
    public static final String COLUMN_MODUSER_NAME_KEY = "personTable.column.modified.name";
    public static final String COLUMN_MODDATE_ID = "modDate";
    public static final String COLUMN_MODDATE_NAME_KEY = "personTable.column.modDate.name";
    public static final String COLUMN_VALIDUSER_ID = "validationUser";
    public static final String COLUMN_VALIDUSER_NAME_KEY = "personTable.column.validUser.name";
    public static final String COLUMN_VALIDDATE_ID = "validationDate";
    public static final String COLUMN_VALIDDATE_NAME_KEY = "personTable.column.validDate.name";
    public static final String COLUMN_ORIGINTEXT_ID = "deliveryText";
    public static final String COLUMN_ORIGINTEXT_NAME_KEY = "personTable.column.originText.name";
    public static final String MANAGER_NAME = "person";
    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;
    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(PersonTableManager.class);
    private static final String ROW_FINALIZED_STYLE = "dhx_row_finalized";
    private static final String ROW_VALID_STYLE = "dhx_row_valid";
    private static final String ROW_READY_STYLE = "dhx_row_ready";
    private static final String ROW_NOT_VALID_STYLE = "dhx_row_not_valid";
    private static final String ROW_IMPORTED_STYLE = "dhx_row_imported";
    private static final JSNumber BUFFSIZE = new JSNumber(500);
    private static final String COLUMN_PLAUSIERROR_ID = "plausiErrors";
    private static final int BUFFERLEN = 500;
    private final String UPDATE_LOCK = "SbgPersonUpdateLock";
    private int _sortIndex = 1;

    private boolean _ascSort = true;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    private IPersonService _personService;

    @Autowired
    private IDeliveryService _deliveryService;

    @Autowired
    private IFilterService _filterService;

    @Autowired
    private DeliveryTableManager _deliveryTableManager;

    @Autowired
    private EventTableManager _eventTableManager;

    @Autowired
    private PersonFilterTableManager _filterTableManager;

    @Autowired
    private PersonWhereTableManager _whereTableManager;

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

    private IDhtmlxControl getEventTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return EventTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return EventTableManager.MANAGER_NAME;
            }
        };
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

    @Override
    public boolean isServerSort() {
        return true;
    }

    /**
     * Initializes a new PersonTableManager. This is a callback interface. This
     * methode is used to initialize a new Manager and would called only once.
     */
    public void create() throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        setFilterVersion(user.getLastFilterVersion());
        if (user.getLastFilterCanton() == null || user.getLastFilterCanton() <= 0) {
            List<Long> cantons = _deliveryService.getFilterCantonsForActUser(_localizationManager);
            setFilterCanton(cantons.get(0) > 0L ? cantons.get(0) : cantons.get(1));
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        // This table is initialy master
        setMaster(true);

        addColumn(new IdentityColumn(COLUMN_PID_ID, COLUMN_PID_NAME_KEY, getLocalizationManager()));

        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 6);
        cantonColumn.setEditType(EditType.readonly);
        cantonColumn.setColor(COLOR.LIGHTGREY);
        addColumn(cantonColumn);

        Column versionColumn = new ReadOnlyColumn(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 4);
        addColumn(versionColumn);

        Column idTypColumn = new BigDecimalColumn(COLUMN_IDTYPE_ID, COLUMN_IDTYPE_NAME_KEY, getLocalizationManager(), 4);
        idTypColumn.setDefault(new Long(1));
        addColumn(idTypColumn);

        addColumn(new Column(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager(), 8));

        ComboCodeGroupColumn comboSexTypeColumn = new ComboCodeGroupColumn(COLUMN_SEX_ID, COLUMN_SEX_NAME_KEY, getLocalizationManager(), CodegroupUtility.SEX,
                4);
        //comboSexTypeColumn.setDefault(comboSex.get(0).getCode());
        addColumn(comboSexTypeColumn);

        Column birthdateColumn = new Column(COLUMN_BIRTHDATE_ID, COLUMN_BIRTHDATE_NAME_KEY, getLocalizationManager(), 6);
        addColumn(birthdateColumn);

        Column newBirthdateColumn = new DateColumn(COLUMN_NEWBIRTHDATE_ID, COLUMN_NEWBIRTHDATE_NAME_KEY, getLocalizationManager());
        addColumn(newBirthdateColumn);

        Column commentColumn = new Column(COLUMN_USERCOMMENT_ID, COLUMN_USERCOMMENT_NAME_KEY, getLocalizationManager(), 49);
        commentColumn.setDefault("");
        addColumn(commentColumn);

        Column plausiStatusColumn = new PlausistatusColumn(COLUMN_PLAUSISTATUS_ID, COLUMN_PLAUSISTATUS_NAME_KEY, CodegroupUtility.SBG_PLAUSISTATUS,
                getLocalizationManager(), 8);
        addColumn(plausiStatusColumn);
        // hidden column for plausierror data
        addColumn(new SbgPlausiErrorColumn(COLUMN_PLAUSIERROR_ID, getLocalizationManager()));

        ComboCodeGroupColumn statusColumn = new ComboCodeGroupColumn(COLUMN_STATUS_ID, COLUMN_STATUS_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.SBG_PERSONSTATUS, 8);
        // statusColumn.setDefault(comboStatus.get(1).getCode());
        if (!user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
            statusColumn.setColor(COLOR.LIGHTGREY);
        }
        addColumn(statusColumn);

        Column modUserColumn = new ReadOnlyColumn(COLUMN_MODUSER_ID, COLUMN_MODUSER_NAME_KEY, getLocalizationManager(), 8);
        modUserColumn.setDefault("");
        addColumn(modUserColumn);
        Column modDateColumn = new DateColumn(COLUMN_MODDATE_ID, COLUMN_MODDATE_NAME_KEY, getLocalizationManager());
        modDateColumn.setEditType(EditType.readonly);
        modDateColumn.setColor(COLOR.LIGHTGREY);
        modDateColumn.setDefault("");
        addColumn(modDateColumn);

        Column valUserColumn = new ReadOnlyColumn(COLUMN_VALIDUSER_ID, COLUMN_VALIDUSER_NAME_KEY, getLocalizationManager(), 8);
        valUserColumn.setDefault("");
        addColumn(valUserColumn);
        Column valDateColumn = new DateColumn(COLUMN_VALIDDATE_ID, COLUMN_VALIDDATE_NAME_KEY, getLocalizationManager());
        valDateColumn.setEditType(EditType.readonly);
        valDateColumn.setColor(COLOR.LIGHTGREY);
        valDateColumn.setDefault("");
        addColumn(valDateColumn);

        Column origTextColumn = new Column(COLUMN_ORIGINTEXT_ID, COLUMN_ORIGINTEXT_NAME_KEY, getLocalizationManager(), 11);
        origTextColumn.setEditorType(EDITOR.HTML_MULTILINE_READ_ONLY);
        origTextColumn.setColor(COLOR.LIGHTGREY);
        origTextColumn.setDefault("");
        addColumn(origTextColumn);

        // auto loading
        enableAutoLoading();

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, _eventTableManager, _maintainglobals);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, _eventTableManager, onRowSelectCallback, 250, _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, _eventTableManager, _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, _eventTableManager, _maintainglobals);
        IJavaScriptFunction refreshButtonsCallback = new RefreshPersonButtonsCallback(this);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateMaintainCallback(this, _eventTableManager);
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this, _eventTableManager, _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, _eventTableManager);

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
        registerCallback(new OnLoadErrorCallback(this, EventTableManager.MANAGER_NAME, null));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertRowCallback(this, _eventTableManager, _maintainglobals));
        registerCallback(new DeleteRowCallback(this, _eventTableManager, _maintainglobals));
        registerCallback(new SaveCallback(this, _eventTableManager, _maintainglobals));
        registerCallback(new ValidateCallback(this));
        registerCallback(new FilterCallback(this, _eventTableManager, _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(new SwitchMasterCallback(this, _eventTableManager, _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(new ExportCsvCallback(this, _eventTableManager, null, false, _maintainglobals));

        registerCallback(refreshButtonsCallback);
        registerCallback(new ReloadSelectedCallback(this));
        registerCallback(new ReloadAllCallback(this));
        registerCallback(onAfterUpdateCallback);
        registerCallback(onGridReconstructedCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(displayNumbersCallback);

        _whereTableManager.create(this);
        _eventTableManager.create(this);
    }

    @Override
    protected String getRowStyleClass(Object row) {
        Person person = (Person) row;
        long status = person.getStatus() == null ? 0L : person.getStatus();
        if (isDeliveryFinalized(person)) {
            return ROW_FINALIZED_STYLE;
        } else if (status >= CodegroupUtility.SBG_PERSONSTATUS_VALIDATED) {
            return ROW_VALID_STYLE;
        } else if (status == CodegroupUtility.SBG_PERSONSTATUS_IMPORTED) {
            return ROW_IMPORTED_STYLE;
        } else if ((person.getPlausiStatus() == CodegroupUtility.SBG_PLAUSISTATUS_VALID)
                || (person.getPlausiStatus() == CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED)) {
            return ROW_READY_STYLE;
        } else {
            return ROW_NOT_VALID_STYLE;
        }
    }

    /**
     * Get finalized state if possible - changes on finalized objects are caught
     * on service layer anyway
     *
     * @param person
     * @return
     */
    public boolean isDeliveryFinalized(Person person) {
        boolean isDeliveryFinalized = false;
        if (person != null && person.getDeliveryId() != null) {
            try {
                SbgDelivery delivery = (SbgDelivery) _deliveryTableManager.getRowData(person.getDeliveryId().toString());
                if (delivery != null && delivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED) {
                    isDeliveryFinalized = true;
                }
            } catch (SessionTimeoutException e) {
                // delivery of person not in delivery tablemanager
                return false;
            }
        }
        return isDeliveryFinalized;
    }

    /**
     * Get finalized state if possible - changes on finalized objects are caught
     * on service layer anyway
     *
     * @param event
     * @return
     */
    public boolean isDeliveryFinalized(Event event) {
        Person person;
        try {
            person = (Person) getRowData(event.getPid().toString());
        } catch (SessionTimeoutException e) {
            PersonResult result = _personService.getPersonById(event.getPid());
            if (result.getPerson() != null) {
                person = result.getPerson();
                updateCache(event.getPid().toString(), person);
            } else {
                return false;
            }
        }
        return isDeliveryFinalized(person);
    }

    /**
     * Get imported state of person if possible
     */
    public boolean isImported(Event event) {
        Person person;
        try {
            person = (Person) getRowData(event.getPid().toString());
        } catch (SessionTimeoutException e) {
            PersonResult result = _personService.getPersonById(event.getPid());
            if (result.getPerson() != null) {
                person = result.getPerson();
                updateCache(event.getPid().toString(), person);
            } else {
                return false;
            }
        }

        if (person != null && person.getStatus() != null) {
            return person.getStatus() == CodegroupUtility.SBG_PERSONSTATUS_IMPORTED;
        } else {
            return false;
        }
    }

    protected boolean isReadOnly(Person person) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (person.getStatus() == null) {
            return true;
        }

        switch ((int) (long) person.getStatus()) {
        case (int) CodegroupUtility.SBG_PERSONSTATUS_VALIDATED:
            return !user.isInRole(SecurityConstants.ROLE_SBG_EV) || isDeliveryFinalized(person);
        case (int) CodegroupUtility.SBG_PERSONSTATUS_DELIVERED:
            return !user.isInRole(SecurityConstants.ROLE_SBG_DL);
        default:
            return true;
        }
    }

    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<String, String>();
        Person person = (Person) row;
        StringBuffer readOnlyString = new StringBuffer();
        // COLUMN_CANTON, COLUMN_VERSION, COLUMN_IDTYPE, COLUMN_ID, COLUMN_SEX,
        // COLUMN_BIRTHDATE, COLUMN_NEWBIRTHDATE,
        // COLUMN_USERCOMMENT, COLUMN_PLAUSISTATUS, COLUMN_STATUS,
        // COLUMN_MODUSER, COLUMN_MODDATE
        // COLUMN_VALIDUSER, COLUMN_VALIDDATE, COLUMN_ORIGINTEXT

        if (isReadOnly(person)) {
            readOnlyString.append("111111111111111");
        } else if (person.getStatus().equals(CodegroupUtility.SBG_PERSONSTATUS_VALIDATED)) {
            readOnlyString.append("000000000000000");
        } else {
            readOnlyString.append("000000000100000");
        }
        userData.put("readOnlyCells", new String(readOnlyString));

        long status = person.getStatus() == null ? 0L : person.getStatus();
        if (isDeliveryFinalized(person)) {
            userData.put("personState", "" + (CodegroupUtility.SBG_PERSONSTATUS_VALIDATED + 1L));
        } else if (status >= CodegroupUtility.SBG_PERSONSTATUS_VALIDATED) {
            userData.put("personState", "" + CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);
        } else if (status >= CodegroupUtility.SBG_PERSONSTATUS_DELIVERED) {
            userData.put("personState", "" + CodegroupUtility.SBG_PERSONSTATUS_DELIVERED);
        } else {
            userData.put("personState", "" + CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);
        }
        return userData;
    }

    /**
     * Gets all rows for person Table with maximum buffer rows, starting at
     * start row.
     *
     * @param start  start from row index
     * @param buffer maximum number of rows
     * @return requested Person rows from start with buffer number of rows
     */
    public PersonList getRows(int start, int buffer) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale((getLocalizationManager().getLocale()).toString());

        return _personService.getPersons(start, buffer, sortContext, getFilterContext(), getFilterVersion(), getFilterCanton());
    }

    /**
     * Gets all rows depending on the selected eventIds.
     *
     * @param selectedRowIds list with event ids
     * @return List with all Persons with given eventId
     */
    public PersonList getRows(List<Long> selectedRowIds) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale((getLocalizationManager().getLocale()).toString());

        return _personService.getPersonsOwnedByEvents(selectedRowIds, sortContext);
    }

    /**
     * Get rows using the parameters from the request
     *
     * @param params Request parameters
     * @return List with persons
     */
    public PersonList getRows(ParameterList params) {
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
     * @param start  start from row index
     * @param buffer maximum number of rows
     * @return requested Event rows from start with buffer number of rows
     */
    public PersonList getRows(ParameterList params, int start, int buffer) {
        PersonList rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            if (selectedRowIds.size() == 0) {
                rows = new PersonList();
                rows.setState(1);
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
    protected List<Person> getExportRows(ParameterList params) {
        return getRows(params, -1, -1).getPersons();
    }

    @Override
    protected String getExportFileName() {
        return "Persons.csv";
    }

    /**
     * Gets rows including the table header
     *
     * @param params request parameters
     * @return xml with the requested rows
     * @throws DhtmlxException Thrown when a mapping error occurs
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        WebFilterContext filterContext = new WebFilterContext();
        WebFilterListResult webFilterListResult = _filterService.getFiltersForRefObject(CodegroupUtility.SBG_OBJECTTYPE_PERSON);
        for (WebFilter webFilter : webFilterListResult.getFilters()) {
            if (webFilter.getIsActive() && webFilter.getIsDefault()) {
                filterContext.getFilter().add(webFilter);
            }
        }
        setFilterContext(filterContext);

        if (params.getRowsLoaded() == 0) {
            Long resultSize;

            PersonList result = getRows(params);
            resultSize = result.getResultSize();
            PersonListTableResultMapper resultMapper = new PersonListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

            HashMap<String, String> userdata = new HashMap<String, String>();
            userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
            return toXMLStream(resultMapper, true, true, userdata);
        } else {
            return load(params);
        }
    }

    /**
     * Gets rows without header information, can be used to incrementaly loading
     * rows
     *
     * @param params request parameters
     * @return xml with the requested rows
     * @throws DhtmlxException Thrown when a mapping error occurs
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        Long resultSize;

        PersonList result = getRows(params);
        resultSize = result.getResultSize();
        PersonListTableResultMapper resultMapper = new PersonListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        return toXMLStream(resultMapper, params.getRowsLoaded() == 0, false, userdata);
    }

    public DhtmlxTableXML sort(ParameterList params) throws DhtmlxException {
        _sortIndex = getColumnIndex(params.getColIndex());
        Column c = _columns.get(_sortIndex);
        _sortCol = c.getName();
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

        PersonList result = getRows(rowsloaded, bufferlen);
        resultSize = result.getResultSize();
        PersonListTableResultMapper resultMapper = new PersonListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        return toXMLStream(resultMapper, rowsloaded == 0, userdata, false);
    }

    /**
     * Tries to validate the selected persons.
     *
     * @param params
     * @return TextHttpResult containing error message or null if ok
     * @throws DhtmlxException
     */
    public IHttpResult validate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedPersonIds = new ArrayList<Long>();
        for (Long selRowId : selectedRowIds) {
            selectedPersonIds.add(((Person) getRowData(selRowId.toString())).getPid());
        }

        PersonResult result = _personService.validatePersons(selectedPersonIds, getLocalizationManager().getLanguage());

        //		if(result.getState() > 1)
        //		{ // Error occured
        //			return new TextHttpResult(getLocalizationManager().getMessage(result.getMessage()));
        //		}

        // Maps result
        PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);

        return toXMLDataStream(resultMapper);
    }

    /**
     * Gets the requested row from DB.
     *
     * @param params
     * @return
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML loadPerson(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        PersonResult result = _personService.getPersonById(new Long(sid));

        // Maps result
        PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
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

        PersonResult result = _personService.getPersonById(new Long(sid));

        // Maps result
        PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.REFRESH, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Gets all unmodified rows from DB. This methode sets the initial values.
     *
     * @param params
     * @return
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undo(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        if (!params.getEditorStatus().equals(EDIT.INSERT)) {
            PersonResult result = _personService.getPersonById(new Long(sid));

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            PersonResult result = new PersonResult();
            result.setState(1);
            result.setPerson(new Person());
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            // merge new data record with cache
            Person person = (Person) merge(params);

            PersonResult result = _personService.updatePerson(person, getLocalizationManager().getLanguage());

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            Person person = (Person) merge(params);

            PersonResult result = _personService.deletePerson(person);

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get all grid data and try to find a unique deliveryid
            Long deliveryId = null;
            for (Object obj : getData()) {
                Person person = (Person) obj;
                if (deliveryId == null) {
                    deliveryId = person.getDeliveryId();
                } else if (!deliveryId.equals(person.getDeliveryId())) {
                    deliveryId = null;
                    break;
                }
            }

            // Get the source id
            String sid = params.getRowId();

            // Merge with an empty record
            Person person = (Person) merge(new Person(), params);

            PersonResult result;
            if (deliveryId != null) {
                person.setDeliveryId(deliveryId);
                person.setStatus(CodegroupUtility.SBG_PERSONSTATUS_DELIVERED);
                result = _personService.insertPerson(person, getLocalizationManager().getLanguage());
            } else {
                result = new PersonResult();
                result.setState(ResultMapperBase.FAILURE);
                result.setMessage("insert.person.unique.message");
            }

            // Maps result
            PersonTableResultMapper resultMapper = new PersonTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public JSNumber getBuffSize() {
        return BUFFSIZE;
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
            for (Long canton : _deliveryService.getFilterCantonsForActUser(_localizationManager)) {
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
}
