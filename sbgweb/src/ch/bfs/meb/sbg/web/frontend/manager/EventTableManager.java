/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: EventTableManager.java 637 2010-11-24 11:59:00Z msc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import java.util.*;

import ognl.OgnlException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.admin.bfs.sbg.dhtmlx.CommandDispatcher.EDIT;
import ch.admin.bfs.sbg.dhtmlx.callback.RefreshEventButtonsCallback;
import ch.admin.bfs.sbg.dhtmlx.table.SbgKeyAspectDynamicComboColumn;
import ch.admin.bfs.sbg.dhtmlx.table.SbgPlausiErrorColumn;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.resultmapper.EventListTableResultMapper;
import ch.bfs.meb.sbg.web.resultmapper.EventTableResultMapper;
import ch.bfs.meb.sbg.web.service.IDeliveryService;
import ch.bfs.meb.sbg.web.service.IEventService;
import ch.bfs.meb.sbg.web.ws.sbgevent.Event;
import ch.bfs.meb.sbg.web.ws.sbgevent.EventList;
import ch.bfs.meb.sbg.web.ws.sbgevent.EventResult;
import ch.bfs.meb.sbg.web.ws.sbgevent.KeyAspect;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
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
import ch.bfs.meb.web.commons.dhtmlx.table.Column.EditType;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Creates a new EventTableManager who acts as an Event Table controller.
 *
 * @author $Author: msc $
 * @version $Revision: 637 $
 */
@Scope("session")
@Component("eventTableManager")
public class EventTableManager extends FilteredTableManagerBase {
    private final String UPDATE_LOCK = "SbgEventUpdateLock";

    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(EventTableManager.class);

    private static final String ROW_FINALIZED_STYLE = "dhx_row_finalized";
    private static final String ROW_VALID_STYLE = "dhx_row_valid";
    private static final String ROW_READY_STYLE = "dhx_row_ready";
    private static final String ROW_NOT_VALID_STYLE = "dhx_row_not_valid";
    private static final String ROW_IMPORTED_STYLE = "dhx_row_imported";

    private static final int BUFFERLEN = 500;
    private static final JSNumber BUFFSIZE = new JSNumber(500);

    public static final String COLUMN_EVTID_ID = "eventid";
    public static final String COLUMN_EVTID_NAME_KEY = "eventTable.column.pid.name";
    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "eventTable.column.canton.name";
    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_VERSION_NAME_KEY = "eventTable.column.version.name";
    public static final String COLUMN_TYPE_ID = "type";
    public static final String COLUMN_TYPE_NAME_KEY = "eventTable.column.type.name";
    public static final String COLUMN_SBFICODE_ID = "sbfiCode";
    public static final String COLUMN_SBFICODE_NAME_KEY = "eventTable.column.sbficode.name";
    public static final String COLUMN_CONTRACTNO_ID = "contractNr";
    public static final String COLUMN_CONTRACTNO_NAME_KEY = "eventTable.column.contractno.name";
    public static final String COLUMN_PROFESSIONCODE_ID = "professionCode";
    public static final String COLUMN_PROFESSIONCODE_NAME_KEY = "eventTable.column.professioncode.name";
    public static final String COLUMN_KEYASPECT_ID = "keyAspect";
    public static final String COLUMN_KEYASPECT_NAME_KEY = "eventTable.column.keyaspect.name";
    public static final String COLUMN_EDUCATIONYEAR_ID = "educationYear";
    public static final String COLUMN_EDUCATIONYEAR_NAME_KEY = "eventTable.column.educationyear.name";
    public static final String COLUMN_CONTRACTTYPE_ID = "contractType";
    public static final String COLUMN_CONTRACTTYPE_NAME_KEY = "eventTable.column.contracttype.name";
    public static final String COLUMN_CONTRACTDATE_ID = "contractDate";
    public static final String COLUMN_CONTRACTDATE_NAME_KEY = "eventTable.column.contractdate.name";
    public static final String COLUMN_EXAMTYPE_ID = "examType";
    public static final String COLUMN_EXAMTYPE_NAME_KEY = "eventTable.column.examtype.name";
    public static final String COLUMN_EXAMNR_ID = "examNr";
    public static final String COLUMN_EXAMNR_NAME_KEY = "eventTable.column.examnr.name";
    public static final String COLUMN_EXAMREP_ID = "examRepetition";
    public static final String COLUMN_EXAMREP_NAME_KEY = "eventTable.column.examrep.name";
    public static final String COLUMN_EXAMRESULT_ID = "examResult";
    public static final String COLUMN_EXAMRESULT_NAME_KEY = "eventTable.column.examresult.name";
    public static final String COLUMN_CANCELREASON_ID = "cancelReason";
    public static final String COLUMN_CANCELREASON_NAME_KEY = "eventTable.column.cancelreason.name";
    public static final String COLUMN_CANCELDATE_ID = "cancelDate";
    public static final String COLUMN_CANCELDATE_NAME_KEY = "eventTable.column.canceldate.name";
    public static final String COLUMN_BURNR_ID = "burnr";
    public static final String COLUMN_BURNR_NAME_KEY = "eventTable.column.burnr.name";

    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "eventTable.column.plausistatus.name";
    public static final String COLUMN_PLAUSIERROR_ID = "plausiErrors";

    public static final String COLUMN_MODUSER_ID = "modUser";
    public static final String COLUMN_MODUSER_NAME_KEY = "eventTable.column.modified.name";
    public static final String COLUMN_MODDATE_ID = "modDate";
    public static final String COLUMN_MODDATE_NAME_KEY = "eventTable.column.modDate.name";

    public static final String COLUMN_USERCOMMENT_ID = "userComment";
    public static final String COLUMN_USERCOMMENT_NAME_KEY = "eventTable.column.usercomment.name";
    public static final String COLUMN_KANTLBCODE_ID = "kantLbCode";
    public static final String COLUMN_KANTLBCODE_NAME_KEY = "eventTable.column.kantlbcode.name";
    public static final String COLUMN_FIRSTNAME_ID = "firmName";
    public static final String COLUMN_FIRSTNAME_NAME_KEY = "eventTable.column.firstname.name";
    public static final String COLUMN_FIRMSTREET_ID = "firmStreet";
    public static final String COLUMN_FIRMSTREET_NAME_KEY = "eventTable.column.firmstreet.name";
    public static final String COLUMN_FIRMSTREETNO_ID = "firmStreetNr";
    public static final String COLUMN_FIRMSTREETNO_NAME_KEY = "eventTable.column.firmstreetno.name";
    public static final String COLUMN_FIRMPLZ_ID = "firmPlz";
    public static final String COLUMN_FIRMPLZ_NAME_KEY = "eventTable.column.firmplz.name";
    public static final String COLUMN_FIRMMUNICIPAL_ID = "firmMunicipality";
    public static final String COLUMN_FIRMMUNICIPAL_NAME_KEY = "eventTable.column.firmmunicipal.name";
    public static final String COLUMN_FLAGLBV_ID = "flagLbv";
    public static final String COLUMN_FLAGLBV_NAME_KEY = "eventTable.checkbox.flaglbv.name";

    private String _sortCol = COLUMN_EVTID_ID;
    private int _sortIndex = 1;
    private boolean _ascSort = true;

    public static final String MANAGER_NAME = "event";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    private IEventService _eventService;

    @Autowired
    private IDeliveryService _deliveryService;

    @Autowired
    private EventFilterTableManager _filterTableManager;

    @Autowired
    private EventWhereTableManager _whereTableManager;

    private PersonTableManager _personTableManager;

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

    @Override
    public boolean isServerSort() {
        return true;
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
            List<Long> cantons = _deliveryService.getFilterCantonsForActUser(_localizationManager);
            setFilterCanton(cantons.get(0) > 0L ? cantons.get(0) : cantons.get(1));
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        _personTableManager = personTableManager;

        addColumn(new IdentityColumn(COLUMN_EVTID_ID, COLUMN_EVTID_NAME_KEY, getLocalizationManager()));
        // canton
        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 6);
        cantonColumn.setEditType(EditType.readonly);
        cantonColumn.setColor(COLOR.LIGHTGREY);
        addColumn(cantonColumn);

        // year column
        Column versionColumn = new ReadOnlyColumn(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 4);
        addColumn(versionColumn);

        // plausiColumn
        Column plausiColumn = new PlausistatusColumn(COLUMN_PLAUSISTATUS_ID, COLUMN_PLAUSISTATUS_NAME_KEY, CodegroupUtility.SBG_PLAUSISTATUS,
                getLocalizationManager(), 8);
        addColumn(plausiColumn);
        // hidden column for plausierror data
        addColumn(new SbgPlausiErrorColumn(COLUMN_PLAUSIERROR_ID, getLocalizationManager()));

        // TYPE
        ComboCodeGroupColumn comboTypeColumn = new ComboCodeGroupColumn(COLUMN_TYPE_ID, COLUMN_TYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.SBG_EVENTTYPE, 8);
        comboTypeColumn.setEditType(EditType.editwheninserted);
        comboTypeColumn.setDefault(CodegroupUtility.SBG_EVENTTYPE_CONTRACT);
        addColumn(comboTypeColumn);

        // contract number
        addColumn(new Column(COLUMN_CONTRACTNO_ID, COLUMN_CONTRACTNO_NAME_KEY, getLocalizationManager(), 6));

        //Profession
        ComboCodeGroupColumn comboProfColumn = new ComboCodeGroupColumn(COLUMN_PROFESSIONCODE_ID, COLUMN_PROFESSIONCODE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.PROFESSIONCODE, false, 7);
        comboProfColumn.setDefault(null);
        addColumn(comboProfColumn);

        // program year (année de programme)
        Column educationYearColumn = new BigDecimalColumn(COLUMN_EDUCATIONYEAR_ID, COLUMN_EDUCATIONYEAR_NAME_KEY, getLocalizationManager(), 4);
        addColumn(educationYearColumn);

        // SEFRI CODE (code Sefri)
        DoubleSearchableCodegroupCombo sbficodeComboColumn = new DoubleSearchableCodegroupCombo(COLUMN_SBFICODE_ID, COLUMN_SBFICODE_NAME_KEY,
                getLocalizationManager(), CodegroupUtility.SBG_SBFICODE, 8, getControlName(), "createSbficodeXml");
        addColumn(sbficodeComboColumn);

        //specific option (option spécifique)
        SbgKeyAspectDynamicComboColumn comboKeyAspect = new SbgKeyAspectDynamicComboColumn(COLUMN_KEYASPECT_ID, COLUMN_KEYASPECT_NAME_KEY,
                getLocalizationManager(), COLUMN_SBFICODE_ID, _eventService, "getSbfiCode()", getControlName());
        addColumn(comboKeyAspect);

        // course type (type de formation)
        ComboCodeGroupColumn comboContractColumn = new ComboCodeGroupColumn(COLUMN_CONTRACTTYPE_ID, COLUMN_CONTRACTTYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CONTRACTTYPE, 7);
        comboContractColumn.setDefault(null);
        comboContractColumn.setCodeFirstFormat(true);
        addColumn(comboContractColumn);

        // starting course (début de formation)
        Column contractDateColumn = new DateColumn(COLUMN_CONTRACTDATE_ID, COLUMN_CONTRACTDATE_NAME_KEY, getLocalizationManager());
        contractDateColumn.setDefault("");
        addColumn(contractDateColumn);

        // resiliation date (date de résiliation)
        Column cancelDateColumn = new DateColumn(COLUMN_CANCELDATE_ID, COLUMN_CANCELDATE_NAME_KEY, getLocalizationManager());
        cancelDateColumn.setDefault("");
        addColumn(cancelDateColumn);

        // resiliation cause (cause de la résiliation)
        ComboCodeGroupColumn comboCancelReasonColumn = new ComboCodeGroupColumn(COLUMN_CANCELREASON_ID, COLUMN_CANCELREASON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANCELREASON, false, 7);
        comboCancelReasonColumn.setDefault(null);
        addColumn(comboCancelReasonColumn);

        // exam type
        ComboCodeGroupColumn comboExamTypeColumn = new ComboCodeGroupColumn(COLUMN_EXAMTYPE_ID, COLUMN_EXAMTYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.EXAMTYPE, false, 7);
        comboExamTypeColumn.setDefault(null);
        addColumn(comboExamTypeColumn);

        //result (résultat)
        ComboCodeGroupColumn comboExamResultColumn = new ComboCodeGroupColumn(COLUMN_EXAMRESULT_ID, COLUMN_EXAMRESULT_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.EXAMRESULT, false, 7);
        comboExamResultColumn.setDefault(null);
        addColumn(comboExamResultColumn);

        // no d'examen
        addColumn(new Column(COLUMN_EXAMNR_ID, COLUMN_EXAMNR_NAME_KEY, getLocalizationManager(), 6));
        // repetition (répétition)
        addColumn(new Column(COLUMN_EXAMREP_ID, COLUMN_EXAMREP_NAME_KEY, getLocalizationManager(), 6));




        // N° REE
        Column burnrColumn = new Column(COLUMN_BURNR_ID, COLUMN_BURNR_NAME_KEY, getLocalizationManager(), 7);
        burnrColumn.setDefault("");
        addColumn(burnrColumn);

        // nom de l'entreprise
        Column firstNameColumn = new Column(COLUMN_FIRSTNAME_ID, COLUMN_FIRSTNAME_NAME_KEY, getLocalizationManager(), 8);
        firstNameColumn.setDefault("");
        addColumn(firstNameColumn);

        // rue
        Column firmStreetColumn = new Column(COLUMN_FIRMSTREET_ID, COLUMN_FIRMSTREET_NAME_KEY, getLocalizationManager(), 8);
        firmStreetColumn.setDefault("");
        addColumn(firmStreetColumn);


        // n° de rue
        Column firmStreetNrColumn = new Column(COLUMN_FIRMSTREETNO_ID, COLUMN_FIRMSTREETNO_NAME_KEY, getLocalizationManager(), 2);
        firmStreetNrColumn.setDefault("");
        addColumn(firmStreetNrColumn);

        //NPA
        Column firmPlzColumn = new Column(COLUMN_FIRMPLZ_ID, COLUMN_FIRMPLZ_NAME_KEY, getLocalizationManager(), 3);
        firmPlzColumn.setDefault("");
        addColumn(firmPlzColumn);

        //localité
        Column municipalColumn = new Column(COLUMN_FIRMMUNICIPAL_ID, COLUMN_FIRMMUNICIPAL_NAME_KEY, getLocalizationManager(), 8);
        municipalColumn.setDefault("");
        addColumn(municipalColumn);


        // code cantonale de l'entreprise
        Column kantLbCodeColumn = new Column(COLUMN_KANTLBCODE_ID, COLUMN_KANTLBCODE_NAME_KEY, getLocalizationManager(), 8);
        kantLbCodeColumn.setDefault("");
        addColumn(kantLbCodeColumn);


        //Comment
        Column commentColumn = new Column(COLUMN_USERCOMMENT_ID, COLUMN_USERCOMMENT_NAME_KEY, getLocalizationManager(), 49);
        commentColumn.setDefault("");
        addColumn(commentColumn);

        //statut de plausibilité
        Column modUserColumn = new ReadOnlyColumn(COLUMN_MODUSER_ID, COLUMN_MODUSER_NAME_KEY, getLocalizationManager(), 8);
        modUserColumn.setDefault("");
        addColumn(modUserColumn);

        //modifié par
        Column modDateColumn = new DateColumn(COLUMN_MODDATE_ID, COLUMN_MODDATE_NAME_KEY, getLocalizationManager());
        modDateColumn.setEditType(EditType.readonly);
        modDateColumn.setColor(COLOR.LIGHTGREY);
        addColumn(modDateColumn);



        //REF
        addColumn(new CheckboxColumn(COLUMN_FLAGLBV_ID, COLUMN_FLAGLBV_NAME_KEY, getLocalizationManager()));

        // auto loading
        enableAutoLoading();

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectCallback(this, _personTableManager, _maintainglobals);
        IJavaScriptFunction onRowSelectDelay = new OnRowSelectDelay(this, _personTableManager, onRowSelectCallback, 250, _maintainglobals);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this, "setSbfiCode");
        IJavaScriptFunction onColumnSortCallback = new OnColumnSortCallback(this, _personTableManager, _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, false, true);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, _personTableManager, _maintainglobals);
        IJavaScriptFunction refreshButtonsCallback = new RefreshEventButtonsCallback(this, _personTableManager);
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
        registerCallback(new InsertRowCallback(this, _personTableManager, _maintainglobals));
        registerCallback(new DeleteRowCallback(this, _personTableManager, _maintainglobals));
        registerCallback(new SaveCallback(this, _personTableManager, _maintainglobals));
        registerCallback(new FilterCallback(this, _personTableManager, _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(new SwitchMasterCallback(this, _personTableManager, _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(new ExportCsvCallback(this, _personTableManager, null, false, _maintainglobals));
        registerCallback(new DuplicateRecordCallback(this, _personTableManager, _maintainglobals));

        registerCallback(refreshButtonsCallback);
        registerCallback(new ReloadSelectedCallback(this));
        registerCallback(new ReloadAllCallback(this));
        registerCallback(onAfterUpdateCallback);
        registerCallback(onGridReconstructedCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(displayNumbersCallback);

        _whereTableManager.create(this);
    }

    @Override
    protected String getRowStyleClass(Object row) {
        Event event = (Event) row;

        boolean isDeliveryFinalized;
        boolean isImported;
        isDeliveryFinalized = _personTableManager.isDeliveryFinalized(event);
        isImported = _personTableManager.isImported(event);

        if (isImported) {
            return ROW_IMPORTED_STYLE;
        } else if (isDeliveryFinalized) {
            return ROW_FINALIZED_STYLE;
        } else if (event.isIsValidated()) {
            return ROW_VALID_STYLE;
        } else if (event.getPlausiStatus() != null && (event.getPlausiStatus() == CodegroupUtility.SBG_PLAUSISTATUS_VALID
                || event.getPlausiStatus() == CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED)) {
            return ROW_READY_STYLE;
        } else {
            return ROW_NOT_VALID_STYLE;
        }
    }

    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<String, String>();
        Event event = (Event) row;
        StringBuffer readOnlyString = new StringBuffer();

        boolean isDeliveryFinalized;
        isDeliveryFinalized = _personTableManager.isDeliveryFinalized(event);
        boolean isImported;
        isImported = _personTableManager.isImported(event);

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL) || (event.isIsValidated() && !user.isInRole(SecurityConstants.ROLE_SBG_EV)) || isDeliveryFinalized
                || isImported) {
            readOnlyString.append("11111111111111111111111111"); // col #1-26 / 26
        } else {
            // BLOCK #1 - col #1-2 / 28:
            // COLUMN_CANTON, COLUMN_VERSION
            readOnlyString.append("11");
            
            // Block #2 - col #3 / 28:
            // COLUMN_PLAUSISTATUS
            readOnlyString.append("0");
            
            // Block #3 - col #4-10 / 28:
            // COLUMN_EVENTTYPE, COLUMN_CONTRACTNO, COLUMN_PROFESSIONCODE, COLUMN_EDUCATIONYEAR
            // COLUMN_SBFICODE, COLUMN_KEYASPECT, COLUMN_CONTRACTTYPE
            readOnlyString.append("0000000");
            
            // Block #4 - col #11 / 28:
            // COLUMN_CONTRACTDATE
            if (event.getType().equals(CodegroupUtility.SBG_EVENTTYPE_CONTRACT)) {
                readOnlyString.append('0');
            } else {
                readOnlyString.append('1');
            }
            
            // Block #5 - col #12-13 / 28:
            // COLUMN_CANCELDATE, COLUMN_CANCELREASON
            if (event.getType().equals(CodegroupUtility.SBG_EVENTTYPE_CANCELLATION)) {
                readOnlyString.append("00");
            } else {
                readOnlyString.append("11");
            }
            
            // Block #6 - col #14-17 / 28:
            // COLUMN_EXAMTYPE, COLUMN_EXAMRESULT, COLUMN_EXAMNR, COLUMN_EXAMREP,
            if (event.getType().equals(CodegroupUtility.SBG_EVENTTYPE_EXAM)) {
                readOnlyString.append("0000");
            } else {
                readOnlyString.append("1111");
            }
            
            // Block #7 - col #18 / 28:
            // COLUMN_BURNR
            if (event.getType().equals(CodegroupUtility.SBG_EVENTTYPE_CONTRACT)) {
                readOnlyString.append('0');
            } else {
                readOnlyString.append('1');
            }
            
            // Block #8 - col #19-24 / 28:
            // COLUMN_FIRSTNAME, COLUMN_FIRMSTREET, COLUMN_FIRMSTREETNO,
            // COLUMN_FIRMPLZ, COLUMN_FIRMMUNICIPAL, COLUMN_KANTLBCODE
            if (event.getType().equals(CodegroupUtility.SBG_EVENTTYPE_CONTRACT)) {
                readOnlyString.append("000000");
            } else {
                readOnlyString.append("111111");
            }
            
            // Block #9 - col #25-27 / 28:
            // COLUMN_USERCOMMENT, COLUMN_MODUSER, COLUMN_MODDATE
            readOnlyString.append("000");
            
            // Block #10 - col #28 / 28:
            // COLUMN_FLAGLBV
            if (event.getType().equals(CodegroupUtility.SBG_EVENTTYPE_CONTRACT)) {
                readOnlyString.append("0");
            } else {
                readOnlyString.append("1");
            }
            
        }
        userData.put("readOnlyCells", new String(readOnlyString));

        if (_personTableManager.isDeliveryFinalized(event)) {
            userData.put("personState", "" + (CodegroupUtility.SBG_PERSONSTATUS_VALIDATED + 1L));
        } else if (event.isIsValidated()) {
            userData.put("personState", "" + CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);
        } else {
            userData.put("personState", "" + CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);
        }
        return userData;
    }

    /**
     * Gets all rows for event Table with maximum buffer rows, starting at start
     * row.
     *
     * @param start  start from row index
     * @param buffer maximum number of rows
     * @return requested Event rows from start with buffer number of rows
     */
    public EventList getRows(int start, int buffer) {
        WebFilterContext filterContext = getFilterContext();
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        EventList events;

        if (filterContext == null || isInInit()) {
            setInInit(false);
            events = new EventList();
            events.setState(ResultBase.OK);
        } else {
            events = _eventService.getEvents(start, buffer, sortContext, filterContext, getFilterVersion(), getFilterCanton());
        }

        return events;
    }

    /**
     * Gets all rows depending on the selected personIds.
     *
     * @return List with all Events
     */
    public EventList getRows(List<Long> selectedRowIds) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _eventService.getEventsOwnedByPersons(selectedRowIds, sortContext);
    }

    /**
     * Get rows using the parameters from the request
     *
     * @param params Request parameters
     * @return List with persons
     */
    public EventList getRows(ParameterList params) {
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
    public EventList getRows(ParameterList params, int start, int buffer) {
        EventList rows;

        if (params.hasParameter(ParameterConstants.PARAM_SELECTED_ROW_IDS)) {
            ArrayList<Long> selectedRowIds = params.getSelectedRows();

            if (selectedRowIds.size() == 0) {
                rows = new EventList();
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
    protected List<Event> getExportRows(ParameterList params) {
        return getRows(params, -1, -1).getEvents();
    }

    @Override
    protected String getExportFileName() {
        return "Events.csv";
    }

    /**
     * Gets rows including the table header
     *
     * @param params request parameters
     * @return xml with the requested rows
     * @throws DhtmlxException Thrown when a mapping error occurs
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        Long resultSize;

        setInInit(true);

        EventList result = getRows(params);
        resultSize = result.getResultSize();
        EventListTableResultMapper resultMapper = new EventListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        return toXMLStream(resultMapper, true, true, userdata);
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

        EventList result = getRows(params);
        resultSize = result.getResultSize();
        EventListTableResultMapper resultMapper = new EventListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        return toXMLStream(resultMapper, params.getRowsLoaded() == 0, false, userdata);
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

        EventResult result = _eventService.getEventById(new Long(sid));

        // Maps result
        EventTableResultMapper resultMapper = new EventTableResultMapper(CommandConstants.REFRESH, sid, result, getLocalizationManager());

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
            EventResult result = _eventService.getEventById(new Long(sid));

            // Maps result
            EventTableResultMapper resultMapper = new EventTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            EventResult result = new EventResult();
            result.setState(1);
            result.setEvent(new Event());
            EventTableResultMapper resultMapper = new EventTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);

        }
    }

    /**
     * Updates all events with given params.
     *
     * @param params contains all parameters
     * @return xml with all rows to update
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            // merge new data record with cache
            Event event = (Event) merge(params);

            EventResult result = _eventService.updateEvent(event, getLocalizationManager().getLanguage());

            // Maps result
            EventTableResultMapper resultMapper = new EventTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);

            return toXMLDataStream(resultMapper);
        }
    }

    /**
     * Deletes the rows who are selected to delete.
     *
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            // Get the source id
            String sid = params.getRowId();

            Event event = (Event) merge(params);

            EventResult result = _eventService.deleteEvent(event);

            deleteFromCache(new Long(event.getEventid()).toString());

            // Maps result
            EventTableResultMapper resultMapper = new EventTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);

            return toXMLDataStream(resultMapper);
        }
    }
    
    /**
     * Duplicate a record from event table.
     *
     * @param params contains all parameters
     * @return xml with duplicated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML duplicate(ParameterList params) throws DhtmlxException, OgnlException {
        return insertOrDuplicate(params, true);
    }
    
    /**
     * Inserts new records from event table.
     *
     * @param params contains all parameters
     * @return xml with new row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        return insertOrDuplicate(params, false);
    }
    
    private DhtmlxTableDataXML insertOrDuplicate(ParameterList params, boolean duplicate) throws DhtmlxException, OgnlException {
        synchronized (UPDATE_LOCK) {
            Long personId = null;
            List<Long> selectedPersons = params.getSelectedRows();
            if (selectedPersons != null && selectedPersons.size() == 1) {
                personId = selectedPersons.get(0);
            }

            // Get the original id
            String sid = params.getRowId();

            // Merge with an empty record
            Event event = (Event) merge(new Event(), params);

            EventResult result;
            if (personId != null) {
                event.setPid(personId);
                if (duplicate) {
                    result = _eventService.duplicateEvent(event, getLocalizationManager().getLanguage());
                    result.getEvent().setPlausiStatus(event.getPlausiStatus());
                }
                else {
                    result = _eventService.insertEvent(event, getLocalizationManager().getLanguage());
                }
            } else {
                result = new EventResult();
                result.setState(ResultMapperBase.FAILURE);
                result.setMessage("insert.event.unique.message");
            }

            // Maps result
            final String command = duplicate ? CommandConstants.DUPLICATE : CommandConstants.INSERT;
            EventTableResultMapper resultMapper = new EventTableResultMapper(command, sid, result, getLocalizationManager());
            resultMapper.addUserData("command", CommandConstants.RELOAD_PARENT);

            return toXMLDataStream(resultMapper);
        }
    }

    /**
     * Sorts the event table ask the service layer for new sortorder.
     *
     * @param params contains all parameters
     * @return xml with new sorted row
     * @throws DhtmlxException
     */
    public DhtmlxTableXML sort(ParameterList params) throws DhtmlxException {

        _sortIndex = getColumnIndex(params.getColIndex());
        Column c = _columns.get(_sortIndex);
        _sortCol = c.getName();
        _ascSort = "asc".equalsIgnoreCase(params.getSortDirection());

        return load(params);
    }

    /**
     * Returns keyAspect suggestions as xml.
     */
    public IHttpResult keyAspectXml(ParameterList params) {
        String mask = params.getParameter("mask");
        Map<Long, String> keyAspects = new TreeMap<Long, String>();
        Long codeGroup = extractCodeGroupInAnExtremeUglyManner(params.getParameter("codeGroup"));
        if (codeGroup != null) {
            for (KeyAspect keyAspect : _eventService.getKeyAspectsForSbfiCode(codeGroup).getKeyAspects()) {
                keyAspects.put(keyAspect.getKeyAspectCode(),
                        getLocalizationManager().getLanguage().toLowerCase().equals("fr") ? keyAspect.getTextFr() : keyAspect.getTextDe());
            }
        }
        return SbgKeyAspectDynamicComboColumn.createComboXml(mask, keyAspects, getLocalizationManager(), CodegroupUtility.SBG_KEYASPECTTYPE);
    }

    private Long extractCodeGroupInAnExtremeUglyManner(String codeGroupParam) {
        if (codeGroupParam == null) {
            return null;
        }
        String value = null;
        int closing = codeGroupParam.lastIndexOf(')');
        int opening = codeGroupParam.lastIndexOf('(');
        if (closing != -1 && opening != -1 && opening < closing) {
            value = codeGroupParam.substring(opening + 1, closing);
        }
        try {
            return new Long(value);
        } catch(NumberFormatException e) {
        }
        return null;
    }

    /**
     * This call is set up via sbficodeComboColumn definition in method #create.
     */
    public IHttpResult createSbficodeXml(ParameterList params) {
        String mask = params.getParameter("mask");
        String codeGroup = params.getParameter("codeGroup");
        return DoubleSearchableCodegroupCombo.createComboXml(mask, codeGroup, getLocalizationManager());
    }

    public String getExtraHtml(String partName) {
        if (partName.equals("sbfiCodeColumn")) {
            return getColumnIndexById(COLUMN_SBFICODE_ID).toString();
        }

        return "";
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

        EventList result = getRows(rowsloaded, bufferlen);
        resultSize = result.getResultSize();
        EventListTableResultMapper resultMapper = new EventListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        return toXMLStream(resultMapper, rowsloaded == 0, userdata, false);
    }

    public JSNumber getBuffSize() {
        return BUFFSIZE;
    }
}
