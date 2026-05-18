/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: FilterTabmanager.java 158 2007-06-20 10:05:55Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.tab.DhtmlxXMLBase;
import ch.bfs.meb.web.commons.dhtmlx.tab.TabManagerBase;
import ch.bfs.meb.web.commons.dhtmlx.tab.TabX;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterList;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 * 
 * @author $Author: lsc $
 * @version $Revision: 158 $
 */
@Scope("session")
@Component("filterTabmanager")
public class FilterTabmanager extends TabManagerBase {
    private static final String TAB_PERSONS_ID = "x1";
    private static final String TAB_PERSONS_NAME_KEY = "filterTabManagerTable.tab.persons.name";
    private static final String TAB_EVENTS_ID = "x2";
    private static final String TAB_EVENTS_NAME_KEY = "filterTabManagerTable.tab.events.name";

    public static final String MANAGER_NAME = "filterTab";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    public FilterTabmanager() throws DhtmlxException {
        super();
    }

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

    public void create() {
        setTabPosition(TABPOSTION.BOTTOM);

        TabX personTab = new TabX(TAB_PERSONS_ID, TAB_PERSONS_NAME_KEY, getLocalizationManager(), "tab1");
        personTab.setSelected(true);
        addTab(personTab);

        TabX eventTab = new TabX(TAB_EVENTS_ID, TAB_EVENTS_NAME_KEY, getLocalizationManager(), "tab2");
        addTab(eventTab);

    }

    public DhtmlxXMLBase init(ParameterList paramList) throws DhtmlxException {
        return toXMLStream();
    }
}
