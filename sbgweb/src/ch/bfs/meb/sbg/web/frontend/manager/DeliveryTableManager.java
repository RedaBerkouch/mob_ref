/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: DeliveryTableManager.java 635 2010-11-23 10:42:39Z msc $
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

import ch.admin.bfs.sbg.dhtmlx.callback.RefreshDeliveryButtonsCallback;
import ch.admin.bfs.sbg.dhtmlx.table.SbgPlausiErrorColumn;
import ch.bfs.meb.sbg.web.resultmapper.DeliveryListTableResultMapper;
import ch.bfs.meb.sbg.web.resultmapper.DeliveryTableResultMapper;
import ch.bfs.meb.sbg.web.service.IDeliveryService;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.DeliveryResult;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.PlausireportResult;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDelivery;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDeliveryListResult;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.commons.dhtmlx.*;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.COLOR;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.EditType;
import ch.bfs.meb.web.commons.exception.MebDhtmlxFileException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ch.bfs.meb.web.commons.util.IFilterService;

/**
 * Creates a new DeliveryTableManager who acts as a Delivery Table controller.
 *
 * @author $Author: msc $
 * @version $Revision: 635 $
 */
@Scope("session")
@Component("deliveryTableManager")
public class DeliveryTableManager extends FilteredTableManagerBase {
    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "deliveryTable.column.canton.name";
    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_VERSION_NAME_KEY = "deliveryTable.column.version.name";
    public static final String COLUMN_PLAUSISTATUS_ID = "plausistatus";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "deliveryTable.column.plausistatus.name";
    public static final String COLUMN_STATUS_ID = "status";
    public static final String COLUMN_STATUS_NAME_KEY = "deliveryTable.column.status.name";
    public static final String COLUMN_CREATING_REPORT_ID = "creatingReport";
    public static final String COLUMN_DELIVERYDATE_ID = "deliverydate";
    public static final String COLUMN_DELIVERYDATE_NAME_KEY = "deliveryTable.column.deliverydate.name";
    public static final String COLUMN_DELIVERYUSER_ID = "deliveryuser";
    public static final String COLUMN_DELIVERYUSER_NAME_KEY = "deliveryTable.column.deliveryuser.name";
    public static final String MANAGER_NAME = "delivery";
    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;
    private static final Log LOGGER = LogFactory.getLog(DeliveryTableManager.class);
    private static final int POLLING_INTERVAL = 30000; // milliseconds
    private static final String ROW_VALID_STYLE = "dhx_row_valid";
    private static final String ROW_READY_STYLE = "dhx_row_ready";
    private static final String ROW_NOT_VALID_STYLE = "dhx_row_not_valid";
    private static final String ROW_FINALIZED_STYLE = "dhx_row_finalized";
    private static final String ROW_INITIALIZED_STYLE = "dhx_row_initialized";
    private static final String ROW_AMENDREPLACECONFIRMATION_STYLE = "dhx_row_arc";
    private static final String ROW_IMPORTED_STYLE = "dhx_row_imported";
    private static final String COLUMN_DELIVERYID_ID = "deliveryid";
    public static final String COLUMN_DELIVERYID_NAME_KEY = "deliveryTable.column.deliveryid.name";
    private static final String COLUMN_PLAUSIERROR_ID = "plausiErrors";
    private static final String COLUMN_PLAUSIPERSON_ID = "nrplausiperson";
    public static final String COLUMN_PLAUSIPERSON_NAME_KEY = "deliveryTable.column.plausiperson.name";
    private static final String COLUMN_PLAUSIEVENT_ID = "nrplausievent";
    private static final String COLUMN_PLAUSIEVENT_NAME_KEY = "deliveryTable.column.plausievent.name";
    private static final String COLUMN_NOTVALID_ID = "notvalid";
    private static final String COLUMN_NOTVALID_NAME_KEY = "deliveryTable.column.notvalid.name";
    private static final String UPLOAD_DELIVERY_WITH_ERRORS_MESSAGE = "upload.deliveryWithErrors.message";

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IDeliveryService _deliveryService;

    @Autowired
    private IFilterService _filterService;

    @Autowired
    private DeliveryFilterTableManager _filterTableManager;

    @Autowired
    private DeliveryWhereTableManager _whereTableManager;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    private IDhtmlxControl getActionTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return ActionTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return ActionTableManager.MANAGER_NAME;
            }
        };
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

    protected Long getActVersion() {
        try {
            WebFilterListResult filters = _filterService.getFiltersForRefObject(CodegroupUtility.SBG_OBJECTTYPE_CONFIGURATION);
            if (filters.getFilters().size() > 0) {
                return Long.parseLong(filters.getFilters().get(0).getSource());
            }
        } catch (Exception e) {
            // nothing to do
        }

        return new Long(new GregorianCalendar().get(Calendar.YEAR));
    }

    /**
     * Initializes a new DeliveryTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    public void create() throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user.getLastFilterVersion() == null) {
            setFilterVersion(getActVersion());
            user.setLastFilterVersion(getFilterVersion());
        } else {
            setFilterVersion(user.getLastFilterVersion());
        }

        if (user.getLastFilterCanton() == null) {
            setFilterCanton(_deliveryService.getFilterCantonsForActUser(_localizationManager).get(0));
            user.setLastFilterCanton(getFilterCanton());
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        addColumn(new IdentityColumn(COLUMN_DELIVERYID_ID, COLUMN_DELIVERYID_NAME_KEY, getLocalizationManager()));

        CodeColumn cantonColumn = new CodeColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(), CodegroupUtility.CANTON, 10);
        addColumn(cantonColumn);

        addColumn(new ReadOnlyColumn(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 5));

        Column plausiColumn = new PlausistatusColumn(COLUMN_PLAUSISTATUS_ID, COLUMN_PLAUSISTATUS_NAME_KEY, CodegroupUtility.SBG_PLAUSISTATUS,
                getLocalizationManager(), 10);
        addColumn(plausiColumn);
        // hidden column for plausierror data
        addColumn(new SbgPlausiErrorColumn(COLUMN_PLAUSIERROR_ID, getLocalizationManager()));

        // hidden column for plausireport creation
        addColumn(new HiddenColumn(COLUMN_CREATING_REPORT_ID));

        Column statusColumn = new StatusColumn(COLUMN_STATUS_ID, COLUMN_STATUS_NAME_KEY, getLocalizationManager(), CodegroupUtility.SBG_DELIVERYSTATUS, 12);
        statusColumn.setEditType(EditType.readonly);
        statusColumn.setColor(COLOR.LIGHTGREY);
        addColumn(statusColumn);

        DateColumn deliveryDateColumn = new DateColumn(COLUMN_DELIVERYDATE_ID, COLUMN_DELIVERYDATE_NAME_KEY, getLocalizationManager());
        deliveryDateColumn.setEditType(EditType.readonly);
        deliveryDateColumn.setColor(COLOR.LIGHTGREY);
        addColumn(deliveryDateColumn);

        addColumn(new ReadOnlyColumn(COLUMN_DELIVERYUSER_ID, COLUMN_DELIVERYUSER_NAME_KEY, getLocalizationManager(), 20));

        addColumn(new ReadOnlyColumn(COLUMN_PLAUSIPERSON_ID, COLUMN_PLAUSIPERSON_NAME_KEY, getLocalizationManager(), 12));
        addColumn(new ReadOnlyColumn(COLUMN_PLAUSIEVENT_ID, COLUMN_PLAUSIEVENT_NAME_KEY, getLocalizationManager(), 12));
        addColumn(new ReadOnlyColumn(COLUMN_NOTVALID_ID, COLUMN_NOTVALID_NAME_KEY, getLocalizationManager(), 12));

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onUpdateCallback = new OnUpdateCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, false, true, true);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);

        IJavaScriptFunction refreshButtonsCallback = new RefreshDeliveryButtonsCallback(this, COLUMN_STATUS_ID);
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectDeliveryCallback(this, getActionTable(), _maintainglobals);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateCallback(this, getActionTable());
        IJavaScriptFunction refreshStatusCallback = new RefreshStatusCallback(this, COLUMN_STATUS_ID, COLUMN_CREATING_REPORT_ID, POLLING_INTERVAL,
                CodegroupUtility.SBG_DELIVERYSTATUS_IMPORTED);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getActionTable());

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setMultiselect(JSBoolean.isfalse)));
        addBeforeOption(new Option(table.setOnEditCellHandler(onEditCellCallback)));
        addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));
        addBeforeOption(new Option(table.setOnSelectStateChangedHandler(onRowSelectCallback)));

        // install load error handler
        enableLoadErrorHandling();

        // Data processor
        DataProcessor dataProcessor = new DataProcessor(this, onErrorCallback);
        dataProcessor.addCallbackFunction("update", onUpdateCallback);
        dataProcessor.addCallbackFunction("refresh", onUpdateCallback);
        dataProcessor.setAfterUpdateFunction(onAfterUpdateCallback);
        dataProcessor.setRowMarkFunction(onRowMarkCallback);
        setDataProcessor(dataProcessor);

        // Register callbacks
        registerCallback(onEditCellCallback);

        registerCallback(onErrorCallback);
        registerCallback(new OnLoadErrorCallback(this, ActionTableManager.MANAGER_NAME, null));
        registerCallback(onUpdateCallback);
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.AmendCallback, CommandConstants.AMEND, true));
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.ReplaceCallback, CommandConstants.REPLACE, true));
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.ConfirmCallback, CommandConstants.CONFIRM, true));
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.CancelCallback, CommandConstants.CANCEL));
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.ValidateCallback, CommandConstants.VALIDATE));
        registerCallback(new UndoValidateSbgDeliveryCallback(this, COLUMN_STATUS_ID));
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.FinalizeCallback, CommandConstants.FINALIZE));
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.UndoFinalizeCallback, CommandConstants.UNDO_FINALIZE));
        registerCallback(new ShowLastPlausireportCallback(this));
        registerCallback(new CreatePlausireportCallback(this));
        registerCallback(new SynchCommandCallback(this, CallbackConstants.SaveCallback, CommandConstants.SAVE));
        registerCallback(new DeleteDeliveryCallback(this, COLUMN_STATUS_ID, CodegroupUtility.MEB_APPLICATION_SBG));
        registerCallback(new ExportCsvCallback(this, getActionTable(), null, false, _maintainglobals));

        registerCallback(refreshButtonsCallback);
        registerCallback(onRowSelectCallback);
        registerCallback(onAfterUpdateCallback);
        registerCallback(refreshStatusCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(new FilterCallback(this, null, _filterTableManager, _whereTableManager, _maintainglobals));

        _whereTableManager.create(this);
    }

    @Override
    protected String getRowStyleClass(Object row) {
        SbgDelivery delivery = (SbgDelivery) row;
        if (delivery.getStatus() == null) {
            return ROW_NOT_VALID_STYLE;
        } else if (delivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED) {
            return ROW_FINALIZED_STYLE;
        } else if (delivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED) {
            return ROW_VALID_STYLE;
        } else if (delivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED) {
            if (delivery.getPlausistatus() >= CodegroupUtility.SBG_PLAUSISTATUS_VALID) {
                return ROW_READY_STYLE;
            } else {
                return ROW_NOT_VALID_STYLE;
            }
        } else if (delivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_IMPORTED) {
            return ROW_IMPORTED_STYLE;
        } else if (delivery.getStatus() == CodegroupUtility.SBG_DELIVERYSTATUS_EMPTY) {
            return ROW_INITIALIZED_STYLE;
        } else {
            return ROW_AMENDREPLACECONFIRMATION_STYLE;
        }
    }

    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<String, String>();
        SbgDelivery delivery = (SbgDelivery) row;
        StringBuffer readOnlyString = new StringBuffer();
        // COLUMN_CANTON, COLUMN_VERSION, COLUMN_PLAUSISTATUS, COLUMN_STATUS,
        // COLUMN_DELIVERYDATE,
        // COLUMN_DELIVERYUSER, COLUMN_PLAUSIPERSON, COLUMN_PLAUSIEVENT,
        // COLUMN_NOTVALID
        if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED)) {
            readOnlyString.append("111111111");
        } else {
            readOnlyString.append("110111111");
        }
        userData.put("readOnlyCells", new String(readOnlyString));
        return userData;
    }

    /**
     * Gets all rows
     *
     * @return List with all Deliveries
     */
    public SbgDeliveryListResult getRows() {
        WebFilterContext filterContext = new WebFilterContext();

        WebFilterListResult result = _filterService.getFiltersForRefObject(CodegroupUtility.SBG_OBJECTTYPE_DELIVERY);
        for (WebFilter webFilter : result.getFilters()) {
            if (webFilter.getIsActive() && webFilter.getIsDefault()) {
                filterContext.getFilter().add(webFilter);
            }
        }

        return getRows(filterContext);
    }

    /**
     * Gets filtered rows
     *
     * @return List with deliveries
     */
    public SbgDeliveryListResult getRows(WebFilterContext filterContext) {
        WebFilterContext fullFilterContext = new WebFilterContext(filterContext);

        if (fullFilterContext.getWhereFilter().size() > 0) {
            fullFilterContext.getWhereFilter().get(fullFilterContext.getWhereFilter().size() - 1).setRelation("AND");
        }
        if (getFilterVersion() != null) {
            WebWhereFilter filterVersion = new WebWhereFilter();
            filterVersion.setAttribute(COLUMN_VERSION_ID);
            filterVersion.setOperator("=");
            filterVersion.setRelation("AND");
            filterVersion.setValue(getFilterVersion().toString());
            fullFilterContext.getWhereFilter().add(filterVersion);
        }
        if (getFilterCanton() != null && getFilterCanton() >= 0L) {
            WebWhereFilter filterCanton = new WebWhereFilter();
            filterCanton.setAttribute(COLUMN_CANTON_ID);
            filterCanton.setOperator("=");
            filterCanton.setRelation("AND");
            filterCanton.setValue(getFilterCanton().toString());
            fullFilterContext.getWhereFilter().add(filterCanton);
        }
        return _deliveryService.getFilteredDeliveries(fullFilterContext);
    }

    /**
     * Get all rows for export
     *
     * @return List with all rows
     */
    @Override
    protected List<SbgDelivery> getExportRows(ParameterList params) {
        return getRows(new WebFilterContext()).getDeliveries();
    }

    @Override
    protected String getExportFileName() {
        return "Deliveries.csv";
    }

    /**
     * Intializes the delivery table with all rows.
     *
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        DeliveryListTableResultMapper resultMapper = new DeliveryListTableResultMapper(getRows(), getLocalizationManager());
        return toXMLStream(resultMapper, true, true);
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
        DeliveryListTableResultMapper resultMapper = new DeliveryListTableResultMapper(getRows(), getLocalizationManager());
        return toXMLStream(resultMapper, true, false);
    }

    public IHttpResult filter(ParameterList params) throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //TODO: analog meb
        setFilterContext(new WebFilterContext());
        String filterError = extractFilterParams(params);
        user.setLastFilterVersion(getFilterVersion());
        user.setLastFilterCanton(getFilterCanton());
        if (filterError != null) {
            return toErrorResponse(filterError);
        }

        DeliveryListTableResultMapper resultMapper = new DeliveryListTableResultMapper(getRows(getFilterContext()), getLocalizationManager());
        return toXMLStream(resultMapper, true, false);
    }


    /**
     * Amend delivery
     *
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML amend(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        DeliveryResult result = _deliveryService.amendDelivery((SbgDelivery) getRowData(selected), getLocalizationManager().getLanguage());

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Replace delivery
     *
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML replace(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        DeliveryResult result = _deliveryService.replaceDelivery((SbgDelivery) getRowData(selected), getLocalizationManager().getLanguage());

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Confirm delivery
     *
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML confirm(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        DeliveryResult result = _deliveryService.confirmDelivery((SbgDelivery) getRowData(selected), getLocalizationManager().getLanguage());

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());
        if (result.getMessage() != null && result.getMessage().equals(UPLOAD_DELIVERY_WITH_ERRORS_MESSAGE)) {
            resultMapper.addUserData("command", CommandConstants.SHOW_LAST_PLAUSIREPORT);
        }

        return toXMLDataStream(resultMapper);
    }

    /**
     * Cancel delivery
     *
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML cancel(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        DeliveryResult result = _deliveryService.cancelDelivery((SbgDelivery) getRowData(selected));

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Validate delivery
     *
     * @param params contains all parameters
     * @return xml with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML validate(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        DeliveryResult result = _deliveryService.validateDelivery((SbgDelivery) getRowData(selected));

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * UnValidate delivery
     *
     * @param params contains all parameters
     * @return xml with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undoValidate(ParameterList params) throws DhtmlxException {
       String selected = params.getRowId();
        DeliveryResult result = _deliveryService.unvalidateDelivery((SbgDelivery) getRowData(selected));

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);

    }

    /**
     * Finalize delivery
     *
     * @param params contains all parameters
     * @return xml with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML finalize(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        DeliveryResult result = _deliveryService.finalizeDelivery((SbgDelivery) getRowData(selected), false);

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Undo finalization of delivery
     *
     * @param params contains all parameters
     * @return xml with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undoFinalize(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        DeliveryResult result = _deliveryService.finalizeDelivery((SbgDelivery) getRowData(selected), true);

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

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
        try {
            String selected = params.getRowId();
            PlausireportResult result = _deliveryService.getLastPlausiReport(Long.valueOf(selected), getLocalizationManager().getLanguage().toLowerCase());

            return new FileHttpResult(result.getPlausireport(), "PlausiReport.xlsx");
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
        DeliveryResult result = _deliveryService.createPlausiReport((SbgDelivery) getRowData(selected));

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());
        // initiate download of plausireport

        return toXMLDataStream(resultMapper);
    }

    /**
     * Update delivery
     *
     * @param params contains all parameters
     * @return XML with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // merge data record with cache
        SbgDelivery delivery = (SbgDelivery) merge(params);

        DeliveryResult result = _deliveryService.updateDelivery(delivery);

        // Maps result
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        DeliveryResult result = _deliveryService.deleteDelivery((SbgDelivery) getRowData(selected));

        // Maps result
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Refresh status of delivery
     *
     * @param params contains all parameters
     * @return xml with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML refreshStatus(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        SbgDelivery beforeRefresh = (SbgDelivery) getRowData(selected);
        if (beforeRefresh == null) // session terminated or other error?
        {
            LOGGER.warn("RefreshStatus: Delivery with id " + selected + " not found in TableManager.");
            return TableManagerBase.toXMLDataErrorStream(null, selected);
        }
        DeliveryResult result = _deliveryService.refreshStatus(beforeRefresh);

        DeliveryTableResultMapper resultMapper;
        boolean reportCreated = beforeRefresh.isCreatingReport() && !result.getDelivery().isCreatingReport();
        if (StringUtils.isEmpty(result.getMessage()) && !reportCreated) {
            // Maps result, row has to be refreshed
            resultMapper = new DeliveryTableResultMapper(CommandConstants.REFRESH, selected, result, getLocalizationManager());
        } else {
            // Maps result, update row
            resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());
        }
        if (reportCreated) {
            resultMapper.addUserData("command", CommandConstants.SHOW_LAST_PLAUSIREPORT);
        }

        return toXMLDataStream(resultMapper);
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
