/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.frontend.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.callback.OnCellChangedWhereCallback;
import ch.bfs.meb.web.commons.dhtmlx.callback.OnRowSelectWhereCallback;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.table.ComboColumn;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;
import ch.bfs.meb.web.commons.dhtmlx.table.WhereTableManagerBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

@Scope("session")
@Component("activityWhereTableManager")
public class ActivityWhereTableManager extends WhereTableManagerBase {
    public static final String MANAGER_NAME = "activityWhere";

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
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_VERSION_ID)), ActivityTableManager.COLUMN_VERSION_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_CANTON_ID)), ActivityTableManager.COLUMN_CANTON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_ID_ID)), ActivityTableManager.COLUMN_ID_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_SCHOOLIDTYPE_ID)), ActivityTableManager.COLUMN_SCHOOLIDTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_SCHOOLID_ID)), ActivityTableManager.COLUMN_SCHOOLID_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_NAMEBURSCHOOL_ID)),
                ActivityTableManager.COLUMN_NAMEBURSCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_IS_PUBLIC_SCHOOL_ID)),
                ActivityTableManager.COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_ID)),
                ActivityTableManager.COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_ID)),
                ActivityTableManager.COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_IS_SPECIAL_SCHOOL_ID)),
                ActivityTableManager.COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_PERSCATEGORY_ID)), ActivityTableManager.COLUMN_PERSCATEGORY_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_CONTRACTTYPE_ID)), ActivityTableManager.COLUMN_CONTRACTTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_QUALIFICATION_ID)),
                ActivityTableManager.COLUMN_QUALIFICATION_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_PENSUM_ID)), ActivityTableManager.COLUMN_PENSUM_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_FULLTIMEREF_ID)), ActivityTableManager.COLUMN_FULLTIMEREF_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_SCHOOLTYPE_ID)), ActivityTableManager.COLUMN_SCHOOLTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_DELIVERYSTATUS_ID)),
                ActivityTableManager.COLUMN_DELIVERYSTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_PLAUSISTATUS_ID)), ActivityTableManager.COLUMN_PLAUSISTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_CREATIONDATE_ID)), ActivityTableManager.COLUMN_CREATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_CREATIONUSER_ID)), ActivityTableManager.COLUMN_CREATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_MODIFICATIONDATE_ID)),
                ActivityTableManager.COLUMN_MODIFICATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_MODIFICATIONUSER_ID)),
                ActivityTableManager.COLUMN_MODIFICATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_PREVELATIONUSER_ID)),
                ActivityTableManager.COLUMN_PREVELATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_PREVELATIONDATE_ID)),
                ActivityTableManager.COLUMN_PREVELATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ActivityTableManager.COLUMN_USERTEXT_ID)), ActivityTableManager.COLUMN_USERTEXT_NAME_KEY);

        addColumn(combo);
    }

    protected IJavaScriptFunction createOnRowSelectCallback(TableManagerBase target) {
        return new OnRowSelectWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }

    protected IJavaScriptFunction createOnCellChangedCallback(TableManagerBase target) {
        return new OnCellChangedWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }
}
