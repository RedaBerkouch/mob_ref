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
@Component("burSchoolWhereTableManager")
public class BurSchoolWhereTableManager extends WhereTableManagerBase {
    public static final String MANAGER_NAME = "burSchoolWhere";

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
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_CANTON_ID)), BurSchoolTableManager.COLUMN_CANTON_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_BURNR_ID)), BurSchoolTableManager.COLUMN_BURNR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_LABEL_ID)), BurSchoolTableManager.COLUMN_LABEL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_IS_PUBLIC_SCHOOL_ID)),
                BurSchoolTableManager.COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_ID)),
                BurSchoolTableManager.COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_ID)),
                BurSchoolTableManager.COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_IS_SPECIAL_SCHOOL_ID)),
                BurSchoolTableManager.COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_DELIVERY_CODE_ID)),
                BurSchoolTableManager.COLUMN_DELIVERY_CODE_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_MUNICIPALITY_ID)),
                BurSchoolTableManager.COLUMN_MUNICIPALITY_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_SYNCHSTATUSBUR_ID)),
                BurSchoolTableManager.COLUMN_SYNCHSTATUSBUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_NAME_BUR_ID)), BurSchoolTableManager.COLUMN_NAME_BUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_IS_PUBLIC_SCHOOL_BUR_ID)),
                BurSchoolTableManager.COLUMN_IS_PUBLIC_SCHOOL_BUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_BUR_ID)),
                BurSchoolTableManager.COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_BUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR_ID)),
                BurSchoolTableManager.COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_IS_SPECIAL_SCHOOL_BUR_ID)),
                BurSchoolTableManager.COLUMN_IS_SPECIAL_SCHOOL_BUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_CANTON_BUR_ID)), BurSchoolTableManager.COLUMN_CANTON_BUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_MUNICIPALITY_BUR_ID)),
                BurSchoolTableManager.COLUMN_MUNICIPALITY_BUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_VALIDFROM_BUR_ID)),
                BurSchoolTableManager.COLUMN_VALIDFROM_BUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_VALIDTO_BUR_ID)), BurSchoolTableManager.COLUMN_VALIDTO_BUR_NAME_KEY);
        combo.addComboItem(new Long(target.getColumnIndexById(BurSchoolTableManager.COLUMN_USERTEXT_ID)), BurSchoolTableManager.COLUMN_USERTEXT_NAME_KEY);
        addColumn(combo);
    }

    protected IJavaScriptFunction createOnRowSelectCallback(TableManagerBase target) {
        return new OnRowSelectWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }

    protected IJavaScriptFunction createOnCellChangedCallback(TableManagerBase target) {
        return new OnCellChangedWhereCallback(this, target, COLUMN_ATTRIBUTE_ID, COLUMN_VALUE_ID, OPERATORS);
    }
}
