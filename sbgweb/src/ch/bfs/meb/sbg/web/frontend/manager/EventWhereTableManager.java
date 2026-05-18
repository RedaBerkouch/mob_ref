/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: EventWhereTableManager.java 630 2010-11-17 13:51:29Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.callback.OnCellChangedWhereCallback;
import ch.bfs.meb.web.commons.dhtmlx.callback.OnRowSelectWhereCallback;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.table.ComboColumn;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;
import ch.bfs.meb.web.commons.dhtmlx.table.WhereTableManagerBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 630 $
 */
@Scope("session")
@Component("eventWhereTableManager")
public class EventWhereTableManager extends WhereTableManagerBase {
    public static final String MANAGER_NAME = "eventWhere";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

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

    protected void addAttributeColumn(TableManagerBase target) throws DhtmlxException {
        ComboColumn combo = new ComboColumn(COLUMN_ATTRIBUTE_ID, COLUMN_ATTRIBUTE_NAME_KEY, getLocalizationManager(), 32);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_CANTON_ID)), EventTableManager.COLUMN_CANTON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_VERSION_ID)), EventTableManager.COLUMN_VERSION_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_TYPE_ID)), EventTableManager.COLUMN_TYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_SBFICODE_ID)), EventTableManager.COLUMN_SBFICODE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_CONTRACTNO_ID)), EventTableManager.COLUMN_CONTRACTNO_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_PROFESSIONCODE_ID)), EventTableManager.COLUMN_PROFESSIONCODE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_KEYASPECT_ID)), EventTableManager.COLUMN_KEYASPECT_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_EDUCATIONYEAR_ID)), EventTableManager.COLUMN_EDUCATIONYEAR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_CONTRACTTYPE_ID)), EventTableManager.COLUMN_CONTRACTTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_CONTRACTDATE_ID)), EventTableManager.COLUMN_CONTRACTDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_EXAMTYPE_ID)), EventTableManager.COLUMN_EXAMTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_EXAMNR_ID)), EventTableManager.COLUMN_EXAMNR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_EXAMREP_ID)), EventTableManager.COLUMN_EXAMREP_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_EXAMRESULT_ID)), EventTableManager.COLUMN_EXAMRESULT_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_CANCELREASON_ID)), EventTableManager.COLUMN_CANCELREASON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_CANCELDATE_ID)), EventTableManager.COLUMN_CANCELDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_BURNR_ID)), EventTableManager.COLUMN_BURNR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_PLAUSISTATUS_ID)), EventTableManager.COLUMN_PLAUSISTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_MODUSER_ID)), EventTableManager.COLUMN_MODUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_MODDATE_ID)), EventTableManager.COLUMN_MODDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_USERCOMMENT_ID)), EventTableManager.COLUMN_USERCOMMENT_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_KANTLBCODE_ID)), EventTableManager.COLUMN_KANTLBCODE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_FIRSTNAME_ID)), EventTableManager.COLUMN_FIRSTNAME_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_FIRMSTREET_ID)), EventTableManager.COLUMN_FIRMSTREET_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_FIRMSTREETNO_ID)), EventTableManager.COLUMN_FIRMSTREETNO_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_FIRMPLZ_ID)), EventTableManager.COLUMN_FIRMPLZ_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_FIRMMUNICIPAL_ID)), EventTableManager.COLUMN_FIRMMUNICIPAL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(EventTableManager.COLUMN_FLAGLBV_ID)), EventTableManager.COLUMN_FLAGLBV_NAME_KEY);
        addColumn(combo);
    }

    protected IJavaScriptFunction createOnRowSelectCallback(TableManagerBase target) {
        return new OnRowSelectWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }

    protected IJavaScriptFunction createOnCellChangedCallback(TableManagerBase target) {
        OnCellChangedWhereCallback onCellChangedWhereCallback = new OnCellChangedWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
        onCellChangedWhereCallback.setCodeGroup(CodegroupUtility.SBG_PLAUSISTATUS);
        return onCellChangedWhereCallback;
    }
}
