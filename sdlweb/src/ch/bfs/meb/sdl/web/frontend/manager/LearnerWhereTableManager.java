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
@Component("learnerWhereTableManager")
public class LearnerWhereTableManager extends WhereTableManagerBase {
    public static final String MANAGER_NAME = "learnerWhere";

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
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_VERSION_ID)), LearnerTableManager.COLUMN_VERSION_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_CANTON_ID)), LearnerTableManager.COLUMN_CANTON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_IDTYPE_ID)), LearnerTableManager.COLUMN_IDTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_ID_ID)), LearnerTableManager.COLUMN_ID_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_DELIVERYSTATUS_ID)),
                LearnerTableManager.COLUMN_DELIVERYSTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_PLAUSISTATUS_ID)), LearnerTableManager.COLUMN_PLAUSISTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_SEX_ID)), LearnerTableManager.COLUMN_SEX_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_BIRTHDATE_ID)), LearnerTableManager.COLUMN_BIRTHDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_NATIONALITY_ID)), LearnerTableManager.COLUMN_NATIONALITY_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_LANGUAGE_ID)), LearnerTableManager.COLUMN_LANGUAGE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_RESIDENCE_ID)), LearnerTableManager.COLUMN_RESIDENCE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_HISTORIC_RESIDENCE_ID)),
                LearnerTableManager.COLUMN_HISTORIC_RESIDENCE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_COUNTRY_ID)), LearnerTableManager.COLUMN_COUNTRY_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_SCHOOLTYPE_ID)), LearnerTableManager.COLUMN_SCHOOLTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_CANTONALYEAR_ID)), LearnerTableManager.COLUMN_CANTONALYEAR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_EDUCATIONTYPE_ID)), LearnerTableManager.COLUMN_EDUCATIONTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_PLANSTATUS_ID)), LearnerTableManager.COLUMN_PLANSTATUS_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_PROFMATURA_ID)), LearnerTableManager.COLUMN_PROFMATURA_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_PREV_SCHOOLTYPE_ID)),
                LearnerTableManager.COLUMN_PREV_SCHOOLTYPE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_PREV_CANTONALYEAR_ID)),
                LearnerTableManager.COLUMN_PREV_CANTONALYEAR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_ADDITION1_ID)), LearnerTableManager.COLUMN_ADDITION1_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_ADDITION2_ID)), LearnerTableManager.COLUMN_ADDITION2_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_ADDITION3_ID)), LearnerTableManager.COLUMN_ADDITION3_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_ADDITION4_ID)), LearnerTableManager.COLUMN_ADDITION4_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_ADDITION5_ID)), LearnerTableManager.COLUMN_ADDITION5_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_CREATIONUSER_ID)), LearnerTableManager.COLUMN_CREATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_CREATIONDATE_ID)), LearnerTableManager.COLUMN_CREATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_MODIFICATIONUSER_ID)),
                LearnerTableManager.COLUMN_MODIFICATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_MODIFICATIONDATE_ID)),
                LearnerTableManager.COLUMN_MODIFICATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_PREVELATIONUSER_ID)),
                LearnerTableManager.COLUMN_PREVELATIONUSER_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_PREVELATIONDATE_ID)),
                LearnerTableManager.COLUMN_PREVELATIONDATE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(LearnerTableManager.COLUMN_USERTEXT_ID)), LearnerTableManager.COLUMN_USERTEXT_NAME_KEY);

        addColumn(combo);
    }

    protected IJavaScriptFunction createOnRowSelectCallback(TableManagerBase target) {
        return new OnRowSelectWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }

    protected IJavaScriptFunction createOnCellChangedCallback(TableManagerBase target) {
        return new OnCellChangedWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }
}
