/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: AdminMacroTableManager.java 632 2010-11-22 10:10:15Z msc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import java.util.List;

import ognl.OgnlException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.admin.bfs.sbg.dhtmlx.CommandDispatcher.EDIT;
import ch.bfs.meb.sbg.web.resultmapper.MacroListTableResultMapper;
import ch.bfs.meb.sbg.web.resultmapper.MacroTableResultMapper;
import ch.bfs.meb.sbg.web.service.IMacroService;
import ch.bfs.meb.sbg.web.ws.sbgmacro.Macro;
import ch.bfs.meb.sbg.web.ws.sbgmacro.MacroList;
import ch.bfs.meb.sbg.web.ws.sbgmacro.MacroResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * This Class represents a MacroTableManager for the admin tab and acts as a
 * controller for the Macro Table.
 * 
 * @author $Author: msc $
 * @version $Revision: 632 $
 */
@Scope("session")
@Component("adminMacroTableManager")
public class AdminMacroTableManager extends TableManagerBase {
    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(AdminMacroTableManager.class);

    public static final String COLUMN_MACRO_ID = "macroid";
    public static final String COLUMN_MACRO_NAME_KEY = "adminMacroTable.column.id.name";
    public static final String COLUMN_NAME_DE_ID = "nameDe";
    public static final String COLUMN_NAME_DE_NAME_KEY = "adminMacroTable.column.name.german.name";
    public static final String COLUMN_NAME_FR_ID = "nameFr";
    public static final String COLUMN_NAME_FR_NAME_KEY = "adminMacroTable.column.name.french.name";
    public static final String COLUMN_TYPE_ID = "type";
    public static final String COLUMN_TYPE_NAME_KEY = "adminMacroTable.column.type.name";
    public static final String COLUMN_DESCRIPTION_DE_ID = "descriptionDe";
    public static final String COLUMN_DESCRIPTION_DE_NAME_KEY = "adminMacroTable.column.description.german.name";
    public static final String COLUMN_DESCRIPTION_FR_ID = "descriptionFr";
    public static final String COLUMN_DESCRIPTION_FR_NAME_KEY = "adminMacroTable.column.description.french.name";
    public static final String COLUMN_MACROORDER_ID = "order";
    public static final String COLUMN_MACROORDER_NAME_KEY = "adminMacroTable.column.order.name";
    public static final String COLUMN_AUTHORISATION_ID = "authorisationlevel";
    public static final String COLUMN_AUTHORISATION_NAME_KEY = "adminMacroTable.column.authorisation.name";
    public static final String COLUMN_PATH_ID = "source";
    public static final String COLUMN_PATH_NAME_KEY = "adminMacroTable.column.path.name";
    public static final String COLUMN_OBJECT_ID = "objecttype";
    public static final String COLUMN_OBJECT_NAME_KEY = "adminMacroTable.column.object.name";
    public static final String COLUMN_ACTIVE_ID = "isactive";
    public static final String COLUMN_ACTIVE_NAME_KEY = "adminMacroTable.column.active.name";
    public static final String COLUMN_CONFIRMABLE_ID = "isconfirmable";
    public static final String COLUMN_CONFIRMABLE_NAME_KEY = "adminMacroTable.column.confirmable.name";

    public static final String MANAGER_NAME = "adminMacro";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IMacroService _macroService;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    private IDhtmlxControl getParameterTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return MacroParamTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return MacroParamTableManager.MANAGER_NAME;
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

    /**
     * Initializes a new AdminFilterTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and is called only once.
     */
    public void create() throws DhtmlxException {
        addColumn(new IdentityColumn(COLUMN_MACRO_ID, COLUMN_MACRO_NAME_KEY, getLocalizationManager()));

        addColumn(new Column(COLUMN_NAME_DE_ID, COLUMN_NAME_DE_NAME_KEY, getLocalizationManager(), 8));
        addColumn(new Column(COLUMN_NAME_FR_ID, COLUMN_NAME_FR_NAME_KEY, getLocalizationManager(), 8));

        ComboCodeGroupColumn typeColumn = new ComboCodeGroupColumn(COLUMN_TYPE_ID, COLUMN_TYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.SBG_MACROTYPE, 6);
        addColumn(typeColumn);

        addColumn(new Column(COLUMN_DESCRIPTION_DE_ID, COLUMN_DESCRIPTION_DE_NAME_KEY, getLocalizationManager(), 16));
        addColumn(new Column(COLUMN_DESCRIPTION_FR_ID, COLUMN_DESCRIPTION_FR_NAME_KEY, getLocalizationManager(), 16));

        ComboCodeGroupColumn authColumn = new ComboCodeGroupColumn(COLUMN_AUTHORISATION_ID, COLUMN_AUTHORISATION_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.SBG_ROLE, 4);
        addColumn(authColumn);

        addColumn(new Column(COLUMN_PATH_ID, COLUMN_PATH_NAME_KEY, getLocalizationManager(), 26));

        ComboCodeGroupColumn objectColumn = new ComboCodeGroupColumn(COLUMN_OBJECT_ID, COLUMN_OBJECT_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.SBG_OBJECTTYPE, 6);
        addColumn(objectColumn);

        addColumn(new CheckboxColumn(COLUMN_ACTIVE_ID, COLUMN_ACTIVE_NAME_KEY, getLocalizationManager()));
        addColumn(new CheckboxColumn(COLUMN_CONFIRMABLE_ID, COLUMN_CONFIRMABLE_NAME_KEY, getLocalizationManager()));
        Column orderColumn = new Column(COLUMN_MACROORDER_ID, COLUMN_MACROORDER_NAME_KEY, getLocalizationManager(), 6);
        orderColumn.setSort(Column.SORT.INT);
        addColumn(orderColumn);

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onRowSelectCallback = new OnRowSelectAdminCallback(this, getParameterTable(), _maintainglobals);
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, false);
        IJavaScriptFunction onGridReconstructedCallback = new OnGridReconstructedReloadChildCallback(this, getParameterTable(), _maintainglobals);
        IJavaScriptFunction onAfterClickCallback = new OnAfterClickCallback(this, getParameterTable(), _maintainglobals);
        IJavaScriptFunction onRowMarkCallback = new OnRowMarkCallback(this, getParameterTable());

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setOnSelectStateChangedHandler(onRowSelectCallback)));
        addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));
        addBeforeOption(new Option(table.setOnGridReconstructedHandler(onGridReconstructedCallback)));
        addBeforeOption(new Option(table.setOnAfterClick(onAfterClickCallback)));

        // install load error handler
        enableLoadErrorHandling();

        // Data processor
        DataProcessor dataProcessor = new DataProcessor(this, onErrorCallback);
        dataProcessor.setRowMarkFunction(onRowMarkCallback);
        setDataProcessor(dataProcessor);

        // Register callbacks
        registerCallback(onRowSelectCallback);
        registerCallback(new ShowSortImgCallback(this));
        registerCallback(onErrorCallback);
        registerCallback(new OnLoadErrorCallback(this, null, null));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(new SynchCommandCallback(this, CallbackConstants.UndoCallback, CommandConstants.UNDO));
        registerCallback(new InsertRowCallback(this));
        registerCallback(new DeleteRowCallback(this));
        registerCallback(new SynchCommandCallback(this, CallbackConstants.SaveCallback, CommandConstants.SAVE));
        registerCallback(onGridReconstructedCallback);
        registerCallback(onAfterClickCallback);
        registerCallback(onRowMarkCallback);
    }

    /**
     * Gets all rows
     * 
     * @return List with all macros
     */
    public MacroList getRows() {
        return _macroService.getMacros();
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<Macro> getExportRows(ParameterList params) {
        return getRows().getMacros();
    }

    @Override
    protected String getExportFileName() {
        return "Macros.csv";
    }

    /**
     * Intializes the macro table with all rows.
     * 
     * @param params
     *            contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        MacroListTableResultMapper resultMapper = new MacroListTableResultMapper(getRows(), getLocalizationManager());
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
        MacroListTableResultMapper resultMapper = new MacroListTableResultMapper(getRows(), getLocalizationManager());
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
            MacroResult result = _macroService.getMacroById(new Long(sid));

            // Maps result
            MacroTableResultMapper resultMapper = new MacroTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            MacroResult result = new MacroResult();
            result.setState(1);
            result.setMacro(new Macro());
            MacroTableResultMapper resultMapper = new MacroTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // merge new data record with cache
        Macro macro = (Macro) merge(params);

        MacroResult result = _macroService.updateMacro(macro, getLocalizationManager().getLanguage());

        // Maps result
        MacroTableResultMapper resultMapper = new MacroTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        Macro macro = (Macro) merge(params);

        MacroResult result = _macroService.deleteMacro(macro);

        // Maps result
        MacroTableResultMapper resultMapper = new MacroTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // Merge with an empty record
        Macro macro = (Macro) merge(new Macro(), params);

        MacroResult result = _macroService.insertMacro(macro, getLocalizationManager().getLanguage());

        // Maps result
        MacroTableResultMapper resultMapper = new MacroTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }
}
