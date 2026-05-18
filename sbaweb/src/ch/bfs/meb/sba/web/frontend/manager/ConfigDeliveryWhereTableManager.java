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
@Component("configDeliveryWhereTableManager")
public class ConfigDeliveryWhereTableManager extends WhereTableManagerBase {
    public static final String MANAGER_NAME = "configDeliveryWhere";

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
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_CANTON_ID)), ConfigDeliveryTableManager.COLUMN_CANTON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_DELIVERYCODE_ID)),
                ConfigDeliveryTableManager.COLUMN_DELIVERYCODE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_DEFAULT_ID)),
                ConfigDeliveryTableManager.COLUMN_DEFAULT_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_DLUSERS_ID)),
                ConfigDeliveryTableManager.COLUMN_DLUSERS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_ROUSERS_ID)),
                ConfigDeliveryTableManager.COLUMN_ROUSERS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_DUEDATE_ID)),
                ConfigDeliveryTableManager.COLUMN_DUEDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_REFERENCEDATE_ID)),
                ConfigDeliveryTableManager.COLUMN_REFERENCEDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_CREATIONDATE_ID)),
                ConfigDeliveryTableManager.COLUMN_CREATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_CREATIONUSER_ID)),
                ConfigDeliveryTableManager.COLUMN_CREATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_MODIFICATIONDATE_ID)),
                ConfigDeliveryTableManager.COLUMN_MODIFICATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_MODIFICATIONUSER_ID)),
                ConfigDeliveryTableManager.COLUMN_MODIFICATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(ConfigDeliveryTableManager.COLUMN_USERTEXT_ID)),
                ConfigDeliveryTableManager.COLUMN_USERTEXT_NAME_KEY);
        addColumn(combo);
    }

    protected IJavaScriptFunction createOnRowSelectCallback(TableManagerBase target) {
        return new OnRowSelectWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }

    protected IJavaScriptFunction createOnCellChangedCallback(TableManagerBase target) {
        return new OnCellChangedWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }
}
