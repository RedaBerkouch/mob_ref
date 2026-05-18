/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.sdl.web.frontend.manager;

import java.util.List;

import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.frontend.resultmapper.ParamListTableResultMapper;
import ch.bfs.meb.sdl.web.frontend.resultmapper.ParamTableResultMapper;
import ch.bfs.meb.sdl.web.service.IParameterService;
import ch.bfs.meb.sdl.web.ws.sdlparameter.Parameter;
import ch.bfs.meb.sdl.web.ws.sdlparameter.ParameterListResult;
import ch.bfs.meb.sdl.web.ws.sdlparameter.ParameterResult;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandDispatcher.EDIT;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;

/**
 * Abstract base class for dhtmlx table managers managing parameters, acts as a
 * controller for the Param Tables. 
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class ParamTableManagerBase extends TableManagerBase {
    private static final String COLUMN_ID_ID = "parameterId";
    private static final String COLUMN_ID_NAME_KEY = "adminParamTable.column.id.name";
    private static final String COLUMN_SORT_ORDER_ID = "parameterOrder";
    private static final String COLUMN_SORT_ORDER_NAME_KEY = "adminParamTable.column.sortorder.name";
    private static final String COLUMN_UNIQUENAME_ID = "uniqueName";
    private static final String COLUMN_UNIQUENAME_NAME_KEY = "adminParamTable.column.name.unique.name";
    private static final String COLUMN_NAME_DE_ID = "nameDe";
    private static final String COLUMN_NAME_DE_NAME_KEY = "adminParamTable.column.name.german.name";
    private static final String COLUMN_NAME_FR_ID = "nameFr";
    private static final String COLUMN_NAME_FR_NAME_KEY = "adminParamTable.column.name.french.name";
    private static final String COLUMN_NAME_IT_ID = "nameIt";
    private static final String COLUMN_NAME_IT_NAME_KEY = "adminParamTable.column.name.italian.name";
    private static final String COLUMN_DEFAULT_ID = "defaultValue";
    private static final String COLUMN_DEFAULT_NAME_KEY = "adminParamTable.column.default.name";

    @Autowired
    protected IParameterService _parameterService;

    public void create() throws DhtmlxException {
        addColumn(new IdentityColumn(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager()));

        addColumn(new Column(COLUMN_SORT_ORDER_ID, COLUMN_SORT_ORDER_NAME_KEY, getLocalizationManager(), 8));
        addColumn(new Column(COLUMN_UNIQUENAME_ID, COLUMN_UNIQUENAME_NAME_KEY, getLocalizationManager(), 15));
        addColumn(new Column(COLUMN_NAME_DE_ID, COLUMN_NAME_DE_NAME_KEY, getLocalizationManager(), 15));
        addColumn(new Column(COLUMN_NAME_FR_ID, COLUMN_NAME_FR_NAME_KEY, getLocalizationManager(), 15));
        addColumn(new Column(COLUMN_NAME_IT_ID, COLUMN_NAME_IT_NAME_KEY, getLocalizationManager(), 15));
        addColumn(new Column(COLUMN_DEFAULT_ID, COLUMN_DEFAULT_NAME_KEY, getLocalizationManager(), 30));

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

    public abstract ParameterListResult getRows(ParameterList params);

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<Parameter> getExportRows(ParameterList params) {
        return getRows(params).getParameters();
    }

    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        ParameterListResult parameterListResult = new ParameterListResult();
        parameterListResult.setState(ResultBase.OK);
        ParamListTableResultMapper resultMapper = new ParamListTableResultMapper(parameterListResult, getLocalizationManager());
        return toXMLStream(resultMapper, true, true);
    }

    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        ParamListTableResultMapper resultMapper = new ParamListTableResultMapper(getRows(params), getLocalizationManager());
        ;

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
            ParameterResult result = _parameterService.getParameterById(new Long(sid));

            // Maps result
            ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            ParameterResult result = new ParameterResult();
            result.setState(ResultBase.OK);
            result.setParameter(new Parameter());
            ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // merge new data record with cache
        Parameter parameter = (Parameter) merge(params);

        ParameterResult result = _parameterService.updateParameter(parameter);

        // Maps result
        ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        Parameter parameter = (Parameter) merge(params);

        ParameterResult result = _parameterService.deleteParameter(parameter);

        // Maps result
        ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // Merge with an empty record
        Parameter parameter = (Parameter) merge(new Parameter(), params);

        setMasterKey(parameter, params.getSelectedRows().get(0));

        ParameterResult result = _parameterService.insertParameter(parameter);

        // Maps result
        ParamTableResultMapper resultMapper = new ParamTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    protected abstract void setMasterKey(Parameter parameter, Long foreignKey);
}
