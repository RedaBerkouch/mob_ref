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

import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.web.dhtmlx.callback.RefreshQualificationButtonsCallback;
import ch.bfs.meb.sba.web.frontend.resultmapper.QualificationListTableResultMapper;
import ch.bfs.meb.sba.web.frontend.resultmapper.QualificationTableResultMapper;
import ch.bfs.meb.sba.web.service.ICantonService;
import ch.bfs.meb.sba.web.service.IPersonService;
import ch.bfs.meb.sba.web.service.IQualificationService;
import ch.bfs.meb.sba.web.ws.sbaperson.SbaPersonResult;
import ch.bfs.meb.sba.web.ws.sbaqualification.*;
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
 * This Class represents a QualificationTableManager for the maintain tab and acts
 * as a controller for the Qualification Table.
 */
@Scope("session")
@Component("qualificationTableManager")
public class QualificationTableManager extends FilteredTableManagerBase implements PlausiErrorColumn.IPlausiErrorDataUpdate {
    private final String UPDATE_LOCK = "SbaQualificationUpdateLock";

    private static final int BUFFERLEN = 500;
    private static final JSNumber BUFFSIZE = new JSNumber(500);

    public static final String COLUMN_QUALIFICATIONID_ID = "qualificationId";
    public static final String COLUMN_QUALIFICATIONID_NAME_KEY = "qualificationTable.column.qualificationId.name";
    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_VERSION_NAME_KEY = "qualificationTable.column.version.name";
    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "qualificationTable.column.canton.name";
    public static final String COLUMN_SCHOOLIDTYPE_ID = "schoolIdType";
    public static final String COLUMN_SCHOOLIDTYPE_NAME_KEY = "qualificationTable.column.schoolIdType.name";
    public static final String COLUMN_SCHOOLID_ID = "schoolId";
    public static final String COLUMN_SCHOOLID_NAME_KEY = "qualificationTable.column.schoolId.name";
    public static final String COLUMN_NAMEBURSCHOOL_ID = "nameBurSchool";
    public static final String COLUMN_NAMEBURSCHOOL_NAME_KEY = "qualificationTable.column.nameBurSchool.name";
    public static final String COLUMN_IS_PUBLIC_SCHOOL_ID = "charPublFlg";
    public static final String COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY = "qualificationTable.column.charPublFlg.name";
    public static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_ID = "charPrivSubFlg";
    public static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY = "qualificationTable.column.charPrivSubFlg.name";
    public static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_ID = "charPrivNoSubFlg";
    public static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY = "qualificationTable.column.charPrivNoSubFlg.name";
    public static final String COLUMN_IS_SPECIAL_SCHOOL_ID = "isSpecialSchool";
    public static final String COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY = "qualificationTable.column.isSpecialSchool.name";
    public static final String COLUMN_EDUCATIONTYPE_ID = "educationType";
    public static final String COLUMN_EDUCATIONTYPE_NAME_KEY = "qualificationTable.column.educationType.name";
    public static final String COLUMN_EXAMTYPE_ID = "examType";
    public static final String COLUMN_EXAMTYPE_NAME_KEY = "qualificationTable.column.examType.name";
    public static final String COLUMN_EXAMDATE_ID = "examDate";
    public static final String COLUMN_EXAMDATE_NAME_KEY = "qualificationTable.column.examDate.name";
    public static final String COLUMN_EXAMNR_ID = "examNr";
    public static final String COLUMN_EXAMNR_NAME_KEY = "qualificationTable.column.examNr.name";
    public static final String COLUMN_RESULT_ID = "result";
    public static final String COLUMN_RESULT_NAME_KEY = "qualificationTable.column.result.name";
    public static final String COLUMN_MATURITY_LANGUAGES_ID = "maturityLanguages";
    public static final String COLUMN_MATURITY_LANGUAGES_NAME_KEY = "qualificationTable.column.maturityLanguages.name";
    public static final String COLUMN_DELIVERYSTATUS_ID = "deliveryStatus";
    public static final String COLUMN_DELIVERYSTATUS_NAME_KEY = "qualificationTable.column.deliveryStatus.name";
    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "qualificationTable.column.plausiStatus.name";
    public static final String COLUMN_PLAUSIERROR_ID = "plausierrors";

    public static final String COLUMN_CREATIONUSER_ID = "creationUser";
    public static final String COLUMN_CREATIONUSER_NAME_KEY = "qualificationTable.column.creationUser.name";
    public static final String COLUMN_CREATIONDATE_ID = "creationDate";
    public static final String COLUMN_CREATIONDATE_NAME_KEY = "qualificationTable.column.creationDate.name";

    public static final String COLUMN_MODIFICATIONUSER_ID = "modificationUser";
    public static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "qualificationTable.column.modificationUser.name";
    public static final String COLUMN_MODIFICATIONDATE_ID = "modificationDate";
    public static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "qualificationTable.column.modificationDate.name";

    public static final String COLUMN_PREVELATIONUSER_ID = "prevalidationUser";
    public static final String COLUMN_PREVELATIONUSER_NAME_KEY = "qualificationTable.column.prevalidationUser.name";
    public static final String COLUMN_PREVELATIONDATE_ID = "prevalidationDate";
    public static final String COLUMN_PREVELATIONDATE_NAME_KEY = "qualificationTable.column.prevalidationDate.name";

    public static final String COLUMN_USERTEXT_ID = "userText";
    public static final String COLUMN_USERTEXT_NAME_KEY = "qualificationTable.column.userText.name";

    protected static final String INSERT_QUALIFICATION_UNIQUE_MESSAGE = "insert.qualification.unique.message";
    protected static final String QUALIFICATION_NOMASTER_MESSAGE = "qualification.nomaster.message";

    public static final String MANAGER_NAME = "qualification";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private ICantonService _cantonService;

    @Autowired
    private IQualificationService _qualificationService;

    @Autowired
    private IPersonService _personService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    private QualificationFilterTableManager _filterTableManager;

    @Autowired
    private QualificationWhereTableManager _whereTableManager;

    private PersonTableManager _personTableManager;

    private String _sortCol = COLUMN_PLAUSISTATUS_ID;

    private boolean _ascSort = true;

    protected final HashMap<Long, List<PlausiError>> _loadedPlausiErrors = new HashMap<Long, List<PlausiError>>();

    public QualificationTableManager() throws DhtmlxException {
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
     * Initializes a new QualificationTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    @Override
    public void create() throws DhtmlxException {}

    public void create(PersonTableManager personTableManager) throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        setFilterVersion(user.getLastFilterVersion());
        if (user.getLastFilterCanton() == null || user.getLastFilterCanton() <= 0) {
            List<Long> cantons = _cantonService.getFilterCantonsForActUser();
            setFilterCanton(cantons.get(0) > 0L ? cantons.get(0) : cantons.get(1));
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        _personTableManager = personTableManager;

        DateColumn dateColumn;

        addColumn(new IdentityColumn(COLUMN_QUALIFICATIONID_ID, COLUMN_QUALIFICATIONID_NAME_KEY, getLocalizationManager()));

        addColumn(new ReadOnlyColumn(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 6));

        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 6);
        cantonColumn.setEditType(EditType.readonly);
        cantonColumn.setColor(COLOR.LIGHTGREY);
        addColumn(cantonColumn);

        addColumn(new Column(COLUMN_SCHOOLIDTYPE_ID, COLUMN_SCHOOLIDTYPE_NAME_KEY, getLocalizationManager(), 8));
        addColumn(new Column(COLUMN_SCHOOLID_ID, COLUMN_SCHOOLID_NAME_KEY, getLocalizationManager(), 5));

        ReadOnlyColumn nameBurSchool = new ReadOnlyColumn(COLUMN_NAMEBURSCHOOL_ID, COLUMN_NAMEBURSCHOOL_NAME_KEY, getLocalizationManager(), 6);
        addColumn(nameBurSchool);

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

        ComboCodeGroupColumn educationTypeColumn = new ComboCodeGroupColumn(COLUMN_EDUCATIONTYPE_ID, COLUMN_EDUCATIONTYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.EXAM_EDUCATION_TYPE, false, 8);
        addColumn(educationTypeColumn);

        ComboCodeGroupColumn examTypeColumn = new ComboCodeGroupColumn(COLUMN_EXAMTYPE_ID, COLUMN_EXAMTYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.EXAM_TYPE, false, 8);
        addColumn(examTypeColumn);

        dateColumn = new DateColumn(COLUMN_EXAMDATE_ID, COLUMN_EXAMDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setDefault("");
        addColumn(dateColumn);
        addColumn(new Column(COLUMN_EXAMNR_ID, COLUMN_EXAMNR_NAME_KEY, getLocalizationManager(), 8));

        ComboCodeGroupColumn examResultColumn = new ComboCodeGroupColumn(COLUMN_RESULT_ID, COLUMN_RESULT_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.EXAM_RESULT, false, 8);
        addColumn(examResultColumn);

        ComboCodeGroupColumn maturitylLanguagesColumn = new ComboCodeGroupColumn(COLUMN_MATURITY_LANGUAGES_ID, COLUMN_MATURITY_LANGUAGES_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MATURITY_LANGUAGES, false, 8, true);
        addColumn(maturitylLanguagesColumn);

        Column deliveryStatusColumn = new StatusColumn(COLUMN_DELIVERYSTATUS_ID, COLUMN_DELIVERYSTATUS_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MEB_DATASTATUS, 8);
        deliveryStatusColumn.setEditType(EditType.readonly);
        deliveryStatusColumn.setColor(COLOR.LIGHTGREY);
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

        // auto loading
        enableAutoLoading();

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, _personTableManager, _maintainglobals);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, _personTableManager, onRowSelectCallback, 250, _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, _personTableManager, _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, _personTableManager, _maintainglobals);
        IJavaScriptFunction refreshButtonsCallback = new RefreshQualificationButtonsCallback(this, _personTableManager, COLUMN_DELIVERYSTATUS_ID,
                PersonTableManager.COLUMN_DELIVERYSTATUS_ID);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateMaintainCallback(this, null, null, _personTableManager, null);
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this, _personTableManager, _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, _personTableManager);

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
        registerCallback(new OnLoadErrorCallback(this, PersonTableManager.MANAGER_NAME, null));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertRowCallback(this, _personTableManager, Master.MANAGER_MUST_NOT_BE_MASTER, _maintainglobals, QUALIFICATION_NOMASTER_MESSAGE));
        registerCallback(new DeleteRowCallback(this, _personTableManager, _maintainglobals));
        registerCallback(new SaveCallback(this, _personTableManager, true, _maintainglobals));
        registerCallback(new FilterCallback(this, _personTableManager, null, _filterTableManager, _whereTableManager, _maintainglobals, true));
        registerCallback(new SwitchMasterCallback(this, _personTableManager, _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(refreshButtonsCallback);
        registerCallback(new ExportCsvCallback(this, _personTableManager, null, false, _maintainglobals));
        registerCallback(new ValidateMultipleCallback(this, CodegroupUtility.MEB_APPLICATION_SBA, true)); // prevalidateOnly
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
     * Gets all rows for qualification Table with maximum buffer rows,
     * starting at start row.
     *
     * @param start  start from row index
     * @param buffer maximum number of rows
     * @return requested qualification rows from start with buffer number of
     * rows
     */
    public SbaQualificationListResult getRows(int start, int buffer) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        SbaQualificationListResult qualifications;

        if (getFilterContext() == null || isInInit()) {
            setInInit(false);
            qualifications = new SbaQualificationListResult();
            qualifications.setState(ResultBase.OK);
        } else {
            qualifications = _qualificationService.getQualifications(start, buffer, sortContext, getFilterContext(), getFilterVersion(), getFilterCanton());
        }

        return qualifications;
    }

    /**
     * Gets all rows depending on the selected personIds.
     *
     * @param selectedRowIds list with event ids
     * @return List with all qualifications with given personId
     */
    public SbaQualificationListResult getRows(List<Long> selectedRowIds) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _qualificationService.getQualificationsOwnedByPersons(selectedRowIds, sortContext);
    }

    /**
     * Get rows using the parameters from the request
     *
     * @param params Request parameters
     * @return List with persons
     */
    public SbaQualificationListResult getRows(ParameterList params) {
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
    public SbaQualificationListResult getRows(ParameterList params, int start, int buffer) {
        SbaQualificationListResult rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            if (selectedRowIds.size() == 0) {
                rows = new SbaQualificationListResult();
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
    protected List<SbaQualification> getExportRows(ParameterList params) {
        return getRows(params, -1, -1).getQualifications();
    }

    @Override
    protected String getExportFileName() {
        return "Qualification.csv";
    }

    /**
     * Intializes the qualification table with all rows.
     *
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        Long resultSize;

        setInInit(true);

        SbaQualificationListResult result = getRows(params);
        clearPlausiErrorData(result.getQualifications());
        resultSize = result.getMaxNrOfQualifications();
        QualificationListTableResultMapper resultMapper = new QualificationListTableResultMapper(result, getLocalizationManager(), resultSize,
                params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<String, String>();
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
     * @throws DhtmlxException
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        Long resultSize;

        SbaQualificationListResult result = getRows(params);
        clearPlausiErrorData(result.getQualifications());
        resultSize = result.getMaxNrOfQualifications();
        QualificationListTableResultMapper resultMapper = new QualificationListTableResultMapper(result, getLocalizationManager(), resultSize,
                params.getRowsLoaded());

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

        SbaQualificationListResult result = getRows(rowsloaded, bufferlen);
        clearPlausiErrorData(result.getQualifications());
        resultSize = result.getMaxNrOfQualifications();
        QualificationListTableResultMapper resultMapper = new QualificationListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        return toXMLStream(resultMapper, rowsloaded == 0, false, userdata);
    }

    /**
     * Update qualification
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
            SbaQualification qualification = (SbaQualification) merge(params);
            PlausiErrorColumn.setPlausiErrorData(qualification.getQualificationId(), params, this);

            SbaQualificationResult result = _qualificationService.updateQualification(qualification,
                    _loadedPlausiErrors.get(qualification.getQualificationId()), params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            QualificationTableResultMapper resultMapper = new QualificationTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);
            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            List<Long> selIds = params.getSelectedRows();
            if (selIds.size() != 1 || !params.getSelectedMaster().equals(PersonTableManager.MANAGER_NAME)) {
                return toXMLDataErrorStream(getLocalizationManager().getMessage(INSERT_QUALIFICATION_UNIQUE_MESSAGE), sid);
            }
            SbaPersonResult personResult = _personService.getPersonById(selIds.get(0));
            if (personResult.getClass() == null) {
                return toXMLDataErrorStream(getLocalizationManager().getMessage(personResult.getMessage()), sid);
            }

            // Merge with an empty record
            SbaQualification qualification = (SbaQualification) merge(new SbaQualification(), params);

            qualification.setPersonId(personResult.getPerson().getPersonId());
            qualification.setCanton(personResult.getPerson().getCanton());
            qualification.setVersion(personResult.getPerson().getVersion());
            qualification.setDeliveryCode(personResult.getPerson().getDeliveryCode());
            qualification.setConfigDeliveryCode(personResult.getPerson().getConfigDeliveryCode());
            qualification.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            qualification.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);

            SbaQualificationResult result = _qualificationService.insertQualification(qualification,
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            QualificationTableResultMapper resultMapper = new QualificationTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);
            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        synchronized (UPDATE_LOCK) {
            String selected = params.getRowId();
            SbaQualificationResult result = _qualificationService.deleteQualification((SbaQualification) getRowData(selected),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            QualificationTableResultMapper resultMapper = new QualificationTableResultMapper(CommandConstants.DELETE, selected, result,
                    getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);
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
            SbaQualificationResult result = _qualificationService.getQualificationById(new Long(sid));

            // Maps result
            QualificationTableResultMapper resultMapper = new QualificationTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            SbaQualificationResult result = new SbaQualificationResult();
            result.setState(ResultBase.OK);
            result.setQualification(new SbaQualification());
            QualificationTableResultMapper resultMapper = new QualificationTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

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

        SbaQualificationResult result = _qualificationService.getQualificationById(new Long(sid));

        // Maps result
        QualificationTableResultMapper resultMapper = new QualificationTableResultMapper(CommandConstants.REFRESH, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    protected void clearPlausiErrorData(List<SbaQualification> qualifications) {
        for (SbaQualification qualification : qualifications) {
            _loadedPlausiErrors.remove(qualification.getQualificationId());
        }
    }

    public void setPlausiErrorData(Long qualificationId, Long plausiErrorId, Boolean isConfirmed) {
        List<PlausiError> plausiErrors = _loadedPlausiErrors.get(qualificationId);
        for (PlausiError plausiError : plausiErrors) {
            if (plausiError.getErrorId().equals(plausiErrorId)) {
                plausiError.setIsConfirmed(isConfirmed);
            }
        }
    }

    public IHttpResult plausierrorData(ParameterList params) throws DhtmlxException {
        String sid = params.getRowId();
        PlausiErrorListResult result = _qualificationService.getPlausiErrorsForQualification(new Long(sid));
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

    protected boolean isReadOnly(SbaQualification sbaQualification) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (sbaQualification.getDeliveryStatus() == null) {
            return !user.isInRole(SecurityConstants.ROLE_SBA_DV);
        }

        switch ((int) (long) sbaQualification.getDeliveryStatus()) {
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
        HashMap<String, String> userData = new HashMap<String, String>();
        SbaQualification sbaQualification = (SbaQualification) row;
        String readOnlyStr;

        // COLUMN_VERSION, COLUMN_CANTON, COLUMN_SCHOOLIDTYPE, COLUMN_SCHOOLID, COLUMN_NAMEBURSCHOOL,
        // IS_PUBLIC_SCHOOL, IS_PRIVATE_SUBSIDISED_SCHOOL, IS_PRIVATE_NOT_SUBSIDISED_SCHOOL, IS_SPECIAL_SCHOOL,
        // COLUMN_EDUCATIONTYPE, COLUMN_EXAMTYPE, COLUMN_EXAMDATE, COLUMN_EXAMNR, COLUMN_RESULT,
        // COLUMN_MATURITY_LANGUAGES, COLUMN_DELIVERYSTATUS, COLUMN_PLAUSISTATUS,
        // COLUMN_CREATIONUSER, COLUMN_CREATIONDATE, COLUMN_MODIFICATIONUSER, COLUMN_MODIFICATIONDATE,
        // COLUMN_PREVELATIONUSER, COLUMN_PREVELATIONDATE, COLUMN_USERTEXT_ID

        if (isReadOnly(sbaQualification)) {
            readOnlyStr = "111111111111111111111111";
        } else {
            readOnlyStr = "110011111000000101111110";
        }

        userData.put("readOnlyCells", readOnlyStr);
        userData.put("plausierror", "empty");
        return userData;
    }

    @Override
    protected String getRowStyleClass(Object row) {
        SbaQualification sbaQualification = (SbaQualification) row;
        if (sbaQualification.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_FINALIZED) {
            return ROW_FINALIZED_STYLE;
        } else if (sbaQualification.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_VALIDATED) {
            return ROW_VALIDATED_STYLE;
        } else if (sbaQualification.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_PREVALIDATED) {
            return ROW_PREVALIDATED_STYLE;
        } else if (sbaQualification.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_IMPORTED) {
            return ROW_DATA_IMPORTED_STYLE;
        } else if (sbaQualification.getPlausiStatus() >= CodegroupUtility.MEB_PLAUSISTATUS_VALID) {
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
