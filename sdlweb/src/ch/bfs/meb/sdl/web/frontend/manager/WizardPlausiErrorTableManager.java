/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: WizardPlausiErrorTableManager.java 990 2010-03-10 10:54:30Z msc $
 */
package ch.bfs.meb.sdl.web.frontend.manager;

import java.util.ArrayList;
import java.util.List;

import ognl.OgnlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.frontend.resultmapper.WizardPlausiErrorListTableResultMapper;
import ch.bfs.meb.sdl.web.frontend.resultmapper.WizardPlausiErrorTableResultMapper;
import ch.bfs.meb.sdl.web.service.IWizardService;
import ch.bfs.meb.sdl.web.ws.sdlwizard.SdlPlausiError;
import ch.bfs.meb.sdl.web.ws.sdlwizard.SdlPlausiErrorListResult;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.COLOR;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Creates a new WizardPlausiErrorTableManager who acts as a Wizard PlausiError Table controller.
 * 
 * @author $Author: msc $
 * @version $Revision: 990 $
 */
@Scope("session")
@Component("wizardPlausiErrorTableManager")
public class WizardPlausiErrorTableManager extends TableManagerBase {
    //	private final static Logger LOGGER = LoggerFactory.getLogger (WizardDeliveryTableManager.class);

    private static final String COLUMN_ERRORID_ID = "errorId";
    private static final String COLUMN_ERRORID_NAME_KEY = "wizardPlausiErrorTable.column.errorid.name";
    private static final String COLUMN_ISCONFIRMED_ID = "isConfirmed";
    private static final String COLUMN_ISCONFIRMED_NAME_KEY = "wizardPlausiErrorTable.column.isconfirmed.name";
    private static final String COLUMN_LEARNERLABEL_ID = "learnerLabel";
    private static final String COLUMN_LEARNERLABEL_NAME_KEY = "wizardPlausiErrorTable.column.learnerlabel.name";
    private static final String COLUMN_CLASSLABEL_ID = "classLabel";
    private static final String COLUMN_CLASSLABEL_NAME_KEY = "wizardPlausiErrorTable.column.classlabel.name";
    private static final String COLUMN_SCHOOLLABEL_ID = "schoolLabel";
    private static final String COLUMN_SCHOOLLABEL_NAME_KEY = "wizardPlausiErrorTable.column.schoollabel.name";
    private static final String COLUMN_ERRORMSG_ID = "errorMsg";
    private static final String COLUMN_ERRORMSG_NAME_KEY = "wizardPlausiErrorTable.column.errormsg.name";
    private static final String COLUMN_PLAUSINAME_ID = "plausiName";
    private static final String COLUMN_PLAUSINAME_NAME_KEY = "wizardPlausiErrorTable.column.plausiname.name";

    public static final String MANAGER_NAME = "wizardPlausiError";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWizardService _wizardService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    private String _dlUser = null;
    private Long _version;
    private SdlPlausiErrorListResult _plausiErrors;
    private List<SdlPlausiError> _changedPlausiErrors;

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

    /**
     * Initializes a new DeliveryTableManager. This is a callback interface.
     * This methode is used to initialize a new Manager and should be called
     * only once.
     */
    @Override
    public void create() throws DhtmlxException {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        _dlUser = user.getEmail();

        addColumn(new IdentityColumn(COLUMN_ERRORID_ID, COLUMN_ERRORID_NAME_KEY, getLocalizationManager()));

        CheckboxColumn cbCol = new CheckboxColumn(COLUMN_ISCONFIRMED_ID, COLUMN_ISCONFIRMED_NAME_KEY, getLocalizationManager());
        cbCol.setWidth(6);
        addColumn(cbCol);

        Column col = new ReadOnlyColumn(COLUMN_LEARNERLABEL_ID, COLUMN_LEARNERLABEL_NAME_KEY, getLocalizationManager(), 13);
        col.setColor(COLOR.WHITE);
        addColumn(col);

        col = new ReadOnlyColumn(COLUMN_CLASSLABEL_ID, COLUMN_CLASSLABEL_NAME_KEY, getLocalizationManager(), 13);
        col.setColor(COLOR.WHITE);
        addColumn(col);

        col = new ReadOnlyColumn(COLUMN_SCHOOLLABEL_ID, COLUMN_SCHOOLLABEL_NAME_KEY, getLocalizationManager(), 13);
        col.setColor(COLOR.WHITE);
        addColumn(col);

        col = new ReadOnlyColumn(COLUMN_ERRORMSG_ID, COLUMN_ERRORMSG_NAME_KEY, true, getLocalizationManager(), 42);
        col.setColor(COLOR.WHITE);
        addColumn(col);

        col = new ReadOnlyColumn(COLUMN_PLAUSINAME_ID, COLUMN_PLAUSINAME_NAME_KEY, true, getLocalizationManager(), 13);
        col.setColor(COLOR.WHITE);
        addColumn(col);

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this, true);
        IJavaScriptFunction onAfterUpdateCallback = new OnAfterUpdateWizardCallback(this, CallbackConstants.ConfirmErrorsCallback);
        IJavaScriptFunction onLoadingStartCallback = new OnLoadingStartCallback(this);
        IJavaScriptFunction onLoadingEndCallback = new OnLoadingEndCallback(this, false);

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setMultiselect(JSBoolean.isfalse)));
        addBeforeOption(new Option(table.setOnLoadingStart(onLoadingStartCallback)));
        addBeforeOption(new Option(table.setOnLoadingEnd(onLoadingEndCallback)));

        // install load error handler
        enableLoadErrorHandling();

        // Data processor
        DataProcessor dataProcessor = new DataProcessor(this, onErrorCallback);
        dataProcessor.setAfterUpdateFunction(onAfterUpdateCallback);
        setDataProcessor(dataProcessor);

        // Register callbacks

        registerCallback(onErrorCallback);
        registerCallback(onAfterUpdateCallback);
        registerCallback(new OnLoadErrorCallback(this, null, null, true));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(new WizardSaveCallback(this, CallbackConstants.ConfirmErrorsCallback));
        registerCallback(new CommandAndReloadCallback(this, CallbackConstants.ConfirmErrorsCallback, CommandConstants.CONFIRM, "dlwizard.page"));
    }

    public void setData(String dlUser, Long version, SdlPlausiErrorListResult plausiErrors) {
        _dlUser = dlUser;
        _version = version;
        _plausiErrors = plausiErrors;
        _changedPlausiErrors = new ArrayList<SdlPlausiError>();
    }

    /**
     * Gets all rows for plausi error Table
     */
    public SdlPlausiErrorListResult getRows() {
        return _plausiErrors;
    }

    /**
     * Get all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<SdlPlausiError> getExportRows(ParameterList params) {
        return getRows().getPlausiErrors();
    }

    @Override
    protected String getExportFileName() {
        return "Plausierrors.csv";
    }

    /**
     * Intializes the delivery table with all rows.
     * 
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        WizardPlausiErrorListTableResultMapper resultMapper = new WizardPlausiErrorListTableResultMapper(getRows(), getLocalizationManager());

        return toXMLStream(resultMapper, true, true);
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
        WizardPlausiErrorListTableResultMapper resultMapper = new WizardPlausiErrorListTableResultMapper(getRows(), getLocalizationManager());

        return toXMLStream(resultMapper, true, false);
    }

    /**
     * Update plausiError
     * 
     * @param params
     *            contains all parameters
     * @return XML with updated row
     * @throws DhtmlxException
     */
    public DhtmlxTableDataXML update(ParameterList params) throws DhtmlxException, OgnlException {
        // Get the source id
        String sid = params.getRowId();

        // merge data record with cache, used in onAfterUpdate --> confirm
        SdlPlausiError plausiError = (SdlPlausiError) merge(params);
        _changedPlausiErrors.add(plausiError);

        // Maps result
        WizardPlausiErrorTableResultMapper resultMapper = new WizardPlausiErrorTableResultMapper(CommandConstants.UPDATE, sid, plausiError,
                getLocalizationManager());

        return toXMLDataStream(resultMapper);
    }

    public DhtmlxTableXML confirm(ParameterList params) throws DhtmlxException {
        SdlPlausiErrorListResult result;

        if (_changedPlausiErrors.size() > 0) {
            result = _wizardService.confirmErrors(_changedPlausiErrors);
            if (result.getState() == ResultBase.OK) {
                result.getPlausiErrors().clear();
            }
        } else {
            result = new SdlPlausiErrorListResult();
            result.setState(ResultBase.OK);
        }

        WizardPlausiErrorListTableResultMapper resultMapper = new WizardPlausiErrorListTableResultMapper(result, getLocalizationManager());

        return toXMLStream(resultMapper, false, false);
    }

    public String getExtraHtml(String partName) {
        if (partName.equals("dlUser")) {
            return _dlUser;
        }

        if (partName.equals("version")) {
            return "" + _version;
        }

        return "";
    }
}
