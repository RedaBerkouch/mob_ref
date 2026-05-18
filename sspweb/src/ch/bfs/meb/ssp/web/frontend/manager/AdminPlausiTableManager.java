/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb
 */
package ch.bfs.meb.ssp.web.frontend.manager;

import java.util.List;

import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.ssp.web.frontend.resultmapper.PlausiListTableResultMapper;
import ch.bfs.meb.ssp.web.frontend.resultmapper.PlausiTableResultMapper;
import ch.bfs.meb.ssp.web.service.IPlausiService;
import ch.bfs.meb.ssp.web.ws.sspplausi.Plausi;
import ch.bfs.meb.ssp.web.ws.sspplausi.PlausiListResult;
import ch.bfs.meb.ssp.web.ws.sspplausi.PlausiResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandDispatcher.EDIT;
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
 * This Class represents a PlausiTableManager for the admin tab and acts as a
 * controller for the Plausi Table.
 */
@Scope("session")
@Component("adminPlausiTableManager")
public class AdminPlausiTableManager extends TableManagerBase {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(AdminPlausiTableManager.class);

    public static final String COLUMN_PLAUSI_ID = "plausiId";
    public static final String COLUMN_PLAUSI_NAME_KEY = "adminPlausiTable.column.id.name";
    public static final String COLUMN_SORT_ORDER_ID = "plausiOrder";
    public static final String COLUMN_SORT_ORDER_NAME_KEY = "adminPlausiTable.column.order.name";
    public static final String COLUMN_ID_ID = "id";
    public static final String COLUMN_ID_NAME_KEY = "adminPlausiTable.column.id.name";
    public static final String COLUMN_NAME_DE_ID = "nameDe";
    public static final String COLUMN_NAME_DE_NAME_KEY = "adminPlausiTable.column.name.german.name";
    public static final String COLUMN_NAME_FR_ID = "nameFr";
    public static final String COLUMN_NAME_FR_NAME_KEY = "adminPlausiTable.column.name.french.name";
    public static final String COLUMN_NAME_IT_ID = "nameIt";
    public static final String COLUMN_NAME_IT_NAME_KEY = "adminPlausiTable.column.name.italian.name";
    public static final String COLUMN_TYPE_ID = "type";
    public static final String COLUMN_TYPE_NAME_KEY = "adminPlausiTable.column.type.name";
    public static final String COLUMN_OBJECT_ID = "objectLevel";
    public static final String COLUMN_OBJECT_NAME_KEY = "adminPlausiTable.column.object.name";
    public static final String COLUMN_ACTIVE_ID = "isActive";
    public static final String COLUMN_ACTIVE_NAME_KEY = "adminPlausiTable.column.active.name";
    public static final String COLUMN_CONFIRMABLE_ID = "isConfirmable";
    public static final String COLUMN_CONFIRMABLE_NAME_KEY = "adminPlausiTable.column.confirmable.name";
    public static final String COLUMN_VALIDFROM_ID = "validFrom";
    public static final String COLUMN_VALIDFROM_NAME_KEY = "adminPlausiTable.column.validFrom.name";
    public static final String COLUMN_VALIDTO_ID = "validTo";
    public static final String COLUMN_VALIDTO_NAME_KEY = "adminPlausiTable.column.validTo.name";
    public static final String COLUMN_DESCRIPTION_DE_ID = "descriptionDe";
    public static final String COLUMN_DESCRIPTION_DE_NAME_KEY = "adminPlausiTable.column.description.german.name";
    public static final String COLUMN_DESCRIPTION_FR_ID = "descriptionFr";
    public static final String COLUMN_DESCRIPTION_FR_NAME_KEY = "adminPlausiTable.column.description.french.name";
    public static final String COLUMN_DESCRIPTION_IT_ID = "descriptionIt";
    public static final String COLUMN_DESCRIPTION_IT_NAME_KEY = "adminPlausiTable.column.description.italian.name";
    public static final String COLUMN_SOURCE_ID = "source";
    public static final String COLUMN_SOURCE_NAME_KEY = "adminPlausiTable.column.source.name";

    public static final String MANAGER_NAME = "adminPlausi";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IPlausiService _plausiService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IGlobalJavaScript _maintainglobals;

    private IDhtmlxControl getParameterTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return PlausiParamTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return PlausiParamTableManager.MANAGER_NAME;
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
     * Initializes a new AdminPlausiTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and is called only once.
     */
    @Override
    public void create() throws DhtmlxException {
        setMaster(true);

        addColumn(new IdentityColumn(COLUMN_PLAUSI_ID, COLUMN_PLAUSI_NAME_KEY, getLocalizationManager()));

        addColumn(new Column(COLUMN_SORT_ORDER_ID, COLUMN_SORT_ORDER_NAME_KEY, getLocalizationManager(), 4));
        addColumn(new Column(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager(), 3));
        addColumn(new Column(COLUMN_NAME_DE_ID, COLUMN_NAME_DE_NAME_KEY, getLocalizationManager(), 8));
        addColumn(new Column(COLUMN_NAME_FR_ID, COLUMN_NAME_FR_NAME_KEY, getLocalizationManager(), 8));
        addColumn(new Column(COLUMN_NAME_IT_ID, COLUMN_NAME_IT_NAME_KEY, getLocalizationManager(), 8));

        ComboCodeGroupColumn typeColumn = new ComboCodeGroupColumn(COLUMN_TYPE_ID, COLUMN_TYPE_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.MEB_PLAUSITYPE, 6);
        // sort bei code not text ok 
        // typeColumn.setSort(SORT.NO_SORT);
        addColumn(typeColumn);

        ComboCodeGroupColumn objectColumn = new ComboCodeGroupColumn(COLUMN_OBJECT_ID, COLUMN_OBJECT_NAME_KEY, getLocalizationManager(),
                CodegroupUtility.SSP_OBJECTTYPE, 6);
        // objectColumn.setSort(SORT.NO_SORT);
        addColumn(objectColumn);

        addColumn(new CheckboxColumn(COLUMN_ACTIVE_ID, COLUMN_ACTIVE_NAME_KEY, getLocalizationManager()));
        addColumn(new CheckboxColumn(COLUMN_CONFIRMABLE_ID, COLUMN_CONFIRMABLE_NAME_KEY, getLocalizationManager()));

        Column validColumn = new Column(COLUMN_VALIDFROM_ID, COLUMN_VALIDFROM_NAME_KEY, getLocalizationManager(), 4);
        validColumn.setSort(Column.SORT.INT);
        addColumn(validColumn);
        validColumn = new Column(COLUMN_VALIDTO_ID, COLUMN_VALIDTO_NAME_KEY, getLocalizationManager(), 4);
        validColumn.setSort(Column.SORT.INT);
        addColumn(validColumn);

        addColumn(new Column(COLUMN_SOURCE_ID, COLUMN_SOURCE_NAME_KEY, getLocalizationManager(), 38));

        addColumn(new Column(COLUMN_DESCRIPTION_DE_ID, COLUMN_DESCRIPTION_DE_NAME_KEY, getLocalizationManager(), 16));
        addColumn(new Column(COLUMN_DESCRIPTION_FR_ID, COLUMN_DESCRIPTION_FR_NAME_KEY, getLocalizationManager(), 16));
        addColumn(new Column(COLUMN_DESCRIPTION_IT_ID, COLUMN_DESCRIPTION_IT_NAME_KEY, getLocalizationManager(), 16));

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
     * @return List with all plausis
     */
    private PlausiListResult getRows() {

        return _plausiService.getPlausis();
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<Plausi> getExportRows(ParameterList params) {
        return getRows().getPlausis();
    }

    /**
     * Intializes the plausi table with all rows.
     * 
     * @param params
     *            contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        PlausiListTableResultMapper resultMapper = new PlausiListTableResultMapper(getRows(), getLocalizationManager());
        ;

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
        PlausiListTableResultMapper resultMapper = new PlausiListTableResultMapper(getRows(), getLocalizationManager());
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
            PlausiResult result = _plausiService.getPlausiById(new Long(sid));

            // Maps result
            PlausiTableResultMapper resultMapper = new PlausiTableResultMapper(CommandConstants.UNDO, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        } else {
            // Maps result
            PlausiResult result = new PlausiResult();
            result.setState(ResultBase.OK);
            result.setPlausi(new Plausi());
            PlausiTableResultMapper resultMapper = new PlausiTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

            return toXMLDataStream(resultMapper);
        }
    }

    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // merge new data record with cache
        Plausi plausi = (Plausi) merge(params);

        PlausiResult result = _plausiService.updatePlausi(plausi);

        // Maps result
        PlausiTableResultMapper resultMapper = new PlausiTableResultMapper(CommandConstants.UPDATE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML delete(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        Plausi plausi = (Plausi) merge(params);

        PlausiResult result = _plausiService.deletePlausi(plausi);

        // Maps result
        PlausiTableResultMapper resultMapper = new PlausiTableResultMapper(CommandConstants.DELETE, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableDataXML insert(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // Merge with an empty record
        Plausi plausi = (Plausi) merge(new Plausi(), params);

        PlausiResult result = _plausiService.insertPlausi(plausi);

        // Maps result
        PlausiTableResultMapper resultMapper = new PlausiTableResultMapper(CommandConstants.INSERT, sid, result, getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

}
