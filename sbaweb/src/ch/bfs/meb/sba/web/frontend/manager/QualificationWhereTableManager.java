/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.frontend.manager;

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
@Component("qualificationWhereTableManager")
public class QualificationWhereTableManager extends WhereTableManagerBase {
    public static final String MANAGER_NAME = "qualificationWhere";

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
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_VERSION_ID)), QualificationTableManager.COLUMN_VERSION_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_CANTON_ID)), QualificationTableManager.COLUMN_CANTON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_SCHOOLIDTYPE_ID)),
                QualificationTableManager.COLUMN_SCHOOLIDTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_SCHOOLID_ID)),
                QualificationTableManager.COLUMN_SCHOOLID_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_NAMEBURSCHOOL_ID)),
                QualificationTableManager.COLUMN_NAMEBURSCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_IS_PUBLIC_SCHOOL_ID)),
                QualificationTableManager.COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_ID)),
                QualificationTableManager.COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_ID)),
                QualificationTableManager.COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_EDUCATIONTYPE_ID)),
                QualificationTableManager.COLUMN_EDUCATIONTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_EXAMTYPE_ID)),
                QualificationTableManager.COLUMN_EXAMTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_EXAMDATE_ID)),
                QualificationTableManager.COLUMN_EXAMDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_EXAMNR_ID)), QualificationTableManager.COLUMN_EXAMNR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_RESULT_ID)), QualificationTableManager.COLUMN_RESULT_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_MATURITY_LANGUAGES_ID)), QualificationTableManager.COLUMN_MATURITY_LANGUAGES_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_DELIVERYSTATUS_ID)),
                QualificationTableManager.COLUMN_DELIVERYSTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_PLAUSISTATUS_ID)),
                QualificationTableManager.COLUMN_PLAUSISTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_CREATIONDATE_ID)),
                QualificationTableManager.COLUMN_CREATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_CREATIONUSER_ID)),
                QualificationTableManager.COLUMN_CREATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_MODIFICATIONDATE_ID)),
                QualificationTableManager.COLUMN_MODIFICATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_MODIFICATIONUSER_ID)),
                QualificationTableManager.COLUMN_MODIFICATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_PREVELATIONUSER_ID)),
                QualificationTableManager.COLUMN_PREVELATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_PREVELATIONDATE_ID)),
                QualificationTableManager.COLUMN_PREVELATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(QualificationTableManager.COLUMN_USERTEXT_ID)),
                QualificationTableManager.COLUMN_USERTEXT_NAME_KEY);

        addColumn(combo);
    }

    protected IJavaScriptFunction createOnRowSelectCallback(TableManagerBase target) {
        return new OnRowSelectWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }

    protected IJavaScriptFunction createOnCellChangedCallback(TableManagerBase target) {
        return new OnCellChangedWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }
}
