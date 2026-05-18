/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PersonWhereTableManager.java 583 2009-09-07 08:50:51Z dzw $
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
 * @version $Revision: 583 $
 */
@Scope("session")
@Component("personWhereTableManager")
public class PersonWhereTableManager extends WhereTableManagerBase {
    public static final String MANAGER_NAME = "personWhere";

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
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_CANTON_ID)), PersonTableManager.COLUMN_CANTON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_VERSION_ID)), PersonTableManager.COLUMN_VERSION_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_IDTYPE_ID)), PersonTableManager.COLUMN_IDTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_ID_ID)), PersonTableManager.COLUMN_ID_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_SEX_ID)), PersonTableManager.COLUMN_SEX_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_BIRTHDATE_ID)), PersonTableManager.COLUMN_BIRTHDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_NEWBIRTHDATE_ID)), PersonTableManager.COLUMN_NEWBIRTHDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_USERCOMMENT_ID)), PersonTableManager.COLUMN_USERCOMMENT_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_PLAUSISTATUS_ID)), PersonTableManager.COLUMN_PLAUSISTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_STATUS_ID)), PersonTableManager.COLUMN_STATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_MODUSER_ID)), PersonTableManager.COLUMN_MODUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_MODDATE_ID)), PersonTableManager.COLUMN_MODDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_VALIDUSER_ID)), PersonTableManager.COLUMN_VALIDUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_VALIDDATE_ID)), PersonTableManager.COLUMN_VALIDDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_ORIGINTEXT_ID)), PersonTableManager.COLUMN_ORIGINTEXT_NAME_KEY);
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
