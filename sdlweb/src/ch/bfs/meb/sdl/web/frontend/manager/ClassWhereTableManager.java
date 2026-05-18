/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.frontend.manager;

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
@Component("classWhereTableManager")
public class ClassWhereTableManager extends WhereTableManagerBase {
    public static final String MANAGER_NAME = "classWhere";

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
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_ID_ID)), ClassTableManager.COLUMN_ID_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_CANTON_ID)), ClassTableManager.COLUMN_CANTON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_VERSION_ID)), ClassTableManager.COLUMN_VERSION_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_SCHOOLTYPE_ID)), ClassTableManager.COLUMN_SCHOOLTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_DELIVERYSTATUS_ID)), ClassTableManager.COLUMN_DELIVERYSTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_PLAUSISTATUS_ID)), ClassTableManager.COLUMN_PLAUSISTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_CREATIONDATE_ID)), ClassTableManager.COLUMN_CREATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_CREATIONUSER_ID)), ClassTableManager.COLUMN_CREATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_MODIFICATIONDATE_ID)),
                ClassTableManager.COLUMN_MODIFICATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_MODIFICATIONUSER_ID)),
                ClassTableManager.COLUMN_MODIFICATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_PREVELATIONUSER_ID)), ClassTableManager.COLUMN_PREVELATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_PREVELATIONDATE_ID)), ClassTableManager.COLUMN_PREVELATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ClassTableManager.COLUMN_USERTEXT_ID)), ClassTableManager.COLUMN_USERTEXT_NAME_KEY);

        addColumn(combo);
    }

    protected IJavaScriptFunction createOnRowSelectCallback(TableManagerBase target) {
        return new OnRowSelectWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }

    protected IJavaScriptFunction createOnCellChangedCallback(TableManagerBase target) {
        return new OnCellChangedWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }
}
