/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb
 */
package ch.bfs.meb.sdl.web.frontend.manager;

import ch.bfs.meb.exception.SessionTimeoutException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.dhtmlx.callback.RefreshLearnerButtonsCallback;
import ch.bfs.meb.sdl.web.dhtmlx.table.AgeColumn;
import ch.bfs.meb.sdl.web.frontend.resultmapper.LearnerListTableResultMapper;
import ch.bfs.meb.sdl.web.frontend.resultmapper.LearnerTableResultMapper;
import ch.bfs.meb.sdl.web.service.ICantonService;
import ch.bfs.meb.sdl.web.service.IClassService;
import ch.bfs.meb.sdl.web.service.ILearnerService;
import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClassResult;
import ch.bfs.meb.sdl.web.ws.sdllearner.*;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
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
import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Class represents a LearnerTableManager for the maintain tab and acts
 * as a controller for the Learner Table.
 */
@Scope("session")
@Component("learnerTableManager")
public class LearnerTableManager extends FilteredTableManagerBase implements PlausiErrorColumn.IPlausiErrorDataUpdate {
    private final String UPDATE_LOCK = "SdlLearnerUpdateLock";

    private static final int BUFFERLEN = 500;
    private static final JSNumber BUFFSIZE = new JSNumber(500);

    public static final String COLUMN_LEARNERID_ID = "learnerId";
    public static final String COLUMN_LEARNERID_NAME_KEY = "learnerTable.column.id.name";
    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_VERSION_NAME_KEY = "learnerTable.column.version.name";
    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "learnerTable.column.canton.name";
    public static final String COLUMN_IDTYPE_ID = "idType";
    public static final String COLUMN_IDTYPE_NAME_KEY = "learnerTable.column.idtype.name";
    public static final String COLUMN_ID_ID = "id";
    public static final String COLUMN_ID_NAME_KEY = "learnerTable.column.id.name";
    public static final String COLUMN_SEX_ID = "sex";
    public static final String COLUMN_SEX_NAME_KEY = "learnerTable.column.sex.name";
    public static final String COLUMN_BIRTHDATE_ID = "birthdate";
    public static final String COLUMN_BIRTHDATE_NAME_KEY = "learnerTable.column.birthdate.name";
    public static final String COLUMN_AGE_ID = "age";
    public static final String COLUMN_AGE_NAME_KEY = "learnerTable.column.age.name";
    public static final String COLUMN_NATIONALITY_ID = "nationality";
    public static final String COLUMN_NATIONALITY_NAME_KEY = "learnerTable.column.nationality.name";
    public static final String COLUMN_LANGUAGE_ID = "language";
    public static final String COLUMN_LANGUAGE_NAME_KEY = "learnerTable.column.language.name";
    public static final String COLUMN_RESIDENCE_ID = "residence";
    public static final String COLUMN_RESIDENCE_NAME_KEY = "learnerTable.column.residence.name";
    public static final String COLUMN_HISTORIC_RESIDENCE_ID = "historicResidence";
    public static final String COLUMN_HISTORIC_RESIDENCE_NAME_KEY = "learnerTable.column.historicResidence.name";
    public static final String COLUMN_COUNTRY_ID = "country";
    public static final String COLUMN_COUNTRY_NAME_KEY = "learnerTable.column.country.name";
    public static final String COLUMN_SCHOOLTYPE_ID = "schoolType";
    public static final String COLUMN_SCHOOLTYPE_NAME_KEY = "learnerTable.column.schoolType.name";
    public static final String COLUMN_CANTONALYEAR_ID = "cantonalYear";
    public static final String COLUMN_CANTONALYEAR_NAME_KEY = "learnerTable.column.cantonalYear.name";
    public static final String COLUMN_EDUCATIONTYPE_ID = "educationType";
    public static final String COLUMN_EDUCATIONTYPE_NAME_KEY = "learnerTable.column.educationType.name";
    public static final String COLUMN_PLANSTATUS_ID = "planStatus";
    public static final String COLUMN_PLANSTATUS_NAME_KEY = "learnerTable.column.planStatus.name";
    public static final String COLUMN_PROFMATURA_ID = "profMatura";
    public static final String COLUMN_PROFMATURA_NAME_KEY = "learnerTable.column.profMatura.name";
    public static final String COLUMN_PREV_SCHOOLTYPE_ID = "prevSchoolType";
    public static final String COLUMN_PREV_SCHOOLTYPE_NAME_KEY = "learnerTable.column.prevSchoolType.name";
    public static final String COLUMN_PREV_CANTONALYEAR_ID = "prevCantonalYear";
    public static final String COLUMN_PREV_CANTONALYEAR_NAME_KEY = "learnerTable.column.prevCantonalYear.name";
    public static final String COLUMN_ADDITION1_ID = "addition1";
    public static final String COLUMN_ADDITION1_NAME_KEY = "learnerTable.column.addition1.name";
    public static final String COLUMN_ADDITION2_ID = "addition2";
    public static final String COLUMN_ADDITION2_NAME_KEY = "learnerTable.column.addition2.name";
    public static final String COLUMN_ADDITION3_ID = "addition3";
    public static final String COLUMN_ADDITION3_NAME_KEY = "learnerTable.column.addition3.name";
    public static final String COLUMN_ADDITION4_ID = "addition4";
    public static final String COLUMN_ADDITION4_NAME_KEY = "learnerTable.column.addition4.name";
    public static final String COLUMN_ADDITION5_ID = "addition5";
    public static final String COLUMN_ADDITION5_NAME_KEY = "learnerTable.column.addition5.name";

    public static final String COLUMN_DELIVERYSTATUS_ID = "deliveryStatus";
    public static final String COLUMN_DELIVERYSTATUS_NAME_KEY = "learnerTable.column.deliveryStatus.name";
    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "learnerTable.column.plausiStatus.name";
    public static final String COLUMN_PLAUSIERROR_ID = "plausierrors";

    public static final String COLUMN_CREATIONUSER_ID = "creationUser";
    public static final String COLUMN_CREATIONUSER_NAME_KEY = "learnerTable.column.creationUser.name";
    public static final String COLUMN_CREATIONDATE_ID = "creationDate";
    public static final String COLUMN_CREATIONDATE_NAME_KEY = "learnerTable.column.creationDate.name";

    public static final String COLUMN_MODIFICATIONUSER_ID = "modificationUser";
    public static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "learnerTable.column.modificationUser.name";
    public static final String COLUMN_MODIFICATIONDATE_ID = "modificationDate";
    public static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "learnerTable.column.modificationDate.name";

    public static final String COLUMN_PREVELATIONUSER_ID = "prevalidationUser";
    public static final String COLUMN_PREVELATIONUSER_NAME_KEY = "learnerTable.column.prevalidationUser.name";
    public static final String COLUMN_PREVELATIONDATE_ID = "prevalidationDate";
    public static final String COLUMN_PREVELATIONDATE_NAME_KEY = "learnerTable.column.prevalidationDate.name";

    public static final String COLUMN_USERTEXT_ID = "userText";
    public static final String COLUMN_USERTEXT_NAME_KEY = "learnerTable.column.userText.name";

    public static final String COLUMN_ORIGDELIVERYDATA_ID = "origDeliveryData";
    public static final String COLUMN_ORIGDELIVERYDATA_NAME_KEY = "learnerTable.column.origDeliveryData.name";

    protected static final String INSERT_LEARNER_UNIQUE_MESSAGE = "insert.learner.unique.message";
    protected static final String LEARNER_NOMASTER_MESSAGE = "learner.nomaster.message";

    public static final String MANAGER_NAME = "learner";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private ICantonService _cantonService;

    @Autowired
    private ILearnerService _learnerService;

    @Autowired
    private IClassService _classService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    private LearnerFilterTableManager _filterTableManager;

    @Autowired
    private LearnerWhereTableManager _whereTableManager;

    private SchoolTableManager _schoolTableManager;

    private ClassTableManager _classTableManager;

    private String _sortCol = COLUMN_PLAUSISTATUS_ID;

    private boolean _ascSort = true;

    private ComboCodeGroupColumn _schoolTypeColumn;
    private ComboCodeGroupColumn _prevSchoolTypeColumn;
    private MunicipalityDynamicComboColumn _residenceColumn;
    private MunicipalityDynamicComboColumn _historicResidenceColumn;

    protected final HashMap<Long, List<PlausiError>> _loadedPlausiErrors = new HashMap<>();

    public LearnerTableManager() {
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
     * Initializes a new LearnerTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    @Override
    public void create() {}

    public void create(SchoolTableManager schoolTableManager, ClassTableManager classTableManager) throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        setFilterVersion(user.getLastFilterVersion());
        if (user.getLastFilterCanton() == null || user.getLastFilterCanton() <= 0) {
            List<Long> cantons = _cantonService.getFilterCantonsForActUser();
            setFilterCanton(cantons.get(0) > 0L ? cantons.get(0) : (cantons.size() > 1 ? cantons.get(1) : null));
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        _schoolTableManager = schoolTableManager;
        _classTableManager = classTableManager;

        DateColumn dateColumn;

        addColumn(new IdentityColumn(COLUMN_LEARNERID_ID, COLUMN_LEARNERID_NAME_KEY, getLocalizationManager()));

        addColumn(new ReadOnlyColumn(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 3));

        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 5);
        cantonColumn.setEditType(EditType.readonly);
        cantonColumn.setColor(COLOR.LIGHTGREY);
        addColumn(cantonColumn);

        addColumn(new Column(COLUMN_IDTYPE_ID, COLUMN_IDTYPE_NAME_KEY, getLocalizationManager(), 4));

        addColumn(new Column(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager(), 5));

        Column deliveryStatusColumn = new StatusColumn(COLUMN_DELIVERYSTATUS_ID, COLUMN_DELIVERYSTATUS_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MEB_DATASTATUS, 3);
        if (!user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            deliveryStatusColumn.setEditType(EditType.readonly);
            deliveryStatusColumn.setColor(COLOR.LIGHTGREY);
        }
        addColumn(deliveryStatusColumn);

        Column plausiStatusColumn = new PlausistatusColumn(COLUMN_PLAUSISTATUS_ID, COLUMN_PLAUSISTATUS_NAME_KEY, getLocalizationManager(), 8);
        addColumn(plausiStatusColumn);
        // hidden column for plausierror data
        addColumn(new PlausiErrorColumn(COLUMN_PLAUSIERROR_ID, getLocalizationManager()));

        ComboCodeGroupColumn sexColumn = new ComboCodeGroupColumn(COLUMN_SEX_ID, COLUMN_SEX_NAME_KEY, getLocalizationManager(), CodegroupUtility.SEX, 3);
        addColumn(sexColumn);

        dateColumn = new DateColumn(COLUMN_BIRTHDATE_ID, COLUMN_BIRTHDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setDefault("");
        addColumn(dateColumn);

        AgeColumn ageColumn = new AgeColumn(COLUMN_AGE_ID, COLUMN_AGE_NAME_KEY, COLUMN_VERSION_ID, COLUMN_BIRTHDATE_ID, getLocalizationManager());
        ageColumn.setWidth(2);
        addColumn(ageColumn);

        ComboCodeGroupColumn nationalityColumn = new ComboCodeGroupColumn(COLUMN_NATIONALITY_ID, COLUMN_NATIONALITY_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.NATIONALITY, false, 7);
        addColumn(nationalityColumn);

        ComboCodeGroupColumn languageColumn = new ComboCodeGroupColumn(COLUMN_LANGUAGE_ID, COLUMN_LANGUAGE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.LANGUAGE, false, 7);
        addColumn(languageColumn);

        _residenceColumn = new MunicipalityDynamicComboColumn(COLUMN_RESIDENCE_ID, COLUMN_RESIDENCE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MUNICIPALITY, getFilterCanton(), 9, getControlName());
        addColumn(_residenceColumn);
        _historicResidenceColumn = new MunicipalityDynamicComboColumn(COLUMN_HISTORIC_RESIDENCE_ID, COLUMN_HISTORIC_RESIDENCE_NAME_KEY,
                getLocalizationManager(), CodegroupUtility.MUNICIPALITY_HIST, getFilterCanton(), 9, getControlName());
        addColumn(_historicResidenceColumn);

        ComboCodeGroupColumn countryColumn = new ComboCodeGroupColumn(COLUMN_COUNTRY_ID, COLUMN_COUNTRY_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.COUNTRY, false, 8, true);
        addColumn(countryColumn);

        _schoolTypeColumn = new ComboCodeGroupColumn(COLUMN_SCHOOLTYPE_ID, COLUMN_SCHOOLTYPE_NAME_KEY, getLocalizationManager(), CodegroupUtility.SCHOOL_TYPE,
                getFilterCanton(), false, 8);
        addColumn(_schoolTypeColumn);

        addColumn(new Column(COLUMN_CANTONALYEAR_ID, COLUMN_CANTONALYEAR_NAME_KEY, getLocalizationManager(), 8));

        ComboCodeGroupColumn educationTypeColumn = new ComboCodeGroupColumn(COLUMN_EDUCATIONTYPE_ID, COLUMN_EDUCATIONTYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.EDUCATION_TYPE, false, 8);
        addColumn(educationTypeColumn);

        ComboCodeGroupColumn planStatusColumn = new ComboCodeGroupColumn(COLUMN_PLANSTATUS_ID, COLUMN_PLANSTATUS_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.TEACH_PLAN_STATUS, false, 8);
        addColumn(planStatusColumn);

        ComboCodeGroupColumn profMaturaColumn = new ComboCodeGroupColumn(COLUMN_PROFMATURA_ID, COLUMN_PROFMATURA_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.PROF_MATURA, false, 8);
        addColumn(profMaturaColumn);

        _prevSchoolTypeColumn = new ComboCodeGroupColumn(COLUMN_PREV_SCHOOLTYPE_ID, COLUMN_PREV_SCHOOLTYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.SCHOOL_TYPE, getFilterCanton(), false, 8);
        addColumn(_prevSchoolTypeColumn);

        addColumn(new Column(COLUMN_PREV_CANTONALYEAR_ID, COLUMN_PREV_CANTONALYEAR_NAME_KEY, getLocalizationManager(), 7));
        addColumn(new Column(COLUMN_ADDITION1_ID, COLUMN_ADDITION1_NAME_KEY, getLocalizationManager(), 7));
        addColumn(new Column(COLUMN_ADDITION2_ID, COLUMN_ADDITION2_NAME_KEY, getLocalizationManager(), 7));
        addColumn(new Column(COLUMN_ADDITION3_ID, COLUMN_ADDITION3_NAME_KEY, getLocalizationManager(), 7));
        addColumn(new Column(COLUMN_ADDITION4_ID, COLUMN_ADDITION4_NAME_KEY, getLocalizationManager(), 7));
        addColumn(new Column(COLUMN_ADDITION5_ID, COLUMN_ADDITION5_NAME_KEY, getLocalizationManager(), 7));

        addColumn(new Column(COLUMN_USERTEXT_ID, COLUMN_USERTEXT_NAME_KEY, getLocalizationManager(), 20));

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

        addColumn(new OrigDeliveryDataColumn(COLUMN_ORIGDELIVERYDATA_ID, COLUMN_ORIGDELIVERYDATA_NAME_KEY, getLocalizationManager(), 11));

        // auto loading
        enableAutoLoading();

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, _classTableManager, _schoolTableManager, false, _maintainglobals, true);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, _classTableManager, _schoolTableManager, false, onRowSelectCallback, 250,
                _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, _classTableManager, _schoolTableManager, false, _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, _classTableManager, _schoolTableManager, false, _maintainglobals);
        IJavaScriptFunction refreshButtonsCallback = new RefreshLearnerButtonsCallback(this, _classTableManager, COLUMN_DELIVERYSTATUS_ID,
                ClassTableManager.COLUMN_DELIVERYSTATUS_ID);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateMaintainCallback(this, null, null, _classTableManager, _schoolTableManager);
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this, _classTableManager, _schoolTableManager, false,
                _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, _classTableManager, _schoolTableManager);

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
        registerCallback(new OnLoadErrorCallback(this, ClassTableManager.MANAGER_NAME, SchoolTableManager.MANAGER_NAME));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertRowCallback(this, _classTableManager, _schoolTableManager, Master.MANAGER_MUST_NOT_BE_MASTER, _maintainglobals,
                LEARNER_NOMASTER_MESSAGE));
        registerCallback(new DeleteRowCallback(this, _classTableManager, _schoolTableManager, _maintainglobals));
        registerCallback(new SaveCallback(this, _classTableManager, _schoolTableManager, false, true, _maintainglobals));
        registerCallback(new FilterCallback(this, _classTableManager, _schoolTableManager, _filterTableManager, _whereTableManager, _maintainglobals, true));
        registerCallback(new SwitchMasterCallback(this, _classTableManager, _schoolTableManager, _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(refreshButtonsCallback);
        registerCallback(new ExportCsvCallback(this, _classTableManager, _schoolTableManager, false, _maintainglobals));
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
     * Gets all rows for learner Table with maximum buffer rows,
     * starting at start row.
     * 
     * @param start
     *            start from row index
     * @param buffer
     *            maximum number of rows
     * @return requested learner rows from start with buffer number of
     *         rows
     */
    public SdlLearnerListResult getRows(int start, int buffer) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        SdlLearnerListResult learners;

        if (getFilterContext() == null || isInInit()) {
            setInInit(false);
            learners = new SdlLearnerListResult();
            learners.setState(ResultBase.OK);
        } else {
            learners = _learnerService.getLearners(start, buffer, sortContext, getFilterContext(), getFilterVersion(), getFilterCanton());
        }

        return learners;
    }

    /**
     * Gets all rows depending on the selected schoolIds.
     * 
     * @param selectedRowIds
     *            list with event ids
     * @return List with all learners with given schoolId
     */
    public SdlLearnerListResult getRows(List<Long> selectedRowIds) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _learnerService.getLearnersOwnedByClasses(selectedRowIds, sortContext);
    }

    /**
     * Get rows using the parameters from the request
     * 
     * @param params
     *            Request parameters
     * @return List with persons
     */
    public SdlLearnerListResult getRows(ParameterList params) {
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
    public SdlLearnerListResult getRows(ParameterList params, int start, int buffer) {
        SdlLearnerListResult rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            if (_classTableManager == null) {
                throw new SessionTimeoutException();
            }

            if (!_classTableManager.getCanton().equals(getFilterCanton())) {
                setFilterCanton(_classTableManager.getCanton());

                _schoolTypeColumn.setCanton(getFilterCanton());
                _prevSchoolTypeColumn.setCanton(getFilterCanton());
                _residenceColumn.setCanton(getFilterCanton());
                _historicResidenceColumn.setCanton(getFilterCanton());
            }

            if (selectedRowIds.size() == 0) {
                rows = new SdlLearnerListResult();
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
    protected List<SdlLearner> getExportRows(ParameterList params) {
        return getRows(params, -1, -1).getLearners();
    }

    @Override
    protected String getExportFileName() {
        return "Learner.csv";
    }

    /**
     * Intializes the learner table with all rows.
     * 
     * @param params
     *            contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException thrown exception
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        Long resultSize;

        setInInit(true);

        SdlLearnerListResult result = getRows(params);
        clearPlausiErrorData(result.getLearners());
        resultSize = result.getMaxNrOfLearners();
        LearnerListTableResultMapper resultMapper = new LearnerListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));

        return toXMLStream(resultMapper, true, true, userdata);
    }

    /**
     * Loads all rows by given parameters.
     * 
     * @param params
     *            contains all parameters
     * @return xml with all selected rows depending on the parent table
     *         selection who is in the param list
     * @throws DhtmlxException thrown exception
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        Long resultSize;
        Long oldCanton = getFilterCanton();

        SdlLearnerListResult result = getRows(params);
        clearPlausiErrorData(result.getLearners());
        resultSize = result.getMaxNrOfLearners();
        LearnerListTableResultMapper resultMapper = new LearnerListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

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
        _prevSchoolTypeColumn.setCanton(getFilterCanton());
        _residenceColumn.setCanton(getFilterCanton());
        _historicResidenceColumn.setCanton(getFilterCanton());

        Long resultSize;

        SdlLearnerListResult result = getRows(rowsloaded, bufferlen);
        clearPlausiErrorData(result.getLearners());
        resultSize = result.getMaxNrOfLearners();
        LearnerListTableResultMapper resultMapper = new LearnerListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        return toXMLStream(resultMapper, rowsloaded == 0, userdata, !MebUtils.areEqual(oldCanton, getFilterCanton()));
    }

    /**
     * Update learner
     * 
     * @param params
     *            contains all parameters
     * @return XML with updated row
     * @throws DhtmlxException thrown exception
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            // merge data record with cache
            SdlLearner learner = (SdlLearner) merge(params);
            PlausiErrorColumn.setPlausiErrorData(learner.getLearnerId(), params, this);

            SdlLearnerResult result = _learnerService.updateLearner(learner, _loadedPlausiErrors.get(learner.getLearnerId()),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            LearnerTableResultMapper resultMapper = new LearnerTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);
            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            List<Long> selIds = params.getSelectedRows();
            if (selIds.size() != 1 || !params.getSelectedMaster().equals(ClassTableManager.MANAGER_NAME)) {
                return toXMLDataErrorStream(getLocalizationManager().getMessage(INSERT_LEARNER_UNIQUE_MESSAGE), sid);
            }
            SdlClassResult classResult = _classService.getClassById(selIds.get(0));
            if (classResult.getClass() == null) {
                return toXMLDataErrorStream(getLocalizationManager().getMessage(classResult.getMessage()), sid);
            }

            // Merge with an empty record
            SdlLearner learner = (SdlLearner) merge(new SdlLearner(), params);

            learner.setClassId(classResult.getSdlClass().getClassId());
            learner.setCanton(classResult.getSdlClass().getCanton());
            learner.setVersion(classResult.getSdlClass().getVersion());
            learner.setDeliveryCode(classResult.getSdlClass().getDeliveryCode());
            learner.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            learner.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);

            SdlLearnerResult result = _learnerService.insertLearner(learner, params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            LearnerTableResultMapper resultMapper = new LearnerTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);
            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        synchronized (UPDATE_LOCK) {
            String selected = params.getRowId();
            SdlLearnerResult result = _learnerService.deleteLearner((SdlLearner) getRowData(selected),
                    params.getParameter(ParameterConstants.PARAM_NO_PLAUSI).equals("1"));

            // Maps result
            LearnerTableResultMapper resultMapper = new LearnerTableResultMapper(CommandConstants.DELETE, selected, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);
            return toXMLDataStream(resultMapper);
        }
    }

    /**
     * Gets all unmodified rows from DB. This method sets the initial values.
     * 
     * @param params ParameterList
     * @return DhtmlxTableDataXML
     * @throws DhtmlxException thrown exception
     */
    public DhtmlxTableDataXML undo(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        if (!params.getEditorStatus().equals(EDIT.INSERT)) {
            SdlLearnerResult result = _learnerService.getLearnerById(new Long(sid));

            // Maps result
            LearnerTableResultMapper resultMapper = new LearnerTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            SdlLearnerResult result = new SdlLearnerResult();
            result.setState(ResultBase.OK);
            result.setLearner(new SdlLearner());
            LearnerTableResultMapper resultMapper = new LearnerTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    /**
     * Reload unmodified data from DB.
     * 
     * @param params ParameterList
     * @return DhtmlxTableDataXML
     * @throws DhtmlxException thrown exception
     */
    public DhtmlxTableDataXML reload(ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        SdlLearnerResult result = _learnerService.getLearnerById(new Long(sid));

        // Maps result
        LearnerTableResultMapper resultMapper = new LearnerTableResultMapper(CommandConstants.REFRESH, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Prevalidate list of learners
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     * @throws DhtmlxException thrown exception
     */
    public DhtmlxTableDataXML validate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedLearnerIds = new ArrayList<>();
        for (Long selRowId : selectedRowIds) {
            selectedLearnerIds.add(((SdlLearner) getRowData(selRowId.toString())).getLearnerId());
        }
        SdlLearnerResult result = _learnerService.validateLearners(selectedLearnerIds, false);

        // Maps result
        LearnerTableResultMapper resultMapper = new LearnerTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);
        return toXMLDataStream(resultMapper);
    }

    /**
     * Undo prevalidation of learners
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     * @throws DhtmlxException thrown exception
     */
    public DhtmlxTableDataXML undoValidate(ParameterList params) throws DhtmlxException {
        List<Long> selectedRowIds = params.getSelectedRows();
        String sid = selectedRowIds.get(0).toString();
        List<Long> selectedLearnerIds = new ArrayList<>();
        for (Long selRowId : selectedRowIds) {
            selectedLearnerIds.add(((SdlLearner) getRowData(selRowId.toString())).getLearnerId());
        }
        SdlLearnerResult result = _learnerService.validateLearners(selectedLearnerIds, true);

        // Maps result
        LearnerTableResultMapper resultMapper = new LearnerTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
        resultMapper.addUserData("command", CommandConstants.RELOAD_CHILDREN);
        return toXMLDataStream(resultMapper);
    }

    /**
     * Returns municipality suggestions as xml.
     * 
     * @param params ParameterList
     * @return IHttpResult
     */
    public IHttpResult municipalityXml(ParameterList params) {
        //		String pos = params.getParameter("pos");
        String mask = params.getParameter("mask");
        String codeGroup = params.getParameter("codeGroup");
        return MunicipalityDynamicComboColumn.createComboXml(mask, getCanton(), codeGroup, getLocalizationManager());
    }

    protected void clearPlausiErrorData(List<SdlLearner> learners) {
        for (SdlLearner learner : learners) {
            _loadedPlausiErrors.remove(learner.getLearnerId());
        }
    }

    public void setPlausiErrorData(Long learnerId, Long plausiErrorId, Boolean isConfirmed) {
        List<PlausiError> plausiErrors = _loadedPlausiErrors.get(learnerId);
        for (PlausiError plausiError : plausiErrors) {
            if (plausiError.getErrorId().equals(plausiErrorId)) {
                plausiError.setIsConfirmed(isConfirmed);
            }
        }
    }

    public IHttpResult plausierrorData(ParameterList params) {
        String sid = params.getRowId();
        PlausiErrorListResult result = _learnerService.getPlausiErrorsForLearner(new Long(sid));
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

    protected boolean isReadOnly(SdlLearner sdlLearner) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (sdlLearner.getDeliveryStatus() == null) {
            return !user.isInRole(SecurityConstants.ROLE_SDL_DV);
        }

        switch ((int) (long) sdlLearner.getDeliveryStatus()) {
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
        SdlLearner sdlLearner = (SdlLearner) row;
        String readOnlyStr;

        // COLUMN_VERSION, COLUMN_CANTON, COLUMN_IDTYPE, COLUMN_ID, COLUMN_DELIVERYSTATUS, COLUMN_PLAUSISTATUS
        // COLUMN_SEX, COLUMN_BIRTHDATE, COLUMN_AGE, COLUMN_NATIONALITY, COLUMN_LANGUAGE, COLUMN_RESIDENCE
        // COLUMN_HISTORIC_RESIDENCE, COLUMN_COUNTRY, COLUMN_SCHOOLTYPE, COLUMN_CANTONALYEAR
        // COLUMN_EDUCATIONTYPE, COLUMN_PLANSTATUS, COLUMN_PROFMATURA, COLUMN_PREV_SCHOOLTYPE
        // COLUMN_PREV_CANTONALYEAR, COLUMN_ADDITION1, COLUMN_ADDITION2, COLUMN_ADDITION3
        // COLUMN_ADDITION4, COLUMN_ADDITION5, COLUMN_USERTEXT, COLUMN_CREATIONUSER,
        // COLUMN_CREATIONDATE, COLUMN_MODIFICATIONUSER, COLUMN_MODIFICATIONDATE
        // COLUMN_PREVELATIONUSER, COLUMN_PREVELATIONDATE, COLUMN_ORIGDELIVERYDATA

        if (isReadOnly(sdlLearner)) {
            readOnlyStr = "1111111111111111111111111111111111";
        } else {
            if (sdlLearner.getDeliveryStatus().equals(CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)) {
                readOnlyStr = "0000000010000000000000000001111111";
            } else {
                readOnlyStr = "0000100010000000000000000001111111";
            }
        }

        userData.put("readOnlyCells", readOnlyStr);
        userData.put("plausierror", "empty");
        return userData;
    }

    @Override
    protected String getRowStyleClass(Object row) {
        SdlLearner sdlLearner = (SdlLearner) row;
        if (sdlLearner.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_FINALIZED) {
            return ROW_FINALIZED_STYLE;
        } else if (sdlLearner.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_VALIDATED) {
            return ROW_VALIDATED_STYLE;
        } else if (sdlLearner.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_PREVALIDATED) {
            return ROW_PREVALIDATED_STYLE;
        } else if (sdlLearner.getDeliveryStatus() == CodegroupUtility.MEB_DATASTATUS_IMPORTED) {
            return ROW_DATA_IMPORTED_STYLE;
        } else if (sdlLearner.getPlausiStatus() >= CodegroupUtility.MEB_PLAUSISTATUS_VALID) {
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
