/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: WizardDeliveryTableManager.java 990 2010-03-10 10:54:30Z msc $
 */
package ch.bfs.meb.sba.web.frontend.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.web.frontend.resultmapper.WizardSchoolListTableResultMapper;
import ch.bfs.meb.sba.web.service.IWizardService;
import ch.bfs.meb.sba.web.ws.sbawizard.*;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.FileHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.callback.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.*;
import ch.bfs.meb.web.commons.dhtmlx.table.Column.COLOR;
import ch.bfs.meb.web.commons.exception.MebDhtmlxFileException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ch.bfs.meb.web.commons.util.FilterContextUtility;
import ch.bfs.meb.web.commons.util.IFilterService;

/**
 * Creates a new WizardDeliveryTableManager who acts as a Wizard Delivery Table controller.
 * 
 * @author $Author: msc $
 * @version $Revision: 990 $
 */
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component("wizardDeliveryTableManager")
public class WizardDeliveryTableManager extends TableManagerBase {
    //	private final static Logger LOGGER = LoggerFactory.getLogger (WizardDeliveryTableManager.class);

    private static final String COLUMN_SCHOOLID_ID = "schoolId";
    private static final String COLUMN_SCHOOLID_NAME_KEY = "wizardDeliveryTable.column.schoolid.name";
    private static final String COLUMN_ICON_ID = "icon";
    private static final String COLUMN_ICON_NAME_KEY = "wizardDeliveryTable.column.icon.name";
    private static final String COLUMN_SCHOOLNAME_ID = "schoolName";
    private static final String COLUMN_SCHOOLNAME_NAME_KEY = "wizardDeliveryTable.column.schoolname.name";
    private static final String COLUMN_NR_OF_QUALIFICATIONS_ID = "nrOfQualifications";
    private static final String COLUMN_NR_OF_QUALIFICATIONS_NAME_KEY = "wizardDeliveryTable.column.nrOfQualifications.name";
    private static final String COLUMN_NR_OF_QUALIFICATIONS_TEXT_KEY = "wizardDeliveryTable.column.nrOfQualifications.text";
    private static final String WIZARD_DELIVERY_DELETE_CONFIRM_MESSAGE = "wizard.delivery.delete.confirm.message";

    public static final String MANAGER_NAME = "wizardDelivery";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWizardService _wizardService;

    @Autowired
    private IFilterService _filterService;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private WizardPlausiErrorTableManager _plausiErrorTableManager;

    private String _dlUser = null;
    private Long _version = null;
    private SbaPlausiErrorListResult _plausiErrors;
    private boolean _allSchoolsDelivered;
    private boolean _deliveriesValidated;
    private boolean _deliveriesValidatedConflict;
    private WizardNrOfColumn _nrOfColumn;

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
        _version = FilterContextUtility.getActVersion(_filterService, CodegroupUtility.SBA_OBJECTTYPE_CONFIGURATION);
        if (user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            List<String> userNames = _wizardService.getDlUserNames(_version).getUserNames();
            _dlUser = userNames.size() == 0 ? "" : userNames.get(0);
        } else {
            _dlUser = user.getEmail();
        }

        addColumn(new IdentityColumn(COLUMN_SCHOOLID_ID, COLUMN_SCHOOLID_NAME_KEY, getLocalizationManager()));

        addColumn(new WizardImageColumn(COLUMN_ICON_ID, COLUMN_ICON_NAME_KEY, COLUMN_NR_OF_QUALIFICATIONS_ID, getLocalizationManager(), 5));

        Column col = new ReadOnlyColumn(COLUMN_SCHOOLNAME_ID, COLUMN_SCHOOLNAME_NAME_KEY, getLocalizationManager(), 35);
        col.setColor(COLOR.WHITE);
        addColumn(col);

        _nrOfColumn = new WizardNrOfColumn(COLUMN_NR_OF_QUALIFICATIONS_ID, COLUMN_NR_OF_QUALIFICATIONS_NAME_KEY, COLUMN_NR_OF_QUALIFICATIONS_TEXT_KEY,
                getLocalizationManager(), 60);
        addColumn(_nrOfColumn);

        enableResizeGrid();

        // Callbacks
        IJavaScriptFunction onErrorCallback = new OnErrorCallback(this, true);
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
        setDataProcessor(dataProcessor);

        // Register callbacks

        registerCallback(onErrorCallback);
        registerCallback(new OnLoadErrorCallback(this, null, null, true));
        registerCallback(onLoadingStartCallback);
        registerCallback(onLoadingEndCallback);
        registerCallback(
                new CommandAndReloadCallback(this, CallbackConstants.ChangeDlUserCallback, CommandConstants.CHANGE_DL_USER, "dlUser", "dlwizard.page"));
        registerCallback(new CommandAndReloadCallback(this, CallbackConstants.DeleteCallback, CommandConstants.DELETE, "dlwizard.page", true,
                WIZARD_DELIVERY_DELETE_CONFIRM_MESSAGE));
        registerCallback(new ShowWizardPlausireportCallback(this));
        registerCallback(new CommandAndReloadCallback(this, CallbackConstants.ConfirmErrorsCallback, CommandConstants.CONFIRM, "dlconfirm.page", true));
        registerCallback(new CommandAndReloadCallback(this, CallbackConstants.ValidateCallback, CommandConstants.VALIDATE, "dlwizard.page", true));
    }

    /**
     * Gets all rows for school Table
     */
    public SbaWizardSchoolListResult getRows() {
        _allSchoolsDelivered = true;
        SbaWizardSchoolListResult schools = _wizardService.getSchools(_dlUser, _version);
        for (SbaWizardSchool school : schools.getSchools()) {
            if (school.getNrOfQualifications() == 0) {
                _allSchoolsDelivered = false;
                break;
            }
        }
        _nrOfColumn.setNrOfPersons(schools.getNrOfPersons());
        return schools;
    }

    /**
     * Get all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<SbaWizardSchool> getExportRows(ParameterList params) {
        return getRows().getSchools();
    }

    @Override
    protected String getExportFileName() {
        return "Schools.csv";
    }

    public String getDlUser() {
        return _dlUser;
    }

    public Long getVersion() {
        return _version;
    }

    /**
     * Intializes the delivery table with all rows.
     * 
     * @param params contains all parameters
     * @return xml with all rows
     * @throws DhtmlxException
     */
    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        WizardSchoolListTableResultMapper resultMapper = new WizardSchoolListTableResultMapper(getRows(), getLocalizationManager());

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
        WizardSchoolListTableResultMapper resultMapper = new WizardSchoolListTableResultMapper(getRows(), getLocalizationManager());

        return toXMLStream(resultMapper, true, false);
    }

    protected DhtmlxTableXML toEmptyXMLStream(boolean ok, String message) throws DhtmlxException {
        SbaWizardSchoolListResult result = new SbaWizardSchoolListResult();
        result.setMessage(message);
        result.setState(ok ? ResultBase.OK : ResultBase.FAILURE);
        WizardSchoolListTableResultMapper resultMapper = new WizardSchoolListTableResultMapper(result, getLocalizationManager());
        return toXMLStream(resultMapper, false, false);
    }

    public DhtmlxTableXML changeDlUser(ParameterList params) throws DhtmlxException {
        _dlUser = params.getParameter("dlUser");
        return toEmptyXMLStream(true, null);
    }

    /**
     * Shows last plausi report if available
     * 
     * @param params contains all parameters
     * @return xml with all rows 
     * @throws DhtmlxException
     */
    public FileHttpResult showLastPlausireport(ParameterList params) throws DhtmlxException {
        try {
            FileResult result = _wizardService.getPlausireport(_dlUser, _version, getLocalizationManager().getLanguage());

            return new FileHttpResult(result.getBinaryFile(), "PlausiReport.xlsx");
        } catch (Exception e) {
            throw new MebDhtmlxFileException(e);
        }
    }

    public DhtmlxTableXML delete(ParameterList params) throws DhtmlxException {
        SbaDeliveryListResult result = _wizardService.deleteDeliveries(_dlUser, _version);

        if (result.getMessage() == null || result.getMessage().equals("")) {
            return toEmptyXMLStream(true, null);
        } else {
            return toEmptyXMLStream(false, result.getMessage());
        }
    }

    public DhtmlxTableXML validate(ParameterList params) throws DhtmlxException {

        SbaDeliveryListResult result = _wizardService.validateDeliveries(_dlUser, _version, getLocalizationManager().getLanguage());

        if (result.getMessage() == null || result.getMessage().equals("")) {
            return toEmptyXMLStream(true, null);
        } else {
            return toEmptyXMLStream(false, result.getMessage());
        }
    }

    public DhtmlxTableXML confirm(ParameterList params) throws DhtmlxException {
        _plausiErrorTableManager.setData(_dlUser, _version, _plausiErrors);

        return toEmptyXMLStream(true, null);
    }

    public int getNrOfErrors(boolean confirmable) {
        int nrOfErrors = 0;

        if (_plausiErrors != null && _plausiErrors.getPlausiErrors() != null) {
            for (SbaPlausiError plausiError : _plausiErrors.getPlausiErrors()) {
                if (confirmable && plausiError.isConfirmable() && !plausiError.isIsConfirmed()) {
                    ++nrOfErrors;
                } else if (!confirmable && !plausiError.isConfirmable()) {
                    ++nrOfErrors;
                }
            }
        }

        return nrOfErrors;
    }

    public boolean hasErrors(boolean confirmable) {
        if (_plausiErrors != null && _plausiErrors.getPlausiErrors() != null) {
            for (SbaPlausiError plausiError : _plausiErrors.getPlausiErrors()) {
                if (confirmable && plausiError.isConfirmable()) {
                    return true;
                } else if (!confirmable && !plausiError.isConfirmable()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean canValidate() {
        return _allSchoolsDelivered && !hasErrors(false) && getNrOfErrors(true) == 0;
    }

    public boolean isValidated() {
        return _deliveriesValidated;
    }

    public String getExtraHtml(String partName) {
        if (partName.equals("init")) {
            getRows();
            _plausiErrors = _wizardService.getErrors(_dlUser, _version);
            Boolean validated = _wizardService.areDeliveriesValidated(_dlUser, _version);
            _deliveriesValidated = validated == null || validated.booleanValue();
            _deliveriesValidatedConflict = validated == null || (validated.booleanValue() && !_allSchoolsDelivered);

            return "";
        }

        if (partName.equals("nrOfNonconfirmableErrors")) {
            return "" + getNrOfErrors(false);
        }

        if (partName.equals("nrOfConfirmableErrors")) {
            return "" + getNrOfErrors(true);
        }

        if (partName.equals("dlUser")) {
            return _dlUser;
        }

        if (partName.equals("version")) {
            return "" + _version;
        }

        if (partName.equals("dlCombo")) {
            try {
                String s = "";
                UserNameListResult userNameResult = _wizardService.getDlUserNames(_version);
                for (String userName : userNameResult.getUserNames()) {
                    String selected = userName.toLowerCase().equals(_dlUser.toLowerCase()) ? " selected" : "";
                    s += "<option value=\"" + userName + "\"" + selected + ">" + userName + "</option>";
                }
                return s;
            } catch (Exception e) {
                return "<option value=\"" + _dlUser + "\">" + _dlUser + "</option>";
            }
        }

        if (partName.equals("disabledValidated")) {
            return _deliveriesValidated ? "disabled" : "";
        }

        return "";
    }

    public boolean showIfTagBody(String condition) {
        if (condition.equals("hasNonconfirmableAndConfirmableErrors")) {
            return hasErrors(false) && hasErrors(true);
        }

        if (condition.equals("hasNonconfirmableErrors")) {
            return hasErrors(false) && !hasErrors(true);
        }

        if (condition.equals("hasConfirmableErrors")) {
            return !hasErrors(false) && hasErrors(true) && getNrOfErrors(true) > 0;
        }

        if (condition.equals("hasConfirmableErrorsAllConfirmed")) {
            return !hasErrors(false) && hasErrors(true) && getNrOfErrors(true) == 0;
        }

        if (condition.equals("hasNoErrors")) {
            return !hasErrors(false) && !hasErrors(true);
        }

        if (condition.equals("validateConflict")) {
            return _deliveriesValidatedConflict;
        }

        if (condition.equals("noValidate")) {
            return !canValidate();
        }

        if (condition.equals("isNotValidated")) {
            return canValidate() && !_deliveriesValidated;
        }

        if (condition.equals("isValidated")) {
            return canValidate() && _deliveriesValidated;
        }

        return false;
    }
    // 🔥 utilisé par le REST controller
    public void setDlUser(String dlUser) {
        if (dlUser != null && !dlUser.isEmpty()) {
            this._dlUser = dlUser;
        }
    }

    // 🔥 indispensable si Angular change la version
    public void setVersion(Long version) {
        if (version != null) {
            this._version = version;
        }
    }

}
