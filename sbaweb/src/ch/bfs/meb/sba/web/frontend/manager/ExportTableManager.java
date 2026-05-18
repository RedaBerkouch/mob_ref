/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb
 */
package ch.bfs.meb.sba.web.frontend.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.web.dhtmlx.table.ExportParameterColumn;
import ch.bfs.meb.sba.web.frontend.resultmapper.ExportListTableResultMapper;
import ch.bfs.meb.sba.web.frontend.resultmapper.ExportTableResultMapper;
import ch.bfs.meb.sba.web.service.IExportService;
import ch.bfs.meb.sba.web.ws.sbaexport.Export;
import ch.bfs.meb.sba.web.ws.sbaexport.ExportListResult;
import ch.bfs.meb.sba.web.ws.sbaexport.ExportResult;
import ch.bfs.meb.sba.web.ws.sbaexport.FileResult;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandDispatcher.EDIT;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.FileHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.IHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.callback.ExportCsvCallback;
import ch.bfs.meb.web.commons.dhtmlx.callback.RunExportCallback;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.RowsDocument;
import ch.bfs.meb.web.commons.exception.MebDhtmlxFileException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * This Class represents a ExportTableManager for the delivery tab and acts as a
 * controller for the Export Table.
 */
@Scope("session")
@Component("exportTableManager")
public class ExportTableManager extends TableManagerBase {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(ExportTableManager.class);

    public static final String COLUMN_EXPORT_ID = "exportId";
    public static final String COLUMN_EXPORT_NAME_KEY = "exportTable.column.id.name";
    public static final String COLUMN_SORT_ORDER_ID = "exportOrder";
    public static final String COLUMN_SORT_ORDER_NAME_KEY = "exportTable.column.order.name";
    private static final String COLUMN_NAME_ID = "name";
    public static final String COLUMN_NAME_NAME_KEY = "exportTable.column.name.name";
    private static final String COLUMN_DESCRIPTION_ID = "description";
    public static final String COLUMN_DESCRIPTION_NAME_KEY = "exportTable.column.description.name";

    public static final String COLUMN_TYPE_ID = "type";
    public static final String COLUMN_TYPE_NAME_KEY = "exportTable.column.type.name";
    public static final String COLUMN_AUTHORISATION_ID = "authorisationLevel";
    public static final String COLUMN_AUTHORISATION_NAME_KEY = "exportTable.column.authorisation.name";
    public static final String COLUMN_ACTIVE_ID = "isActive";
    public static final String COLUMN_ACTIVE_NAME_KEY = "exportTable.column.active.name";
    public static final String COLUMN_SOURCE_ID = "source";
    public static final String COLUMN_SOURCE_NAME_KEY = "exportTable.column.source.name";

    private static final String COLUMN_PARAMETER_ID = "parameters";
    public static final String COLUMN_PARAMETER_NAME_KEY = "exportTable.column.parameter.name";

    public static final String MANAGER_NAME = "export";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    @Autowired
    private IExportService _exportService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

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

    /**
     * Initializes a new ExportTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and is called only once.
     */
    @Override
    public void create() throws DhtmlxException {
        addColumn(new IdentityColumn(COLUMN_EXPORT_ID, COLUMN_EXPORT_NAME_KEY, getLocalizationManager()));

        addColumn(new MultilanguageReadOnlyColumn(COLUMN_NAME_ID, COLUMN_NAME_NAME_KEY, getLocalizationManager()));
        addColumn(new MultilanguageReadOnlyColumn(COLUMN_DESCRIPTION_ID, COLUMN_DESCRIPTION_NAME_KEY, getLocalizationManager(), 49));

        addColumn(new ExportParameterColumn(COLUMN_PARAMETER_ID, COLUMN_PARAMETER_NAME_KEY, COLUMN_EXPORT_ID, getLocalizationManager()));

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction runExportCallback = new RunExportCallback(this);

        //		IJavaScriptFunction onRowSelectCallback = new OnRowSelectAdminCallback(this, getParameterTable());
        //		IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        //		IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        //		IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, false);

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        //		addBeforeOption(new Option(table.setOnRowSelectHandler(onRowSelectCallback)));
        //		addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        //		addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));

        // install load error handler
        //		enableLoadErrorHandling();

        // Data processor
        //		setDataProcessor(new DataProcessor(this, onErrorCallback));

        // Register callbacks
        registerCallback(runExportCallback);
        registerCallback(new ExportCsvCallback(this, null, null, false, _maintainglobals));
        //		registerCallback(onRowSelectCallback);
        //		registerCallback(new ShowSortImgCallback(this));
        //		registerCallback(onErrorCallback);
        //		registerCallback(new OnLoadErrorCallback(this, null, null));
        //		registerCallback(onLoadingStartCallback);
        //		registerCallback(onLoadingEndCallback);
        //		registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        //		registerCallback(new InsertRowCallback(this));
        //		registerCallback(new DeleteRowCallback(this));
        //		registerCallback(new SynchCommandCallback(this, CallbackConstants.SaveCallback, CommandConstants.SAVE));
    }

    /**
     * Gets all rows
     * 
     * @return List with all exports
     */
    private ExportListResult getRows() {

        return _exportService.getActiveExports();
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<Export> getExportRows(ParameterList params) {
        return getRows().getExports();
    }

    @Override
    protected String getExportFileName() {
        return "Exports.csv";
    }

    /**
     * Intializes the export table with all rows.
     * 
     * @param params
     *            contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        ExportListTableResultMapper resultMapper = new ExportListTableResultMapper(getRows(), getLocalizationManager());

        return toXMLStream(resultMapper, true, true);
    }

    /**
     * Loads all rows by given parameters.
     * 
     * @param params
     *            contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        ExportListTableResultMapper resultMapper = new ExportListTableResultMapper(getRows(), getLocalizationManager());

        return toXMLStream(resultMapper, true, false);
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
            ExportResult result = _exportService.getExportById(new Long(sid));

            // Maps result
            ExportTableResultMapper resultMapper = new ExportTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            ExportResult result = new ExportResult();
            result.setState(ResultBase.OK);
            result.setExport(new Export());
            ExportTableResultMapper resultMapper = new ExportTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    /**
     * Runs the selected Export.
     * 
     * @param params
     * @return
     * @throws DhtmlxException
     */
    public IHttpResult runExport(ParameterList params) throws DhtmlxException {
        try {
            // Get the source id
            String sid = params.getRowId();

            // find selected export in serialized table data
            Export tableExport = null;
            RowsDocument exportDocument = params.getData(ParameterConstants.PARAM_EXPORTDATA);
            List<Object> exportList = toObjectList(exportDocument, Export.class);
            for (int i = 0; i < exportList.size(); i++) {
                Export e = (Export) exportList.get(i);
                if (e.getExportId().toString().equals(sid)) {
                    tableExport = e;
                    break;
                }
            }

            // take the transient parameter values from serialized table - take all other attributes from loaded data
            Export export = (Export) getRowData(sid);
            export.getParameters().clear();
            export.getParameters().addAll(tableExport.getParameters());

            FileResult result = _exportService.runExport(tableExport, getLocalizationManager().getLanguage().toLowerCase());

            if (ResultBase.OK == result.getState()) {
                return new FileHttpResult(result.getBinaryFile(), result.getFilename());
            } else {
                return new FileHttpResult(_localizationManager.getMessage(result.getMessage()).getBytes(), "error.txt");
            }
        } catch (Exception e) {
            throw new MebDhtmlxFileException(e);
        }
    }
}