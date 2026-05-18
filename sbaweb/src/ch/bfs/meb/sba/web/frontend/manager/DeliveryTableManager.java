/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: DeliveryTableManager.java 990 2010-03-10 10:54:30Z dzw $
 */
package ch.bfs.meb.sba.web.frontend.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.bfs.meb.web.commons.util.FileUtils;
import ch.bfs.meb.web.commons.util.ValidationUtils;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.web.dhtmlx.callback.RefreshDeliveryButtonsCallback;
import ch.bfs.meb.sba.web.frontend.resultmapper.DeliveryListTableResultMapper;
import ch.bfs.meb.sba.web.frontend.resultmapper.DeliveryTableResultMapper;
import ch.bfs.meb.sba.web.service.ICantonService;
import ch.bfs.meb.sba.web.service.IDeliveryService;
import ch.bfs.meb.sba.web.ws.sbadelivery.*;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.commons.dhtmlx.*;
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
import ch.bfs.meb.web.commons.exception.MebDhtmlxFileException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ch.bfs.meb.web.commons.util.FilterContextUtility;
import ch.bfs.meb.web.commons.util.IFilterService;

/**
 * Creates a new DeliveryTableManager who acts as a Delivery Table controller.
 * 
 * @author $Author: dzw $
 * @version $Revision: 990 $
 */
@Scope("session")
@Component("deliveryTableManager")
public class DeliveryTableManager extends FilteredTableManagerBase implements PlausiErrorColumn.IPlausiErrorDataUpdate {
    private final static Logger LOGGER = LoggerFactory.getLogger(DeliveryTableManager.class);

    private static final int BUFFERLEN = 500;
    private static final JSNumber BUFFSIZE = new JSNumber(500);

    private static final int POLLING_INTERVAL = 30000; // milliseconds

    private static final String COLUMN_DELIVERYID_ID = "deliveryId";
    public static final String COLUMN_DELIVERYID_NAME_KEY = "deliveryTable.column.deliveryid.name";
    public static final String COLUMN_CANTON_ID = "canton";
    public static final String COLUMN_CANTON_NAME_KEY = "deliveryTable.column.canton.name";
    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_VERSION_NAME_KEY = "deliveryTable.column.version.name";
    public static final String COLUMN_CODE_ID = "deliveryCode";
    public static final String COLUMN_CODE_NAME_KEY = "deliveryTable.column.deliveryid.name";

    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "deliveryTable.column.plausistatus.name";
    private static final String COLUMN_PLAUSIERROR_ID = "plausierrors";

    public static final String COLUMN_STATUS_ID = "deliveryStatus";
    public static final String COLUMN_STATUS_NAME_KEY = "deliveryTable.column.status.name";
    public static final String COLUMN_CREATING_REPORT_ID = "creatingReport";

    public static final String COLUMN_DELIVERYDATE_ID = "creationDate";
    public static final String COLUMN_DELIVERYDATE_NAME_KEY = "deliveryTable.column.deliverydate.name";
    public static final String COLUMN_DELIVERYUSER_ID = "creationUser";
    public static final String COLUMN_DELIVERYUSER_NAME_KEY = "deliveryTable.column.deliveryuser.name";
    public static final String COLUMN_PLAUSIPERSON_ID = "nrPlausiPerson";
    public static final String COLUMN_PLAUSIPERSON_NAME_KEY = "deliveryTable.column.plausiperson.name";
    public static final String COLUMN_PLAUSIQUALIFICATION_ID = "nrPlausiQualification";
    public static final String COLUMN_PLAUSIQUALIFICATION_NAME_KEY = "deliveryTable.column.plausiqualification.name";

    public static final String MANAGER_NAME = "delivery";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    private static final String UPLOAD_DELIVERY_WITH_ERRORS_MESSAGE = "upload.deliveryWithErrors.message";

    @Autowired
    private ICantonService _cantonService;

    @Autowired
    private IFilterService _filterService;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    private DeliveryFilterTableManager _filterTableManager;

    @Autowired
    private DeliveryWhereTableManager _whereTableManager;

    @Autowired
    private IDeliveryService _deliveryService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    private String _sortCol = null;

    private boolean _ascSort = false;

    protected final HashMap<Long, List<PlausiError>> _loadedPlausiErrors = new HashMap<Long, List<PlausiError>>();

    private IDhtmlxControl getInterventionTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return InterventionTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return InterventionTableManager.MANAGER_NAME;
            }
        };
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
     * Initializes a new DeliveryTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    @Override
    public void create() throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user.getLastFilterVersion() == null) {
            setFilterVersion(FilterContextUtility.getActVersion(_filterService, CodegroupUtility.SBA_OBJECTTYPE_CONFIGURATION));
            user.setLastFilterVersion(getFilterVersion());
        } else {
            setFilterVersion(user.getLastFilterVersion());
        }

        if (user.getLastFilterCanton() == null) {
            setFilterCanton(_cantonService.getFilterCantonsForActUser().get(0));
            user.setLastFilterCanton(getFilterCanton());
        } else {
            setFilterCanton(user.getLastFilterCanton());
        }

        setMaster(true);

        addColumn(new IdentityColumn(COLUMN_DELIVERYID_ID, COLUMN_DELIVERYID_NAME_KEY, getLocalizationManager()));

        addColumn(new ReadOnlyColumn(COLUMN_CODE_ID, COLUMN_CODE_NAME_KEY, getLocalizationManager(), 10));

        CodeColumn cantonColumn = new CodeColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(), CodegroupUtility.CANTON, 10);
        addColumn(cantonColumn);

        addColumn(new ReadOnlyColumn(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 5));

        Column statusColumn = new StatusColumn(COLUMN_STATUS_ID, COLUMN_STATUS_NAME_KEY, getLocalizationManager(), CodegroupUtility.MEB_DELIVERYSTATUS, 12);
        if (!user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            statusColumn.setEditType(EditType.readonly);
            statusColumn.setColor(COLOR.LIGHTGREY);
        }
        addColumn(statusColumn);

        Column plausiColumn = new PlausistatusColumn(COLUMN_PLAUSISTATUS_ID, COLUMN_PLAUSISTATUS_NAME_KEY, getLocalizationManager(), 10);
        addColumn(plausiColumn);
        // hidden column for plausierror data
        addColumn(new PlausiErrorColumn(COLUMN_PLAUSIERROR_ID, getLocalizationManager()));

        // hidden column for plausireport creation
        addColumn(new HiddenColumn(COLUMN_CREATING_REPORT_ID));

        DateColumn deliveryDateColumn = new DateColumn(COLUMN_DELIVERYDATE_ID, COLUMN_DELIVERYDATE_NAME_KEY, getLocalizationManager());
        deliveryDateColumn.setEditType(EditType.readonly);
        deliveryDateColumn.setColor(COLOR.LIGHTGREY);
        addColumn(deliveryDateColumn);

        addColumn(new ReadOnlyColumn(COLUMN_DELIVERYUSER_ID, COLUMN_DELIVERYUSER_NAME_KEY, getLocalizationManager(), 16));

        addColumn(new ReadOnlyColumn(COLUMN_PLAUSIPERSON_ID, COLUMN_PLAUSIPERSON_NAME_KEY, false, true, getLocalizationManager(), 10));
        addColumn(new ReadOnlyColumn(COLUMN_PLAUSIQUALIFICATION_ID, COLUMN_PLAUSIQUALIFICATION_NAME_KEY, false, true, getLocalizationManager(), 20));

        // auto loading
        enableAutoLoading();

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onUpdateCallback = new OnUpdateCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, true, true, true);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onCellChangedCallback = new ReplaceDataCallback(this, COLUMN_PLAUSIPERSON_ID);
        OnColumnSortCallback onColumnSortCallback = new OnColumnSortCallback(this, getInterventionTable(), _maintainglobals);

        onColumnSortCallback.addIgnoreColumn(getColumnIndexById(COLUMN_PLAUSIPERSON_ID));
        onColumnSortCallback.addIgnoreColumn(getColumnIndexById(COLUMN_PLAUSIQUALIFICATION_ID));

        IJavaScriptFunction displayNumbersCallback = new DisplayNumbersCallback(this);

        IJavaScriptFunction refreshButtonsCallback = new RefreshDeliveryButtonsCallback(this, COLUMN_STATUS_ID, COLUMN_PLAUSISTATUS_ID);
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectDeliveryCallback(this, getInterventionTable(), _maintainglobals);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateCallback(this, getInterventionTable());
        IJavaScriptFunction refreshStatusCallback = new RefreshStatusCallback(this, COLUMN_STATUS_ID, COLUMN_CREATING_REPORT_ID, POLLING_INTERVAL);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getInterventionTable());

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setMultiselect(JSBoolean.isfalse)));
        addBeforeOption(new Option(table.setOnEditCellHandler(onEditCellCallback)));
        addBeforeOption(new Option(table.setOnColumnSort(onColumnSortCallback)));
        addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));
        addBeforeOption(new Option(table.setOnSelectStateChangedHandler(onRowSelectCallback)));
        addBeforeOption(new Option(table.setOnCellChanged(onCellChangedCallback)));

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
        registerCallback(onColumnSortCallback);
        registerCallback(new ShowSortImgCallback(this));

        registerCallback(onErrorCallback);
        registerCallback(new OnLoadErrorCallback(this, InterventionTableManager.MANAGER_NAME, null));
        registerCallback(onUpdateCallback);
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onCellChangedCallback);
        registerCallback(new ShowReplaceDataCallback(this, COLUMN_PLAUSIPERSON_ID, COLUMN_PLAUSIQUALIFICATION_ID));
        //function ShowReplaceDataCallback(loader) {
        //    var dataValues = loader.xmlDoc.responseText.split("#");
        //
        //    if (dataValues[1] == '!*loading*!') {
        //        // 🔄 Les données ne sont pas prêtes, on réessaie dans 1 seconde
        //        window.setTimeout(function() {
        //            dhtmlxAjax.get(
        //                'baseurl?control=myTable&command=REPLACE_DATA&gr_id=' + dataValues[0] + '&t=' + (new Date().getTime()),
        //                myTableShowReplaceDataCallback
        //            );
        //        }, 1000);
        //    } else {
        //        // ✅ Les données sont prêtes, on met à jour les cellules
        //        myTable.cells(dataValues[0], 2).setValue(dataValues[1]); // Colonne data1
        //        myTable.cells(dataValues[0], 5).setValue(dataValues[2]); // Colonne data2 (optionnel)
        //        myTable.cells(dataValues[0], 8).setValue(dataValues[3]); // Colonne data3 (optionnel)
        //    }
        //}
        //new ShowReplaceDataCallback(manager, "colonne1", "colonne2", "colonne3")
        //```
        //
        //- `data1ColumnId` : ID de la colonne obligatoire à mettre à jour
        //- `data2ColumnId` : ID de la colonne optionnelle (peut être `"-"`)
        //- `data3ColumnId` : ID de la colonne optionnelle (peut être `"-"`)
        //
        //### **Format de réponse attendu**
        //
        //**Pendant le chargement** :
        //```
        //123#!*loading*!
        //```
        //
        //**Données finales** :
        //```
        //123#Valeur A#Valeur B#Valeur C
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.AmendCallback, CommandConstants.AMEND, true));
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.ReplaceCallback, CommandConstants.REPLACE, true));
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.ConfirmCallback, CommandConstants.CONFIRM, true));
        registerCallback(new SimpleDeliveryButtonCallback(this, CallbackConstants.CancelCallback, CommandConstants.CANCEL));
        registerCallback(new ValidateDeliveryCallback(this, CodegroupUtility.MEB_APPLICATION_SBA));
        registerCallback(new UndoValidateDeliveryCallback(this, COLUMN_STATUS_ID));
        registerCallback(new ShowLastPlausireportCallback(this));
        registerCallback(new CreatePlausireportCallback(this));
        registerCallback(new SynchCommandCallback(this, CallbackConstants.SaveCallback, CommandConstants.SAVE));
        registerCallback(new DeleteDeliveryCallback(this, COLUMN_STATUS_ID, CodegroupUtility.MEB_APPLICATION_SBA));
        registerCallback(new FilterCallback(this, getInterventionTable(), _filterTableManager, _whereTableManager, _maintainglobals));
        registerCallback(new ExportCsvCallback(this, getInterventionTable(), null, false, _maintainglobals));

        registerCallback(refreshButtonsCallback);
        registerCallback(onRowSelectCallback);
        registerCallback(onAfterUpdateCallback);
        registerCallback(refreshStatusCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(displayNumbersCallback);

        _whereTableManager.create(this);
    }

    //	@Override
    //	public Map<String, String> getRowUserData(Object row) 
    //	{
    //		HashMap<String, String> userData = new HashMap<String, String> ();
    //		Delivery delivery = (Delivery) row;
    //		StringBuffer readOnlyString = new StringBuffer ();
    //		// COLUMN_CANTON, COLUMN_VERSION, COLUMN_PLAUSISTATUS, COLUMN_STATUS, COLUMN_DELIVERYDATE,
    //		// COLUMN_DELIVERYUSER, COLUMN_PLAUSIPERSON, COLUMN_PLAUSIEVENT, COLUMN_NOTVALID
    //		if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED))
    //		{
    //			readOnlyString.append("111111111");
    //		}
    //		else
    //		{
    //			readOnlyString.append("110111111");
    //		}
    //		userData.put("readOnlyCells", new String (readOnlyString));
    //		return userData;
    //	}

    protected Long getDeliveryId(String selected) {
        return ((SbaDelivery) getRowData(selected)).getDeliveryId();
    }

    /**
     * Gets all rows for delivery Table with maximum buffer rows,
     * starting at start row.
     * 
     * @param start
     *            start from row index
     * @param buffer
     *            maximum number of rows
     * @return requested delivery rows from start with buffer number of
     *         rows
     */
    public SbaDeliveryListResult getRows(int start, int buffer) {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(_sortCol);
        sortContext.setAscSortOrder(_ascSort);
        sortContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return _deliveryService.getDeliveries(start, buffer, sortContext, getFilterContext(), getFilterVersion(),
                getFilterCanton());
    }

    /**
     * Get rows using the parameters from the request
     * 
     * @param params
     *            Request parameters
     * @return List with persons
     */
    public SbaDeliveryListResult getRows(ParameterList params) {
        int bufferlen = BUFFERLEN;
        if (params.hasParameter(ParameterConstants.PARAM_COUNT)) {
            bufferlen = params.getCount();
        }
        return getRows(params.getRowsLoaded(), bufferlen);
    }

    /**
     * Get all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<SbaDelivery> getExportRows(ParameterList params) {
        return getRows(-1, -1).getDeliveries();
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
        if (params.getRowsLoaded() == 0) {
            Long resultSize;

            SbaDeliveryListResult result = getRows(params);
            clearPlausiErrorData(result.getDeliveries());
            resultSize = result.getMaxNrOfDeliveries();
            DeliveryListTableResultMapper resultMapper = new DeliveryListTableResultMapper(result, getLocalizationManager(), resultSize,
                    params.getRowsLoaded());

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
     *         selection who is in the param list
     * @throws DhtmlxException
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        Long resultSize;

        SbaDeliveryListResult result = getRows(params);
        clearPlausiErrorData(result.getDeliveries());
        resultSize = result.getMaxNrOfDeliveries();
        DeliveryListTableResultMapper resultMapper = new DeliveryListTableResultMapper(result, getLocalizationManager(), resultSize, params.getRowsLoaded());

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
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        int rowsloaded = params.getRowsLoaded();
        int bufferlen = BUFFERLEN;
        if (params.hasParameter(ParameterConstants.PARAM_COUNT)) {
            bufferlen = params.getCount();
        }

        String filterError = extractFilterParams(params);
        user.setLastFilterVersion(getFilterVersion());
        user.setLastFilterCanton(getFilterCanton());
        if (filterError != null) {
            return toErrorResponse(filterError);
        }

        Long resultSize;

        SbaDeliveryListResult result = getRows(rowsloaded, bufferlen);
        clearPlausiErrorData(result.getDeliveries());
        resultSize = result.getMaxNrOfDeliveries();
        DeliveryListTableResultMapper resultMapper = new DeliveryListTableResultMapper(result, getLocalizationManager(), resultSize, rowsloaded);

        HashMap<String, String> userdata = new HashMap<String, String>();
        userdata.put("resultsize", resultSize == null ? "0" : resultSize.toString());
        userdata.put("plausierrorUrl", "controller.do?control=" + getControlName() + "&command=" + CommandConstants.PLAUSIERROR_DATA);
        userdata.put("plausierror_header", PlausiErrorColumn.getHeaderUserData(getLocalizationManager()));
        return toXMLStream(resultMapper, rowsloaded == 0, false, userdata);
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
        SbaDeliveryResult result = _deliveryService.amendDelivery(getDeliveryId(selected));

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
        SbaDeliveryResult result = _deliveryService.replaceDelivery(getDeliveryId(selected));

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
        SbaDeliveryResult result = _deliveryService.confirmDelivery(getDeliveryId(selected));

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
        SbaDeliveryResult result = _deliveryService.cancelDelivery(getDeliveryId(selected));

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
        SbaDeliveryResult result = _deliveryService.validateDelivery(getDeliveryId(selected), false);

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Undo validation of delivery
     * 
     * @param params contains all parameters
     * @return xml with updated row 
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undoValidate(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        SbaDeliveryResult result = _deliveryService.validateDelivery(getDeliveryId(selected), true);

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
        String filename = "PlausiReport";
        try {
            String selected = params.getRowId();
            FileResult result = _deliveryService.getLastPlausireport(Long.valueOf(selected));

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

        SbaDeliveryResult result = _deliveryService.createPlausireport(getDeliveryId(selected));

        // Maps result, row has to be updated
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    /**
     * Update delivery
     * 
     * @param params 	contains all parameters
     * @return			XML with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();
        // merge data record with cache
        SbaDelivery delivery = (SbaDelivery) merge(params);
        PlausiErrorColumn.setPlausiErrorData(delivery.getDeliveryId(), params, this);

        SbaDeliveryResult result = _deliveryService.updateDelivery(delivery, _loadedPlausiErrors.get(delivery.getDeliveryId()));

        // Maps result
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        SbaDeliveryResult result = _deliveryService.deleteDelivery(getDeliveryId(selected));
        String action = result.getDelivery() == null && StringUtils.isEmpty(result.getMessage()) ? CommandConstants.DELETE : CommandConstants.UPDATE;
        // Maps result
        DeliveryTableResultMapper resultMapper = new DeliveryTableResultMapper(action, selected, result, getLocalizationManager());

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
        SbaDelivery beforeRefresh = (SbaDelivery) getRowData(selected);
        if (beforeRefresh == null) // session terminated or other error?
        {
            LOGGER.warn("RefreshStatus: Delivery with id " + selected + " not found in TableManager.");
            return TableManagerBase.toXMLDataErrorStream(null, selected);
        }
        SbaDeliveryResult result = _deliveryService.refreshStatus(beforeRefresh);

        DeliveryTableResultMapper resultMapper;
        boolean reportCreated = beforeRefresh.isCreatingReport() && !result.getDelivery().isCreatingReport();
        if (StringUtils.isEmpty(result.getMessage()) && !reportCreated) {
            // Maps result, row has to be refreshed
            resultMapper = new DeliveryTableResultMapper(CommandConstants.REFRESH, selected, result, getLocalizationManager());
        } else if (CodegroupUtility.REMOVE_DELIVERY_COMMAND.equals(result.getMessage())) {
            result.setMessage(null);
            // Maps result, delete row
            resultMapper = new DeliveryTableResultMapper(CommandConstants.DELETE, selected, result, getLocalizationManager());
        } else {
            // Maps result, update row
            resultMapper = new DeliveryTableResultMapper(CommandConstants.UPDATE, selected, result, getLocalizationManager());
        }
        if (reportCreated) {
            resultMapper.addUserData("command", CommandConstants.SHOW_LAST_PLAUSIREPORT);
        }

        return toXMLDataStream(resultMapper);
    }

    protected void clearPlausiErrorData(List<SbaDelivery> deliveries) {
        for (SbaDelivery delivery : deliveries) {
            _loadedPlausiErrors.remove(delivery.getDeliveryId());
        }
    }

    public void setPlausiErrorData(Long deliveryId, Long plausiErrorId, Boolean isConfirmed) {
        List<PlausiError> plausiErrors = _loadedPlausiErrors.get(deliveryId);
        for (PlausiError plausiError : plausiErrors) {
            if (plausiError.getErrorId().equals(plausiErrorId)) {
                plausiError.setIsConfirmed(isConfirmed);
            }
        }
    }

    public IHttpResult plausierrorData(ParameterList params) throws DhtmlxException {
        String sid = params.getRowId();
        PlausiErrorListResult result = _deliveryService.getPlausiErrorsForDelivery(new Long(sid));
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

    public class ReplaceDeliveryData implements Runnable {
        final SecurityContext _securityContext;
        final SbaDelivery _delivery;

        public ReplaceDeliveryData(SbaDelivery delivery, SecurityContext securityContext) {
            _delivery = delivery;
            _securityContext = securityContext;
        }

        public void run() {
            SecurityContext ctx = new SecurityContextImpl();
            ctx.setAuthentication(_securityContext.getAuthentication());
            SecurityContextHolder.setContext(ctx);

            try {
                SbaDeliveryResult result = _deliveryService.getDeliveryById(_delivery.getDeliveryId());
                if (result.getState() != ResultBase.OK) {
                    _delivery.setNrPlausiQualification("");
                    _delivery.setNrPlausiPerson(result.getMessage());
                } else {
                    updateCache(_delivery.getDeliveryId().toString(), result.getDelivery());
                }
            } catch (Exception e) {
                _delivery.setNrPlausiQualification("");
                _delivery.setNrPlausiPerson("Error");
                LOGGER.warn("DeliveryTableManager.replaceData: " + e.getMessage());
            }

            SecurityContextHolder.clearContext();
        }
    }

    public synchronized IHttpResult replaceData(ParameterList params) throws DhtmlxException {
        String sid = params.getRowId();
        SbaDelivery delivery = (SbaDelivery) getRowData(sid);
        if (delivery.getNrPlausiPerson().equals(MebUtils.getDeliveryToBeLoadedMessage())) {
            delivery.setNrPlausiPerson(ShowReplaceDataCallback.LOADING_KEY);
            (new Thread(new ReplaceDeliveryData(delivery, SecurityContextHolder.getContext()))).start();
            return toReplaceDataResponse(sid, ShowReplaceDataCallback.LOADING_KEY, "", "");
        } else if (delivery.getNrPlausiPerson().equals(ShowReplaceDataCallback.LOADING_KEY)) {
            return toReplaceDataResponse(sid, ShowReplaceDataCallback.LOADING_KEY, "", "");
        } else {
            return toReplaceDataResponse(sid, _localizationManager.getMessage(delivery.getNrPlausiPerson(), null, delivery.getNrPlausiPerson()),
                    delivery.getNrPlausiQualification(), "");
        }
    }

    private boolean isReadOnly(SbaDelivery delivery) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (delivery.getDeliveryStatus() == null) {
            return !user.isInRole(SecurityConstants.ROLE_SBA_DV);
        }

        switch ((int) (long) delivery.getDeliveryStatus()) {
        case (int) CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED:
            return !user.isInRole(SecurityConstants.ROLE_SBA_EV);
        case (int) CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED:
            return !user.isInRole(SecurityConstants.ROLE_SBA_DV);
        case (int) CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED:
            return !user.isInRole(SecurityConstants.ROLE_SBA_DL);
        default:
            return true;
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase#getRowUserData(java.lang.Object)
     */
    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<String, String>();
        SbaDelivery delivery = (SbaDelivery) row;
        String readOnlyStr;

        // COLUMN_CODE, COLUMN_CANTON, COLUMN_VERSION, COLUMN_STATUS, COLUMN_PLAUSISTATUS
        // COLUMN_DELIVERYDATE, COLUMN_DELIVERYUSER, COLUMN_PLAUSIPERSON, COLUMN_PLAUSIQUALIFICATION

        if (isReadOnly(delivery)) {
            readOnlyStr = "111111111";
        } else {
            if (delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED)
                    || delivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED)) {
                readOnlyStr = "111001111";
            } else {
                readOnlyStr = "111101111";
            }
        }
        userData.put("readOnlyCells", readOnlyStr);
        userData.put("plausierror", "empty");
        return userData;
    }

    @Override
    protected String getRowStyleClass(Object row) {
        SbaDelivery sbaDelivery = (SbaDelivery) row;

        if (sbaDelivery.getDeliveryStatus() == null) {
            return ROW_NOT_VALID_STYLE;
        } else if (sbaDelivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED) {
            return ROW_FINALIZED_STYLE;
        } else if (sbaDelivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED) {
            return ROW_VALIDATED_STYLE;
        } else if (sbaDelivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED) {
            return ROW_PREVALIDATED_STYLE;
        } else if (sbaDelivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED) {
            if (sbaDelivery.getPlausiStatus() >= CodegroupUtility.MEB_PLAUSISTATUS_VALID) {
                return ROW_VALID_STYLE;
            } else {
                return ROW_NOT_VALID_STYLE;
            }
        } else if (sbaDelivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED) {
            return ROW_DELIVERY_IMPORTED_STYLE;
        } else if (sbaDelivery.getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED) {
            return ROW_INITIALIZED_STYLE;
        } else {
            return ROW_AMENDREPLACECONFIRMATION_STYLE;
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
            for (Long canton : _cantonService.getFilterCantonsForActUser()) {
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

    @Override
    public JSNumber getBuffSize() {
        return BUFFSIZE;
    }
}
