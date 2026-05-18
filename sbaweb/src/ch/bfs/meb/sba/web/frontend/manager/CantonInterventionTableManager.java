/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: InterventionTableManager.java 416 2010-01-11 16:45:00Z jfu $
 */
package ch.bfs.meb.sba.web.frontend.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.bfs.meb.sba.web.service.IUploadFileService;
import ch.bfs.meb.sba.web.ws.sbauploadfile.SbaUploadFile;
import ch.bfs.meb.web.commons.dhtmlx.FileDownloadResult;
import ch.bfs.meb.web.commons.util.FileUtils;
import ch.bfs.meb.web.commons.util.ValidationUtils;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.web.frontend.resultmapper.CantonInterventionListTableResultMapper;
import ch.bfs.meb.sba.web.frontend.resultmapper.CantonInterventionTableResultMapper;
import ch.bfs.meb.sba.web.service.ICantonInterventionService;
import ch.bfs.meb.sba.web.service.ICantonService;
import ch.bfs.meb.sba.web.ws.sbacanton.CantonResult;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.CantonIntervention;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.CantonInterventionListResult;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.CantonInterventionResult;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.FileResult;
import ch.bfs.meb.sba.web.ws.sbaparameter.Parameter;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandDispatcher.EDIT;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.FileHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
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
import org.springframework.web.multipart.MultipartFile;
/**
 * This Class represents a CantonInterventionTableManager for the maintain tab and acts
 * as a controller for the CantonIntervention Table.
 * 
 * @author $Author: jfu $
 * @version $Revision: 416 $
 */
@Scope("session")
@Component("cantonInterventionTableManager")
public class CantonInterventionTableManager extends TableManagerBase {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(CantonInterventionTableManager.class);

    private static final String COLUMN_INTERVENTION_ID = "interventionId";
    private static final String COLUMN_INTERVENTION_NAME_KEY = "interventionTable.column.interventionid.name";
    private static final String COLUMN_CANTON_ID = "canton";
    private static final String COLUMN_CANTON_NAME_KEY = "interventionTable.column.canton.name";
    private static final String COLUMN_VERSION_ID = "version";
    private static final String COLUMN_VERSION_NAME_KEY = "interventionTable.column.version.name";
    private static final String COLUMN_INTERVENTION_USER_ID = "interventionUser";
    private static final String COLUMN_INTERVENTION_USER_NAME_KEY = "interventionTable.column.interventionuser.name";
    private static final String COLUMN_INTERVENTION_DATE_ID = "InterventionDate";
    private static final String COLUMN_INTERVENTION_DATE_NAME_KEY = "interventionTable.column.interventiondate.name";
    private static final String COLUMN_TYPE_ID = "type";
    private static final String COLUMN_TYPE_NAME_KEY = "interventionTable.column.type.name";
    private static final String COLUMN_TEXT_ID = "text";
    private static final String COLUMN_TEXT_NAME_KEY = "interventionTable.column.text.name";

    public static final String MANAGER_NAME = "cantonIntervention";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    protected ICantonInterventionService _interventionService;

    @Autowired
    protected ICantonService _cantonService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    IUploadFileService _uploadFileService;

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

    public IDhtmlxControl getCantonTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return CantonTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return CantonTableManager.MANAGER_NAME;
            }
        };
    }

    public CantonInterventionListResult getRows(ParameterList params) {
        CantonInterventionListResult result;

        ArrayList<Long> selectedRowIds = params.getSelectedRows();
        if (selectedRowIds.size() == 0) {
            result = new CantonInterventionListResult();
        } else {
            Long selected = selectedRowIds.get(0);
            result = _interventionService.getInterventionsForCanton(selected);
        }

        return result;
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<CantonIntervention> getExportRows(ParameterList params) {
        return getRows(params).getInterventions();
    }

    @Override
    protected String getExportFileName() {
        return "Interventions.csv";
    }

    protected void setMasterKey(Parameter parameter, Long foreignKey) {
        parameter.setFilterId(foreignKey);
    }

    public void create() throws DhtmlxException {
        addColumn(new IdentityColumn(COLUMN_INTERVENTION_ID, COLUMN_INTERVENTION_NAME_KEY, getLocalizationManager()));

        ComboCodeGroupColumn cantonColumn = new ComboCodeGroupColumn(COLUMN_CANTON_ID, COLUMN_CANTON_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.CANTON, 15);
        cantonColumn.setEditType(EditType.readonly);
        cantonColumn.setColor(COLOR.LIGHTGREY);
        cantonColumn.setSort(SORT.NO_SORT);
        addColumn(cantonColumn);

        addColumn(new ReadOnlyColumn(COLUMN_VERSION_ID, COLUMN_VERSION_NAME_KEY, getLocalizationManager(), 4));
        addColumn(new ReadOnlyColumn(COLUMN_INTERVENTION_USER_ID, COLUMN_INTERVENTION_USER_NAME_KEY, getLocalizationManager(), 10));
        Column interventionDateColumn = new DateTimeColumn(COLUMN_INTERVENTION_DATE_ID, COLUMN_INTERVENTION_DATE_NAME_KEY, getLocalizationManager());
        interventionDateColumn.setDefault("");
        addColumn(interventionDateColumn);

        ComboCodeGroupColumn typeColumn = new ComboCodeGroupColumn(COLUMN_TYPE_ID, COLUMN_TYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.SBA_CANTONINTERVENTIONTYPE, 15, CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_MANUAL);
        typeColumn.setDefault(CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_MANUAL);
        typeColumn.setSort(SORT.NO_SORT);
        addColumn(typeColumn);

        addColumn(new Column(COLUMN_TEXT_ID, COLUMN_TEXT_NAME_KEY, getLocalizationManager(), 47));

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, false);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onRowSelectCallback = new RefreshCantonInterventionButtonsCallback(this, COLUMN_TYPE_ID) {
            @Override
            protected long getCantonInterventionTypeCreatePlausi() {
                return CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT;
            }

            @Override
            protected long getCantonInterventionTypeManual() {
                return CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_MANUAL;
            }

            @Override
            protected long getCantonInterventionTypeUpload() {
                return CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_UPLOAD;
            }
        };
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getCantonTable());

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setOnEditCellHandler(onEditCellCallback)));
        addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));
        addBeforeOption(new Option(table.setOnSelectStateChangedHandler(onRowSelectCallback)));
        addBeforeOption(new Option(table.setOnGridReconstructedHandler(onGridReconstructedCallback)));

        // Data processor
        DataProcessor dataProcessor = new DataProcessor(this, onErrorCallback);
        dataProcessor.setRowMarkFunction(onRowMarkCallback);
        setDataProcessor(dataProcessor);

        // Register callbacks
        registerCallback(onEditCellCallback);
        registerCallback(onErrorCallback);
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);

        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertInterventionRowCallback(this, getCantonTable(), _maintainglobals));
        registerCallback(new DeleteRowCallback(this, getCantonTable(), _maintainglobals));
        registerCallback(new SaveCallback(this, getCantonTable(), _maintainglobals));
        registerCallback(new ShowLastPlausireportCallback(this));
        registerCallback(new ShowDeliveryCallback(this));
        registerCallback(new DownloadFileCallback(this));
        registerCallback(onRowSelectCallback);
        registerCallback(onGridReconstructedCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(new ExportCsvCallback(this, getCantonTable(), null, false, _maintainglobals));
    }

    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        CantonInterventionListResult interventionListResult = new CantonInterventionListResult();
        interventionListResult.setState(ResultBase.OK);
        CantonInterventionListTableResultMapper resultMapper = new CantonInterventionListTableResultMapper(interventionListResult, getLocalizationManager());
        return toXMLStream(resultMapper, true, true);
    }

    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        CantonInterventionListTableResultMapper resultMapper = new CantonInterventionListTableResultMapper(getRows(params), getLocalizationManager());

        return toXMLStream(resultMapper, true, false);
    }

    /**
     * Update manual intervention
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
        CantonIntervention intervention = (CantonIntervention) merge(params);

        //TODO: msc
        //		intervention.setType(CodegroupUtility.MEB_CANTONINTERVENTIONTYPE_MANUAL);

        CantonInterventionResult result = _interventionService.updateIntervention(intervention);

        CantonResult cantonResult = _cantonService.getCantonById(intervention.getCantonId());
        if (cantonResult.getCanton() == null) {
            return toXMLDataErrorStream(getLocalizationManager().getMessage(cantonResult.getMessage()), sid);
        }

        // set transient attributes
        result.getIntervention().setVersion(cantonResult.getCanton().getVersion());
        result.getIntervention().setCanton(cantonResult.getCanton().getCanton());

        // Maps result
        CantonInterventionTableResultMapper resultMapper = new CantonInterventionTableResultMapper(CommandConstants.UPDATE, sid, result,
                getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        List<Long> selIds = params.getSelectedRows();
        if (selIds.size() != 1) {
            // TODO add specific error message
            return toXMLDataErrorStream(getLocalizationManager().getMessage("unknown.error.message"), sid);
        }
        CantonResult cantonResult = _cantonService.getCantonById(selIds.get(0));
        if (cantonResult.getCanton() == null) {
            return toXMLDataErrorStream(getLocalizationManager().getMessage(cantonResult.getMessage()), sid);
        }

        // Merge with an empty record
        CantonIntervention intervention = (CantonIntervention) merge(new CantonIntervention(), params);

        // set parent id
        intervention.setCantonId(cantonResult.getCanton().getCantonId());

        // set user
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        intervention.setInterventionUser(user.getEmail());

        CantonInterventionResult result = _interventionService.insertIntervention(intervention);

        // set transient attributes
        result.getIntervention().setVersion(cantonResult.getCanton().getVersion());
        result.getIntervention().setCanton(cantonResult.getCanton().getCanton());

        // Maps result
        CantonInterventionTableResultMapper resultMapper = new CantonInterventionTableResultMapper(CommandConstants.INSERT, sid, result,
                getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }
    public FileDownloadResult downloadFile(ParameterList params) throws DhtmlxException, IOException {
        final String parameter = "gr_id";
        String fileIdParam = params.getParameter(parameter);

        // Extract Variable
        int fileId = Integer.parseInt(fileIdParam);

        // Introduce Variable
        List<SbaUploadFile> documentList = _uploadFileService.findAllByInterventionId(fileId);

        // Check for Null or empty list
        if (documentList != null && !documentList.isEmpty()) {
            SbaUploadFile document = documentList.get(0);

            return new FileDownloadResult(document.getContent(), document.getName(), document.getType());
        } else {
            throw new IOException("No file found for the provided id");
        }
    }


    public void insertFile(Long cantonId, MultipartFile file) throws DhtmlxException, IOException {
        // Get the source id
        Long sid = cantonId;
        CantonResult cantonResult = _cantonService.getCantonById(sid);

        // Merge with an empty record
        CantonIntervention intervention = new CantonIntervention();

        // set parent id
        intervention.setCantonId(cantonResult.getCanton().getCantonId());

        // set user
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Corriger l'encodage du nom de fichier en UTF-8
        String originalFilename = file.getOriginalFilename();
        String utf8Filename = convertToUtf8(originalFilename);

        // set intervention
        intervention.setInterventionUser(user.getEmail());
        intervention.setType(CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_UPLOAD);
        intervention.setText(utf8Filename); // Nom en UTF-8

        CantonInterventionResult result = _interventionService.insertIntervention(intervention);

        // set file
        SbaUploadFile newFile = new SbaUploadFile();
        newFile.setInterventionId(result.getIntervention().getInterventionId());
        newFile.setName(utf8Filename); // Nom en UTF-8
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

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        CantonInterventionResult result = _interventionService.deleteIntervention((CantonIntervention) getRowData(selected));

        // Maps result
        CantonInterventionTableResultMapper resultMapper = new CantonInterventionTableResultMapper(CommandConstants.DELETE, selected, result,
                getLocalizationManager());

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
            CantonInterventionResult result = _interventionService.getInterventionById(new Long(sid));

            // Maps result
            CantonInterventionTableResultMapper resultMapper = new CantonInterventionTableResultMapper(CommandConstants.UNDO, sid, result,
                    getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            CantonInterventionResult result = new CantonInterventionResult();
            result.setState(ResultBase.OK);
            result.setIntervention(new CantonIntervention());
            CantonInterventionTableResultMapper resultMapper = new CantonInterventionTableResultMapper(CommandConstants.DELETE, sid, result,
                    getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    protected boolean isReadOnly(CantonIntervention intervention) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user.isInRole(SecurityConstants.ROLE_SBA_DL)) {
            if (intervention.getType() != null && intervention.getType() >= CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_MANUAL) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<String, String>();
        CantonIntervention intervention = (CantonIntervention) row;
        String readOnlyStr;
        // COLUMN_CANTON, COLUMN_VERSION, COLUMN_INTERVENTION_USER
        // COLUMN_INTERVENTION_DATE, COLUMN_TYPE, COLUMN_TEXT

        if (isReadOnly(intervention)) {
            readOnlyStr = "111111";
        } else {
            readOnlyStr = "111100";
        }

        userData.put("readOnlyCells", readOnlyStr);
        return userData;
    }

    /**
     * Shows plausi report if available
     * 
     * @param params contains all parameters
     * @return plausi report
     * @throws DhtmlxException
     */
    public FileHttpResult showLastPlausireport(ParameterList params) throws DhtmlxException {
        String filename = "PlausiReport";
        try {
            String selected = params.getRowId();
            FileResult result = _interventionService.getPlausiReportFile(Long.valueOf(selected));

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

}