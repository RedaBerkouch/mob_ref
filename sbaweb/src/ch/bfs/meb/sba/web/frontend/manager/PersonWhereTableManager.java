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
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_VERSION_ID)), PersonTableManager.COLUMN_VERSION_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_CANTON_ID)), PersonTableManager.COLUMN_CANTON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_DELIVERYCODE_ID)), PersonTableManager.COLUMN_DELIVERYCODE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_IDTYPE_ID)), PersonTableManager.COLUMN_IDTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_ID_ID)), PersonTableManager.COLUMN_ID_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_SEX_ID)), PersonTableManager.COLUMN_SEX_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_BIRTHDATE_ID)), PersonTableManager.COLUMN_BIRTHDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_RESIDENCE_ID)), PersonTableManager.COLUMN_RESIDENCE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_HISTORIC_RESIDENCE_ID)),
                PersonTableManager.COLUMN_HISTORIC_RESIDENCE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_COUNTRY_ID)), PersonTableManager.COLUMN_COUNTRY_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_DELIVERYSTATUS_ID)), PersonTableManager.COLUMN_DELIVERYSTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_PLAUSISTATUS_ID)), PersonTableManager.COLUMN_PLAUSISTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_CREATIONDATE_ID)), PersonTableManager.COLUMN_CREATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_CREATIONUSER_ID)), PersonTableManager.COLUMN_CREATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_MODIFICATIONDATE_ID)),
                PersonTableManager.COLUMN_MODIFICATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_MODIFICATIONUSER_ID)),
                PersonTableManager.COLUMN_MODIFICATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_PREVELATIONUSER_ID)),
                PersonTableManager.COLUMN_PREVELATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_PREVELATIONDATE_ID)),
                PersonTableManager.COLUMN_PREVELATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(PersonTableManager.COLUMN_USERTEXT_ID)), PersonTableManager.COLUMN_USERTEXT_NAME_KEY);

        addColumn(combo);
    }

    protected IJavaScriptFunction createOnRowSelectCallback(TableManagerBase target) {
        return new OnRowSelectWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }

    protected IJavaScriptFunction createOnCellChangedCallback(TableManagerBase target) {
        return new OnCellChangedWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }
}
