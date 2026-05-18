/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ExportTableManager.java 632 2010-11-22 10:10:15Z msc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import java.util.List;

import ch.bfs.meb.web.commons.dhtmlx.callback.ExportCsvCallback;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.admin.bfs.sbg.dhtmlx.table.SbgParameterColumn;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.resultmapper.MacroListTableResultMapper;
import ch.bfs.meb.sbg.web.service.IMacroService;
import ch.bfs.meb.sbg.web.ws.sbgmacro.ExportResult;
import ch.bfs.meb.sbg.web.ws.sbgmacro.Macro;
import ch.bfs.meb.sbg.web.ws.sbgmacro.MacroList;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.FileHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.IHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.callback.OnErrorCallback;
import ch.bfs.meb.web.commons.dhtmlx.callback.RunExportCallback;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.RowsDocument;
import ch.bfs.meb.web.commons.exception.MebDhtmlxFileException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Creates a new DeliveryTableManager who acts as a Delivery Table controller.
 *
 * @author $Author: msc $
 * @version $Revision: 632 $
 */
@Scope("session")
@Component("exportTableManager")
public class ExportTableManager extends TableManagerBase {
    public static final String MANAGER_NAME = "export";
    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;
    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(ExportTableManager.class);
    private static final String COLUMN_MACROID_ID = "macroid";
    private static final String COLUMN_MACROID_NAME_KEY = "exportTable.column.macroid.name";
    private static final String COLUMN_NAME_ID = "name";
    private static final String COLUMN_NAME_NAME_KEY = "exportTable.column.name.name";
    private static final String COLUMN_DESCRIPTION_ID = "description";
    private static final String COLUMN_DESCRIPTION_NAME_KEY = "exportTable.column.description.name";
    private static final String COLUMN_PARAMETER_ID = "parameters";
    private static final String COLUMN_PARAMETER_NAME_KEY = "exportTable.column.parameter.name";
    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IMacroService _macroService;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

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
     * Initializes a new DeliveryTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    public void create() throws DhtmlxException {
        addColumn(new IdentityColumn(COLUMN_MACROID_ID, COLUMN_MACROID_NAME_KEY, getLocalizationManager()));

        addColumn(new MultilanguageReadOnlyColumn(COLUMN_NAME_ID, COLUMN_NAME_NAME_KEY, getLocalizationManager(), 20));
        addColumn(new MultilanguageReadOnlyColumn(COLUMN_DESCRIPTION_ID, COLUMN_DESCRIPTION_NAME_KEY, getLocalizationManager(), 49));
        addColumn(new SbgParameterColumn(COLUMN_PARAMETER_ID, COLUMN_PARAMETER_NAME_KEY, COLUMN_MACROID_ID, getLocalizationManager()));

        enableResizeGrid();

        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this);

        // Register callbacks
        registerCallback(new ExportCsvCallback(this, null, null, false, _maintainglobals));
        registerCallback(onErrorCallback);
        registerCallback(new RunExportCallback(this));

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
    }

    /**
     * Gets all rows
     *
     * @return List with all Deliveries
     */
    public MacroList getRows() {
        return _macroService.getExportMacros();
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
     * Intializes the delivery table with all rows.
     *
     * @param params contains all parameters
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
     * @param params contains all parameters
     * @return xml with all selected rows depending on the parent table
     * selection who is in the param list
     * @throws DhtmlxException
     */
    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        MacroListTableResultMapper resultMapper = new MacroListTableResultMapper(getRows(), getLocalizationManager());
        return toXMLStream(resultMapper, true, false);
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

            // find selected macro in serialized table data
            Macro tableMacro = null;
            RowsDocument exportDocument = params.getData(ParameterConstants.PARAM_EXPORTDATA);
            List<Object> exportList = toObjectList(exportDocument, Macro.class);
            for (int i = 0; i < exportList.size(); ++i) {
                Macro m = (Macro) exportList.get(i);
                if (m.getMacroid().toString().equals(sid)) {
                    tableMacro = m;
                    break;
                }
            }

            // take the transient parameter values from serialized table - take all other attributes from loaded data
            Macro exportMacro = (Macro) getRowData(sid);

            exportMacro.getParameters().clear();
            exportMacro.getParameters().addAll(tableMacro.getParameters());

            ExportResult result = _macroService.runExport(exportMacro, getLocalizationManager().getLanguage());

            if (ResultBase.OK == result.getState()) {
                return new FileHttpResult(result.getExport(), result.getFilename());
            } else {
                return new FileHttpResult(_localizationManager.getMessage(result.getMessage()).getBytes(), "error.txt");
            }
        } catch (Exception e) {
            throw new MebDhtmlxFileException(e);
        }
    }

}
