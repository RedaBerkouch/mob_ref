/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb
 */
package ch.bfs.meb.ssp.web.frontend.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.bfs.meb.web.commons.util.FileUtils;
import ch.bfs.meb.web.commons.util.ValidationUtils;
import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.ssp.web.frontend.resultmapper.CantonListTableResultMapper;
import ch.bfs.meb.ssp.web.frontend.resultmapper.CantonTableResultMapper;
import ch.bfs.meb.ssp.web.service.ICantonService;
import ch.bfs.meb.ssp.web.ws.sspcanton.*;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.*;
import ch.bfs.meb.web.commons.dhtmlx.CommandDispatcher.EDIT;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.COLOR;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.EditType;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.SORT;
import ch.bfs.meb.web.commons.exception.MebDhtmlxFileException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ch.bfs.meb.web.commons.util.FilterContextUtility;
import ch.bfs.meb.web.commons.util.IFilterService;

/**
 * This Class represents a CantonTableManager for the init tab and acts as a
 * controller for the Canton Table.
 */
@Scope("session")
@Component("cantonTableManager")
public class CantonTableManager extends TableManagerBase implements PlausiErrorColumn.IPlausiErrorDataUpdate {
    //	private final static Logger LOGGER = LoggerFactory.getLogger(CantonTableManager.class);

    public static final String PLAUSIREPORT_CONFIRM_MESSAGE = "plausireport.canton.confirm.message";
    public static final String INIT_BUR_NOT_SYNCHRON_MESSAGE = "init.burnotsynchron.message";
    public static final String INIT_ALREADY_DONE_1_MESSAGE = "init.alreadydone1.message";
    public static final String INIT_ALREADY_DONE_2_MESSAGE = "init.alreadydone2.message";

    public static final String COLUMN_CANTONID_ID = "cantonId";
    public static final String COLUMN_CANTONID_NAME_KEY = "cantonTable.column.cantonId.name";

    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "cantonTable.column.canton.name";

    public static final String COLUMN_DELIVERYSTATUS_ID = "deliveryStatus";
    public static final String COLUMN_DELIVERYSTATUS_NAME_KEY = "cantonTable.column.deliveryStatus.name";

    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "cantonTable.column.plausiStatus.name";
    private static final String COLUMN_PLAUSIERROR_ID = "plausierrors";

    public static final String COLUMN_PLAUSIDATE_ID = "plausiDate";
    public static final String COLUMN_PLAUSIDATE_NAME_KEY = "cantonTable.column.plausiDate.name";
    public static final String COLUMN_PLAUSIUSER_ID = "plausiUser";
    public static final String COLUMN_PLAUSIUSER_NAME_KEY = "cantonTable.column.plausiUser.name";

    public static final String COLUMN_CREATIONDATE_ID = "creationDate";
    public static final String COLUMN_CREATIONDATE_NAME_KEY = "cantonTable.column.creationDate.name";
    public static final String COLUMN_CREATIONUSER_ID = "creationUser";
    public static final String COLUMN_CREATIONUSER_NAME_KEY = "cantonTable.column.creationUser.name";

    public static final String COLUMN_MODIFICATIONDATE_ID = "modificationDate";
    public static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "cantonTable.column.modificationDate.name";
    public static final String COLUMN_MODIFICATIONUSER_ID = "modificationUser";
    public static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "cantonTable.column.modificationUser.name";

    public static final String COLUMN_VALIDATIONDATE_ID = "validationDate";
    public static final String COLUMN_VALIDATIONDATE_NAME_KEY = "cantonTable.column.validationDate.name";
    public static final String COLUMN_VALIDATIONUSER_ID = "validationUser";
    public static final String COLUMN_VALIDATIONUSER_NAME_KEY = "cantonTable.column.validationUser.name";

    public static final String COLUMN_FINALISATIONDATE_ID = "finalisationDate";
    public static final String COLUMN_FINALISATIONDATE_NAME_KEY = "cantonTable.column.finalisationDate.name";
    public static final String COLUMN_FINALISATIONUSER_ID = "finalisationUser";
    public static final String COLUMN_FINALISATIONUSER_NAME_KEY = "cantonTable.column.finalisationUser.name";

    public static final String COLUMN_USERTEXT_ID = "userText";
    public static final String COLUMN_USERTEXT_NAME_KEY = "cantonTable.column.userText.name";

    private static final String NO_CANTON_SELECTED_MESSAGE = "no.canton.selected.message";
    private static final String CONFIRM_VALIDATE_DV_MESSAGE = "confirm.validate.message";
    private static final String CONFIRM_VALIDATE_MESSAGE = "confirm.validate.message";
    private static final String CONFIRM_UNDO_VALIDATE_MESSAGE = "confirm.undo.validate.message";
    private static final String CONFIRM_FINALIZE_MESSAGE = "confirm.finalize.message";
    private static final String CONFIRM_UNDO_FINALIZE_MESSAGE = "confirm.undo.finalize.message";

    public static final String MANAGER_NAME = "canton";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    // not autowired!
    private BurSchoolTableManager _burSchoolTableManager;

    @Autowired
    private ICantonService _cantonService;

    @Autowired
    private IFilterService _filterService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    private Long _version;

    private Long _canton;

    private boolean _inInitVersion = false;
    private int _initVersionCount = 0;
    private ComboCodeGroupColumn _cantonColumn;

    protected final HashMap<Long, List<PlausiError>> _loadedPlausiErrors = new HashMap<Long, List<PlausiError>>();

    public CantonTableManager() throws DhtmlxException {
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

    public void setBurSchoolTableManager(BurSchoolTableManager burSchoolTableManager) {
        _burSchoolTableManager = burSchoolTableManager;
    }

    public Long getVersion() {
        return _version;
    }

    /**
     * @see ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager#getLocalizationManager()
     */
    @Override
    public IWebLocalizationManager getLocalizationManager() {
        return _localizationManager;
    }

    private IDhtmlxControl getInterventionTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return CantonInterventionTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return CantonInterventionTableManager.MANAGER_NAME;
            }
        };
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

    /**
     * Initializes a new CantonTableManager. This is a callback interface. This
     * methode is used to initialize a new Manager and should be called only
     * once.
     */
    public void create() throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        _version = FilterContextUtility.getInitVersion(_filterService, CodegroupUtility.SSP_OBJECTTYPE_CONFIGURATION);
        if (user.getLastFilterCanton() == null) {
            _canton = getCantonsForActUser().get(0);
        } else {
            _canton = user.getLastFilterCanton();
        }

        setMaster(true);

        addColumn(new IdentityColumn(COLUMN_CANTONID_ID, COLUMN_CANTONID_NAME_KEY, getLocalizationManager()));

        _cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(), CodegroupUtility.CANTON, 10);
        _cantonColumn.setSort(SORT.INT);
        addColumn(_cantonColumn);

        Column deliveryStatusColumn = new StatusColumn(COLUMN_DELIVERYSTATUS_ID, COLUMN_DELIVERYSTATUS_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MEB_CANTONSTATUS, 6);
        deliveryStatusColumn.setEditType(EditType.readonly);
        deliveryStatusColumn.setColor(COLOR.LIGHTGREY);
        addColumn(deliveryStatusColumn);

        Column plausiColumn = new PlausistatusColumn(COLUMN_PLAUSISTATUS_ID, COLUMN_PLAUSISTATUS_NAME_KEY, getLocalizationManager(), 10);
        addColumn(plausiColumn);
        // hidden column for plausierror data
        addColumn(new PlausiErrorColumn(COLUMN_PLAUSIERROR_ID, getLocalizationManager()));

        DateColumn dateColumn = new DateColumn(COLUMN_PLAUSIDATE_ID, COLUMN_PLAUSIDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        dateColumn.setDefault("");
        addColumn(dateColumn);
        addColumn(new ReadOnlyColumn(COLUMN_PLAUSIUSER_ID, COLUMN_PLAUSIUSER_NAME_KEY, getLocalizationManager(), 12));

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

        dateColumn = new DateColumn(COLUMN_VALIDATIONDATE_ID, COLUMN_VALIDATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        dateColumn.setDefault("");
        addColumn(dateColumn);
        addColumn(new ReadOnlyColumn(COLUMN_VALIDATIONUSER_ID, COLUMN_VALIDATIONUSER_NAME_KEY, getLocalizationManager(), 12));

        dateColumn = new DateColumn(COLUMN_FINALISATIONDATE_ID, COLUMN_FINALISATIONDATE_NAME_KEY, getLocalizationManager());
        dateColumn.setEditType(EditType.readonly);
        dateColumn.setColor(COLOR.LIGHTGREY);
        dateColumn.setDefault("");
        addColumn(dateColumn);
        addColumn(new ReadOnlyColumn(COLUMN_FINALISATIONUSER_ID, COLUMN_FINALISATIONUSER_NAME_KEY, getLocalizationManager(), 12));

        addColumn(new Column(COLUMN_USERTEXT_ID, COLUMN_USERTEXT_NAME_KEY, getLocalizationManager(), 20));

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, false);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateCallback(this, getInterventionTable());
        IJavaScriptFunction refreshButtonsCallback = new RefreshCantonButtonsCallback(this, COLUMN_DELIVERYSTATUS_ID, COLUMN_PLAUSIUSER_ID,
                CodegroupUtility.MEB_APPLICATION_SSP);
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectDeliveryCallback(this, getInterventionTable(), _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getInterventionTable(), getConfigDeliveryTable(), getSchoolTable());

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setOnEditCellHandler(onEditCellCallback)));
        addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));
        addBeforeOption(new Option(table.setOnSelectStateChangedHandler(onRowSelectCallback)));

        // Data processor
        DataProcessor dataProcessor = new DataProcessor(this, onErrorCallback);
        dataProcessor.setAfterUpdateFunction(onAfterUpdateCallback);
        dataProcessor.setRowMarkFunction(onRowMarkCallback);
        setDataProcessor(dataProcessor);

        // Register callbacks
        registerCallback(onEditCellCallback);
        registerCallback(onErrorCallback);
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onAfterUpdateCallback);
        registerCallback(refreshButtonsCallback);
        registerCallback(onRowSelectCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new SynchCommandCallback(this, CallbackConstants.SaveCallback, CommandConstants.SAVE, true));
        registerCallback(new InsertRowCallback(this));
        registerCallback(new DeleteRowCallback(this));
        registerCallback(new ShowLastPlausireportCallback(this));
        registerCallback(new SimpleButtonCallback(this, CallbackConstants.CreatePlausireportCallback, CommandConstants.CREATE_PLAUSIREPORT,
                NO_CANTON_SELECTED_MESSAGE, PLAUSIREPORT_CONFIRM_MESSAGE));
        String validateMessage = user.isInRole(SecurityConstants.ROLE_SSP_EV) ? CONFIRM_VALIDATE_MESSAGE : CONFIRM_VALIDATE_DV_MESSAGE;
        registerCallback(new SimpleButtonCallback(this, CallbackConstants.ValidateCallback, CommandConstants.VALIDATE, NO_CANTON_SELECTED_MESSAGE,
                validateMessage, true));
        registerCallback(new SimpleButtonCallback(this, CallbackConstants.UndoValidateCallback, CommandConstants.UNDO_VALIDATE, NO_CANTON_SELECTED_MESSAGE,
                CONFIRM_UNDO_VALIDATE_MESSAGE, true));
        registerCallback(new SimpleButtonCallback(this, CallbackConstants.FinalizeCallback, CommandConstants.FINALIZE, NO_CANTON_SELECTED_MESSAGE,
                CONFIRM_FINALIZE_MESSAGE, true));
        registerCallback(new SimpleButtonCallback(this, CallbackConstants.UndoFinalizeCallback, CommandConstants.UNDO_FINALIZE, NO_CANTON_SELECTED_MESSAGE,
                CONFIRM_UNDO_FINALIZE_MESSAGE, true));
        registerCallback(new ExportCsvCallback(this, getInterventionTable(), null, false, _maintainglobals));
    }

    /**
     * Get rows using the parameters from the request
     * 
     * @param params
     *            Request parameters
     * @return List with persons
     */
    public CantonListResult getRows(ParameterList params) {
        CantonListResult rows;

        rows = _cantonService.getCantons(_version, _canton);

        return rows;
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<Canton> getExportRows(ParameterList params) {
        return getRows(params).getCantons();
    }

    @Override
    protected String getExportFileName() {
        return "Cantons.csv";
    }

    protected List<Long> getMissingCantons() {
        List<Long> userCantons = _cantonService.getFilterCantonsForActUser();
        CantonListResult existingCantons = _cantonService.getCantons(_version, -1L);
        List<Long> missingCantons = new ArrayList<Long>(userCantons);

        for (Canton existingCanton : existingCantons.getCantons()) {
            missingCantons.remove(existingCanton.getCanton());
        }

        return missingCantons;
    }

    protected List<Long> actualMissingCantons(List<Long> missingCantons) {
        if (_canton >= 0L) {
            List<Long> actMissingCantons = new ArrayList<Long>();
            if (missingCantons.contains(_canton)) {
                actMissingCantons.add(_canton);
            }
            return actMissingCantons;
        } else {
            return missingCantons;
        }
    }

    protected String hasMissingCantons(List<Long> missingCantons) {
        return new Boolean(actualMissingCantons(missingCantons).size() > 0).toString();
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
        CantonListTableResultMapper resultMapper;

        CantonListResult result = getRows(params);
        clearPlausiErrorData(result.getCantons());
        resultMapper = new CantonListTableResultMapper(result, getLocalizationManager());
        List<Long> missingCantons = getMissingCantons();

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        userdata.put("missingCantons", hasMissingCantons(missingCantons));

        _cantonColumn.setCodegroupIdList(actualMissingCantons(missingCantons));

        return toXMLStream(resultMapper, true, true, userdata);
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
        CantonListTableResultMapper resultMapper;

        CantonListResult result = getRows(params);
        clearPlausiErrorData(result.getCantons());
        resultMapper = new CantonListTableResultMapper(result, getLocalizationManager());
        List<Long> missingCantons = getMissingCantons();

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        userdata.put("missingCantons", hasMissingCantons(missingCantons));

        _cantonColumn.setCodegroupIdList(actualMissingCantons(missingCantons));

        return toXMLStream(resultMapper, true, true, userdata);
    }

    public DhtmlxTableXML load(ParameterList params, String message) throws DhtmlxException {
        CantonListTableResultMapper resultMapper;

        CantonListResult result = getRows(params);
        clearPlausiErrorData(result.getCantons());
        result.setMessage(message);
        resultMapper = new CantonListTableResultMapper(result, getLocalizationManager());
        List<Long> missingCantons = getMissingCantons();

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        userdata.put("missingCantons", hasMissingCantons(missingCantons));

        _cantonColumn.setCodegroupIdList(actualMissingCantons(missingCantons));

        return toXMLStream(resultMapper, true, true, userdata);
    }

    public IHttpResult filter(ParameterList params) throws DhtmlxException {
        _version = params.getFilterVersion();
        _canton = params.getFilterCanton();

        if (CallbackConstants.InitVersionCallback.equals(params.getParameter(ParameterConstants.PARAM_FILTERCOMMAND))
                || CallbackConstants.InitVersionNoSyncCallback.equals(params.getParameter(ParameterConstants.PARAM_FILTERCOMMAND))) {
            String message = initVersion(CallbackConstants.InitVersionNoSyncCallback.equals(params.getParameter(ParameterConstants.PARAM_FILTERCOMMAND)));
            if (message != null && !message.equals("")) {
                return load(params, message);
            }
        }

        return load(params);
    }

    /**
     * Validate canton
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML validate(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        CantonResult result = _cantonService.validateCanton((Canton) getRowData(selected), false);

        // Maps result, row has to be updated
        CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Undo validation of canton
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undoValidate(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        CantonResult result = _cantonService.validateCanton((Canton) getRowData(selected), true);

        // Maps result, row has to be updated
        CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Finalize canton
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML finalize(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        CantonResult result = _cantonService.finalizeCanton((Canton) getRowData(selected), false);

        // Maps result, row has to be updated
        CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Undo finalization of canton
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undoFinalize(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        CantonResult result = _cantonService.finalizeCanton((Canton) getRowData(selected), true);

        // Maps result, row has to be updated
        CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Shows last plausi report if available
     * 
     * @param params contains all parameters
     * @return xml with all rows 
     * @throws DhtmlxException
     */
    public FileHttpResult showLastPlausireport(ParameterList params) throws DhtmlxException {
        String filename = "PlausiReport";
        try {
            String selected = params.getRowId();
            FileResult result = _cantonService.getLastPlausireport(Long.valueOf(selected));

            if (result.getState() != ResultBase.OK) {
                return new FileHttpResult(_localizationManager.getMessage(result.getMessage()).getBytes(), "error.txt");
            }

            byte[] returnValue;
            try {
                ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(result.getBinaryFile()));
                // Get the first entry
                ZipEntry entry = in.getNextEntry();
                if (entry == null) {
                    returnValue = result.getBinaryFile();
                } else {
                    // Read data from the ZIP stream
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        baos.write(buf, 0, len);
                    }
                    baos.close();

                    returnValue = baos.toByteArray();
                }
            } catch (IOException e) {
                throw new DhtmlxException(getLocalizationManager().getMessage("unknown.error.message"), e);
            }
        return FileUtils.createFileHttpResult(returnValue,filename);
        } catch (Exception e) {
            throw new MebDhtmlxFileException(e);
        }
    }


    /**
     * Initiating plausis and creating new plausi report
     * 
     * @param params contains all parameters
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML createPlausireport(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();

        CantonResult result = _cantonService.createPlausireport((Canton) getRowData(selected));

        // Maps result, row has to be updated
        CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());
        // initiate download of plausireport
        resultMapper.addUserData("command", CommandConstants.SHOW_LAST_PLAUSIREPORT);

        return toXMLDataStream(resultMapper);
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
        Canton canton = (Canton) merge(params);
        PlausiErrorColumn.setPlausiErrorData(canton.getCantonId(), params, this);

        CantonResult result = _cantonService.updateCanton(canton, _loadedPlausiErrors.get(canton.getCantonId()));

        // Maps result
        CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // Merge with an empty record
        Canton canton = (Canton) merge(new Canton(), params);
        canton.setVersion(_version);

        if (canton.getCanton() == null) {
            return toXMLDataErrorStream(getLocalizationManager().getMessage("configdelivery.cantonempty.message"), sid);
        }

        CantonResult result = _cantonService.insertCanton(canton);

        // Maps result
        CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // merge data record with cache
        Canton canton = (Canton) merge(params);

        CantonResult result = _cantonService.deleteCanton(canton);

        // Maps result
        CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

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
            CantonResult result = _cantonService.getCantonById(new Long(sid));

            // Maps result
            CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            CantonResult result = new CantonResult();
            result.setState(ResultBase.OK);
            result.setCanton(new Canton());
            CantonTableResultMapper resultMapper = new CantonTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    protected void clearPlausiErrorData(List<Canton> cantons) {
        for (Canton canton : cantons) {
            _loadedPlausiErrors.remove(canton.getCantonId());
        }
    }

    public void setPlausiErrorData(Long cantonId, Long plausiErrorId, Boolean isConfirmed) {
        List<PlausiError> plausiErrors = _loadedPlausiErrors.get(cantonId);
        for (PlausiError plausiError : plausiErrors) {
            if (plausiError.getErrorId().equals(plausiErrorId)) {
                plausiError.setIsConfirmed(isConfirmed);
            }
        }
    }

    public IHttpResult plausierrorData(ParameterList params) throws DhtmlxException {
        String sid = params.getRowId();
        PlausiErrorListResult result = _cantonService.getPlausiErrorsForCanton(new Long(sid));
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

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase#getRowUserData(java.lang.Object)
     */
    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<String, String>();
        String readOnlyStr = "11011111111110";

        userData.put("plausierror", "empty");
        userData.put("readOnlyCells", readOnlyStr);
        return userData;
    }

    @Override
    protected String getRowStyleClass(Object row) {
        Canton canton = (Canton) row;
        if (canton.getDeliveryStatus() == CodegroupUtility.MEB_CANTONSTATUS_FINALIZED) {
            return ROW_FINALIZED_STYLE;
        } else if (canton.getDeliveryStatus() == CodegroupUtility.MEB_CANTONSTATUS_VALIDATED) {
            return ROW_VALIDATED_STYLE;
        } else if (canton.getDeliveryStatus() == CodegroupUtility.MEB_CANTONSTATUS_DELIVERED
                && canton.getPlausiStatus() >= CodegroupUtility.MEB_PLAUSISTATUS_VALID) {
            return ROW_VALID_STYLE;
        } else {
            return null;
        }
    }

    protected void incrementInit() {
        ++_initVersionCount;
    }

    protected void decrementInit() {
        // wait for at least one incrementInit
        while (_initVersionCount == 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // nothing todo
            }
        }

        --_initVersionCount;
    }

    protected synchronized boolean startNewInit() {
        if (!_inInitVersion) {
            _inInitVersion = true;
            return true;
        } else {
            return false;
        }
    }

    protected void waitForInitDone() {
        while (_inInitVersion) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // nothing todo
            }
        }
    }

    /**
     * if multiple initVersion have been invoked, only the first will be executed,
     * the second waits until first has finished and returns without an action.
     * For each initVersion, a waitForVersionInitialized will be executed from
     * the master table manager.
     */
    protected String initVersion(boolean noSync) {
        if (startNewInit()) {
            try {
                CantonListResult initResult = _cantonService.initVersion(_version, _canton, noSync);
                if (initResult.getState() == ResultBase.OK) {
                    if (!noSync) {
                        _burSchoolTableManager.setInSynchBur();
                    }
                } else {
                    if (!noSync && (INIT_BUR_NOT_SYNCHRON_MESSAGE.equals(initResult.getMessage()) || INIT_ALREADY_DONE_1_MESSAGE.equals(initResult.getMessage())
                            || INIT_ALREADY_DONE_2_MESSAGE.equals(initResult.getMessage()))) {
                        _burSchoolTableManager.setInSynchBur();
                    }
                }
                return initResult.getMessage();
            } catch (RuntimeException e) {
                throw e;
            } finally {
                incrementInit();
                _inInitVersion = false;
            }
        } else {
            waitForInitDone();
            incrementInit();
            return "";
        }
    }

    /**
     * after initVersion has finished, the init version counter is > 0.
     * the master table manager then can show its data (and will decrement the
     * init version counter)
     */
    public void waitForVersionInitialized() {
        decrementInit();
    }
}
