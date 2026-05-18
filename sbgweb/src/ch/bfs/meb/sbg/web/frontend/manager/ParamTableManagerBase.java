/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ParamTableManagerBase.java 641 2010-11-25 08:29:14Z msc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import java.util.List;

import ch.admin.bfs.sbg.dhtmlx.CommandDispatcher.EDIT;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.resultmapper.ParamListTableResultMapper;
import ch.bfs.meb.sbg.web.resultmapper.ParamTableResultMapper;
import ch.bfs.meb.sbg.web.service.IMacroParameterService;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.MacroParameterResult;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.Parameter;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.ParameterListResult;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.SbgParameter;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ognl.OgnlException;

/**
 * TODO Describe this class
 *
 * @author $Author: msc $
 * @version $Revision: 641 $
 */
public abstract class ParamTableManagerBase extends TableManagerBase {
    private static final String COLUMN_ID_ID = "parameterId";
    private static final String COLUMN_ID_NAME_KEY = "adminParamTable.column.id.name";
    private static final String COLUMN_SORT_ORDER_ID = "parameterOrder";
    private static final String COLUMN_SORT_ORDER_NAME_KEY = "adminParamTable.column.sortorder.name";
    private static final String COLUMN_UNIQUE_NAME_ID = "uniqueName";
    private static final String COLUMN_UNIQUE_NAME_NAME_KEY = "adminParamTable.column.id.name";
    private static final String COLUMN_NAME_DE_ID = "nameDe";
    private static final String COLUMN_NAME_DE_NAME_KEY = "adminFilterTable.column.name.german.name";
    private static final String COLUMN_NAME_FR_ID = "nameFr";
    private static final String COLUMN_NAME_FR_NAME_KEY = "adminFilterTable.column.name.french.name";
    private static final String COLUMN_DEFAULT_ID = "defaultValue";
    private static final String COLUMN_DEFAULT_NAME_KEY = "adminParamTable.column.default.name";

    public void create() throws DhtmlxException {
        addColumn(new IdentityColumn(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager()));

        addColumn(new Column(COLUMN_SORT_ORDER_ID, COLUMN_SORT_ORDER_NAME_KEY, getLocalizationManager(), 8));
        addColumn(new Column(COLUMN_UNIQUE_NAME_ID, COLUMN_UNIQUE_NAME_NAME_KEY, getLocalizationManager(), 15));
        addColumn(new Column(COLUMN_NAME_DE_ID, COLUMN_NAME_DE_NAME_KEY, getLocalizationManager(), 35));
        addColumn(new Column(COLUMN_NAME_FR_ID, COLUMN_NAME_FR_NAME_KEY, getLocalizationManager(), 35));
        addColumn(new Column(COLUMN_DEFAULT_ID, COLUMN_DEFAULT_NAME_KEY, getLocalizationManager(), 14));

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, false);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getFilterTable());

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));

        // Data processor
        DataProcessor dataProcessor = new DataProcessor(this, onErrorCallback);
        dataProcessor.setRowMarkFunction(onRowMarkCallback);
        setDataProcessor(dataProcessor);

        // Register callbacks
        registerCallback(new ShowSortImgCallback(this));
        registerCallback(onErrorCallback);
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertRowCallback(this));
        registerCallback(new DeleteRowCallback(this));
        registerCallback(new SaveParamCallback(this, getFilterTable()));
        registerCallback(onRowMarkCallback);
    }

    public abstract IDhtmlxControl getFilterTable();

    public abstract ParameterListResult getRows(ch.bfs.meb.web.commons.dhtmlx.table.ParameterList params);

    public abstract IMacroParameterService getMacroParameterService();

    /**
     * Gets all rows for export
     *
     * @return List with all rows
     */
    @Override
    protected List<SbgParameter> getExportRows(ch.bfs.meb.web.commons.dhtmlx.table.ParameterList params) {
        return getRows(params).getParameters();
    }

    public DhtmlxTableXML init(ch.bfs.meb.web.commons.dhtmlx.table.ParameterList params) throws DhtmlxException {
        ParameterListResult parameterListResult = new ParameterListResult();
        parameterListResult.setState(ResultBase.OK);
        ParamListTableResultMapper resultMapper = new ParamListTableResultMapper(parameterListResult, getLocalizationManager());
        return toXMLStream(resultMapper, true, true);
    }

    public DhtmlxTableXML load(ch.bfs.meb.web.commons.dhtmlx.table.ParameterList params) throws DhtmlxException {
        ParamListTableResultMapper resultMapper = new ParamListTableResultMapper(getRows(params), getLocalizationManager());
        return toXMLStream(resultMapper, true, false);
    }

    /**
     * Gets all unmodified rows from DB. This methode sets the initial values.
     *
     * @param params
     * @return
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML undo(ch.bfs.meb.web.commons.dhtmlx.table.ParameterList params) throws DhtmlxException {
        // Get the source id
        String sid = params.getRowId();

        if (!params.getEditorStatus().equals(EDIT.INSERT)) {
            MacroParameterResult result = getMacroParameterService().getParameterById(new Long(sid));

            // Maps result
            ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            MacroParameterResult result = new MacroParameterResult();
            result.setState(1);
            result.setMacroParameter(new SbgParameter());
            ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML update(ch.bfs.meb.web.commons.dhtmlx.table.ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // merge new data record with cache
        SbgParameter parameter = (SbgParameter) merge(params);

        MacroParameterResult result = getMacroParameterService().updateParameter(parameter, getLocalizationManager().getLanguage());

        // Maps result
        ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ch.bfs.meb.web.commons.dhtmlx.table.ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        SbgParameter parameter = (SbgParameter) merge(params);

        MacroParameterResult result = getMacroParameterService().deleteParameter(parameter);

        // Maps result
        ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML insert(ch.bfs.meb.web.commons.dhtmlx.table.ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // Merge with an empty record
        SbgParameter parameter = (SbgParameter) merge(new SbgParameter(), params);

        setMasterKey(parameter, params.getSelectedRows().get(0));

        MacroParameterResult result = getMacroParameterService().insertParameter(parameter, getLocalizationManager().getLanguage());

        // Maps result
        ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    protected abstract void setMasterKey(Parameter parameter, Long foreignKey);
}
