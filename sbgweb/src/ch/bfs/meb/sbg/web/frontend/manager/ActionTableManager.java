/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ActionTableManager.java 632 2010-11-22 10:10:15Z msc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import ch.bfs.meb.sbg.web.resultmapper.ActionListTableSingleResultMapper;
import ch.bfs.meb.sbg.web.resultmapper.PersonTableResultMapper;
import ch.bfs.meb.sbg.web.service.IUploadFileService;
import ch.bfs.meb.sbg.web.ws.sbgaction.ActionResult;
import ch.bfs.meb.sbg.web.ws.sbgaction.DeleteAction;
import ch.bfs.meb.sbg.web.ws.sbguploadfile.SbgUploadFile;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.FileDownloadResult;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.admin.bfs.sbg.dhtmlx.callback.RefreshActionButtonsCallback;
import ch.bfs.meb.sbg.web.resultmapper.ActionListTableResultMapper;
import ch.bfs.meb.sbg.web.service.IActionService;
import ch.bfs.meb.sbg.web.ws.sbgaction.Action;
import ch.bfs.meb.sbg.web.ws.sbgaction.ActionList;
import ch.bfs.meb.sbg.web.ws.sbgaction.ExportResult;
import ch.bfs.meb.sbg.web.ws.sbgaction.PlausireportResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.FileHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.COLOR;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.EditType;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.SORT;
import ch.bfs.meb.web.commons.exception.MebDhtmlxFileException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import org.springframework.web.multipart.MultipartFile;

/**
 * Creates a new ActionTableManager who acts as an Action Table controller.
 * 
 * @author $Author: msc $
 * @version $Revision: 632 $
 */
@Scope("session")
@Component("actionTableManager")
public class ActionTableManager extends TableManagerBase {
    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(ActionTableManager.class);

    private static final String COLUMN_ACTIONID_ID = "actionid";
    private static final String COLUMN_ACTIONID_NAME_KEY = "actionTable.column.actionid.name";
    private static final String COLUMN_CANTON_ID = "canton";
    private static final String COLUMN_CANTON_NAME_KEY = "actionTable.column.canton.name";
    private static final String COLUMN_VERSION_ID = "version";
    private static final String COLUMN_VERSION_NAME_KEY = "actionTable.column.version.name";

    private static final String COLUMN_ACTIONUSER_ID = "actionuser";
    private static final String COLUMN_ACTIONUSER_NAME_KEY = "actionTable.column.actionuser.name";
    private static final String COLUMN_EXECUTIONDATE_ID = "executiondate";
    private static final String COLUMN_EXECUTIONDATE_NAME_KEY = "actionTable.column.executiondate.name";

    private static final String COLUMN_TYPE_ID = "type";
    private static final String COLUMN_TYPE_NAME_KEY = "actionTable.column.type.name";

    private static final String COLUMN_PLAUSIREPORT_ID = "plausireportname";
    private static final String COLUMN_PLAUSIREPORT_NAME_KEY = "actionTable.column.plausireport.name";
    private static final String COLUMN_VALIDATION_ID = "validationreport";
    private static final String COLUMN_VALIDATION_NAME_KEY = "actionTable.column.validation.name";

    public static final String MANAGER_NAME = "action";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IActionService _actionService;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    IUploadFileService _uploadFileService;

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

    public IDhtmlxControl getDeliveryTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return DeliveryTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return DeliveryTableManager.MANAGER_NAME;
            }
        };
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<Action> getExportRows(ParameterList params) {
        return getRows(params).getActions();
    }

    @Override
    protected String getExportFileName() {
        return "Actions.csv";
    }

    /**
     * Initializes a new ActionTableManager. This is a callback interface. This
     * methode is used to initialize a new Manager and would called only once.
     */
    public void create() throws DhtmlxException {
        addColumn(new IdentityColumn(COLUMN_ACTIONID_ID, COLUMN_ACTIONID_NAME_KEY, getLocalizationManager()));

        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 8);
        cantonColumn.setEditType(EditType.readonly);
        cantonColumn.setColor(COLOR.LIGHTGREY);
        cantonColumn.setSort(SORT.NO_SORT);
        addColumn(cantonColumn);

        addColumn(new ReadOnlyColumn(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 4));

        addColumn(new ReadOnlyColumn(COLUMN_ACTIONUSER_ID, COLUMN_ACTIONUSER_NAME_KEY, getLocalizationManager(), 10));

        DateTimeColumn execDateColumn = new DateTimeColumn(COLUMN_EXECUTIONDATE_ID, COLUMN_EXECUTIONDATE_NAME_KEY, getLocalizationManager());
        addColumn(execDateColumn);

        Column typeColumn = new StatusColumn(COLUMN_TYPE_ID, COLUMN_TYPE_NAME_KEY, getLocalizationManager(), CodegroupUtility.SBG_ACTIONTYPE, 8);
        typeColumn.setEditType(EditType.readonly);
        typeColumn.setColor(COLOR.LIGHTGREY);
        // typeColumn.setDefault(CodegroupUtility.MEB_ACTINTERVENTIONTYPE_MANUAL);
        typeColumn.setSort(SORT.NO_SORT);
        addColumn(typeColumn);

        String localeSuffix = getLocalizationManager().getLanguage();

        addColumn(new ReadOnlyColumn(COLUMN_PLAUSIREPORT_ID, COLUMN_PLAUSIREPORT_NAME_KEY, getLocalizationManager(), 12));
        addColumn(new ReadOnlyColumn(COLUMN_VALIDATION_ID + localeSuffix, COLUMN_VALIDATION_NAME_KEY, getLocalizationManager(), 49));

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, false);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onRowSelectCallback = new RefreshActionButtonsCallback(this, COLUMN_PLAUSIREPORT_ID, COLUMN_TYPE_ID);

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setOnEditCellHandler(onEditCellCallback)));
        addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));
        addBeforeOption(new Option(table.setOnSelectStateChangedHandler(onRowSelectCallback)));

        // Data processor
        DataProcessor dataProcessor = new DataProcessor(this, onErrorCallback);
        setDataProcessor(dataProcessor);

        // Register callbacks
        registerCallback(onErrorCallback);
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(onEditCellCallback);

        registerCallback(new ShowLastPlausireportCallback(this));
        registerCallback(new ShowDeliveryCallback(this));
        registerCallback(new DownloadFileCallback(this));
        registerCallback(new SynchCommandCallback(this, CallbackConstants.SaveCallback, CommandConstants.SAVE));
        registerCallback(new DeleteRowCallback(this, getDeliveryTable(),_maintainglobals));
        registerCallback(onRowSelectCallback);
        
        registerCallback(new ExportCsvCallback(this, getDeliveryTable(), null, false, _maintainglobals));
    }

    /**
     * Get rows using the parameters from the request
     * 
     * @param params Request parameters
     * @return List with persons
     */
    public ActionList getRows(ParameterList params) {
        Long selected = params.getSelectedRows().get(0);
        return _actionService.getActions(selected);
    }

    /**
     * Gets rows including the table header
     * 
     * @param params request parameters
     * @return xml with the requested rows
     * @throws DhtmlxException Thrown when a mapping error occurs
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        ActionList actionList = new ActionList();
        actionList.setState(1);
        ActionListTableResultMapper resultMapper = new ActionListTableResultMapper(actionList, getLocalizationManager());
        return toXMLStream(resultMapper, true, true);
    }

    /**
     * Gets rows without header information
     * 
     * @param params request parameters
     * @return xml with the requested rows
     * @throws DhtmlxException Thrown when a mapping error occurs
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
         ActionListTableResultMapper resultMapper = new ActionListTableResultMapper(getRows(params), getLocalizationManager());
        return toXMLStream(resultMapper, true, false);
    }

    /**
     * Shows plausi report if available
     * 
     * @param params contains all parameters
     * @return plausi report
     * @throws DhtmlxException
     */
    public FileHttpResult showLastPlausireport(ParameterList params) throws DhtmlxException {
        try {
            String selected = params.getRowId();
            PlausireportResult result = _actionService.getPlausiReport(Long.valueOf(selected));
            return new FileHttpResult(result.getPlausireport(), "PlausiReport.xlsx");
        } catch (Exception e) {
            throw new MebDhtmlxFileException(e);
        }
    }

    public FileDownloadResult downloadFile(ParameterList params) throws DhtmlxException, IOException {
        final String parameter = "gr_id";
        String fileIdParam = params.getParameter(parameter);

        // Extract Variable
        int fileId = Integer.parseInt(fileIdParam);

        // Introduce Variable
        List<SbgUploadFile> documentList = _uploadFileService.findAllByInterventionId(fileId);

        // Check for Null or empty list
        if (documentList != null && !documentList.isEmpty()) {
            SbgUploadFile document = documentList.get(0);

            return new FileDownloadResult(document.getContent(), document.getName(), document.getType());
        } else {
            throw new IOException("No file found for the provided id");
        }
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
       ActionResult result = _actionService.deleteAction((Action) getRowData(selected));
        ActionListTableSingleResultMapper resultMapper = new ActionListTableSingleResultMapper(CommandConstants.DELETE, selected, result, getLocalizationManager());
        return toXMLDataStream(resultMapper);
    }

    /**
     * Shows delivery file if available
     * 
     * @param params contains all parameters
     * @return delivery file
     * @throws DhtmlxException
     */
    public FileHttpResult showDelivery(ParameterList params) throws DhtmlxException {
        try {
            String selected = params.getRowId();
            ExportResult result = _actionService.getDeliveryfile(Long.valueOf(selected));
            return new FileHttpResult(result.getExport(), "DeliveryFile.zip");
        } catch (Exception e) {
            throw new MebDhtmlxFileException(e);
        }
    }

    public void insertFile(long actionID, MultipartFile file) throws DhtmlxException, IOException{

        // Corriger l'encodage du nom de fichier en UTF-8
        String originalFilename = file.getOriginalFilename();
        String utf8Filename = convertToUtf8(originalFilename);

        SbgUploadFile newFile = new SbgUploadFile();
        newFile.setInterventionId(actionID);
        newFile.setName(utf8Filename);
        newFile.setContent(file.getBytes());
        newFile.setType(file.getContentType());
        String locale =_localizationManager.getLanguage();
        _uploadFileService.save(newFile,locale);
    }

    // Méthode utilitaire
    private String convertToUtf8(String name) {
        if (name == null) return null;
        try {
            return new String(name.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return name; // fallback si erreur
        }
    }
}
