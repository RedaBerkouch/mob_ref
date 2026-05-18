/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id: InterventionTableManager.java 416 2010-01-11 16:45:00Z jfu $
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.ssp.web.frontend.resultmapper.InterventionListTableResultMapper;
import ch.bfs.meb.ssp.web.frontend.resultmapper.InterventionTableResultMapper;
import ch.bfs.meb.ssp.web.service.IDeliveryService;
import ch.bfs.meb.ssp.web.service.IInterventionService;
import ch.bfs.meb.ssp.web.ws.sspdelivery.SspDeliveryResult;
import ch.bfs.meb.ssp.web.ws.sspintervention.FileResult;
import ch.bfs.meb.ssp.web.ws.sspintervention.Intervention;
import ch.bfs.meb.ssp.web.ws.sspintervention.InterventionListResult;
import ch.bfs.meb.ssp.web.ws.sspintervention.InterventionResult;
import ch.bfs.meb.ssp.web.ws.sspparameter.Parameter;
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

/**
 * This Class represents a InterventionTableManager for the maintain tab and acts
 * as a controller for the Intervention Table.
 * 
 * @author $Author: jfu $
 * @version $Revision: 416 $
 */
@Scope("session")
@Component("interventionTableManager")
public class InterventionTableManager extends TableManagerBase {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(InterventionTableManager.class);

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

    private static final String COLUMN_REPORT_ID = "report";
    private static final String COLUMN_REPORT_NAME_KEY = "interventionTable.column.report.name";

    public static final String MANAGER_NAME = "intervention";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    protected IInterventionService _interventionService;

    @Autowired
    protected IDeliveryService _deliveryService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

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

    public InterventionListResult getRows(ParameterList params) {
        InterventionListResult result;

        ArrayList<Long> selectedRowIds = params.getSelectedRows();
        if (selectedRowIds.size() == 0) {
            result = new InterventionListResult();
        } else {
            Long selected = selectedRowIds.get(0);
            result = _interventionService.getInterventionsForDelivery(selected);
        }

        return result;
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<Intervention> getExportRows(ParameterList params) {
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

        Column typeColumn = new StatusColumn(COLUMN_TYPE_ID, COLUMN_TYPE_NAME_KEY, getLocalizationManager(), CodegroupUtility.MEB_INTERVENTIONTYPE, 15);
        typeColumn.setEditType(EditType.readonly);
        typeColumn.setColor(COLOR.LIGHTGREY);
        typeColumn.setDefault(CodegroupUtility.MEB_INTERVENTIONTYPE_MANUAL);
        typeColumn.setSort(SORT.NO_SORT);
        addColumn(typeColumn);

        addColumn(new MultilanguageColumn(COLUMN_REPORT_ID, COLUMN_REPORT_NAME_KEY, getLocalizationManager(), 47));

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, false);
        IJavaScriptFunction onEditCellCallback = new OnEditCellCallback(this);
        IJavaScriptFunction onRowSelectCallback = new RefreshInterventionButtonsCallback(this, COLUMN_TYPE_ID);
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getDeliveryTable());

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
        registerCallback(new InsertInterventionRowCallback(this, getDeliveryTable(), _maintainglobals));
        registerCallback(new DeleteRowCallback(this, getDeliveryTable(), _maintainglobals));
        registerCallback(new SaveCallback(this, getDeliveryTable(), _maintainglobals));
        registerCallback(new ShowLastPlausireportCallback(this));
        registerCallback(new ShowDeliveryCallback(this));
        registerCallback(onRowSelectCallback);
        registerCallback(onGridReconstructedCallback);
        registerCallback(onRowMarkCallback);

        registerCallback(new ExportCsvCallback(this, getDeliveryTable(), null, false, _maintainglobals));
    }

    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        InterventionListResult interventionListResult = new InterventionListResult();
        interventionListResult.setState(ResultBase.OK);
        InterventionListTableResultMapper resultMapper = new InterventionListTableResultMapper(interventionListResult, getLocalizationManager());
        return toXMLStream(resultMapper, true, true);
    }

    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        InterventionListTableResultMapper resultMapper = new InterventionListTableResultMapper(getRows(params), getLocalizationManager());

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
        Intervention intervention = (Intervention) merge(params);

        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_MANUAL);

        // synchronize text
        String language = getLocalizationManager().getLanguage();
        if ("fr".equals(language.toLowerCase())) {
            intervention.setReportDe(intervention.getReportFr());
            intervention.setReportIt(intervention.getReportFr());
        } else if ("it".equals(language.toLowerCase())) {
            intervention.setReportDe(intervention.getReportIt());
            intervention.setReportFr(intervention.getReportIt());
        } else {
            intervention.setReportFr(intervention.getReportDe());
            intervention.setReportIt(intervention.getReportDe());
        }

        InterventionResult result = _interventionService.updateIntervention(intervention);

        SspDeliveryResult deliveryResult = _deliveryService.getDeliveryById(intervention.getDeliveryId());
        if (deliveryResult.getDelivery() == null) {
            return toXMLDataErrorStream(getLocalizationManager().getMessage(deliveryResult.getMessage()), sid);
        }

        // set transient attributes
        result.getIntervention().setVersion(deliveryResult.getDelivery().getVersion());
        result.getIntervention().setCanton(deliveryResult.getDelivery().getCanton());

        // Maps result
        InterventionTableResultMapper resultMapper = new InterventionTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

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
        SspDeliveryResult deliveryResult = _deliveryService.getDeliveryById(selIds.get(0));
        if (deliveryResult.getDelivery() == null) {
            return toXMLDataErrorStream(getLocalizationManager().getMessage(deliveryResult.getMessage()), sid);
        }

        // Merge with an empty record
        Intervention intervention = (Intervention) merge(new Intervention(), params);

        intervention.setType(CodegroupUtility.MEB_INTERVENTIONTYPE_MANUAL);

        // set parent id
        intervention.setDeliveryId(deliveryResult.getDelivery().getDeliveryId());

        // synchronize text
        String language = getLocalizationManager().getLanguage();
        if ("fr".equals(language.toLowerCase())) {
            intervention.setReportDe(intervention.getReportFr());
            intervention.setReportIt(intervention.getReportFr());
        } else if ("it".equals(language.toLowerCase())) {
            intervention.setReportDe(intervention.getReportIt());
            intervention.setReportFr(intervention.getReportIt());
        } else {
            intervention.setReportFr(intervention.getReportDe());
            intervention.setReportIt(intervention.getReportDe());
        }

        // set user
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        intervention.setInterventionUser(user.getEmail());

        InterventionResult result = _interventionService.insertIntervention(intervention);

        // set transient attributes
        result.getIntervention().setVersion(deliveryResult.getDelivery().getVersion());
        result.getIntervention().setCanton(deliveryResult.getDelivery().getCanton());

        // Maps result
        InterventionTableResultMapper resultMapper = new InterventionTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException {
        String selected = params.getRowId();
        InterventionResult result = _interventionService.deleteIntervention((Intervention) getRowData(selected));

        // Maps result
        InterventionTableResultMapper resultMapper = new InterventionTableResultMapper(CommandConstants.DELETE, selected, result, getLocalizationManager());

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
            InterventionResult result = _interventionService.getInterventionById(new Long(sid));

            // Maps result
            InterventionTableResultMapper resultMapper = new InterventionTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            InterventionResult result = new InterventionResult();
            result.setState(ResultBase.OK);
            result.setIntervention(new Intervention());
            InterventionTableResultMapper resultMapper = new InterventionTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    protected boolean isReadOnly(Intervention intervention) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user.isInRole(SecurityConstants.ROLE_SSP_DL)) {
            if (intervention.getType() != null && intervention.getType().equals(CodegroupUtility.MEB_INTERVENTIONTYPE_MANUAL)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Map<String, String> getRowUserData(Object row) {
        HashMap<String, String> userData = new HashMap<String, String>();
        Intervention intervention = (Intervention) row;
        String readOnlyStr;
        // COLUMN_CANTON, COLUMN_VERSION, COLUMN_INTERVENTION_USER
        // COLUMN_INTERVENTION_DATE, COLUMN_TYPE, COLUMN_REPORT

        if (isReadOnly(intervention)) {
            readOnlyStr = "111111";
        } else {
            readOnlyStr = "111110";
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
            FileResult result = _interventionService.getDeliveryFile(Long.valueOf(selected));

            return new FileHttpResult(result.getBinaryFile(), "DeliveryFile.zip");
        } catch (Exception e) {
            throw new MebDhtmlxFileException(e);
        }
    }
}