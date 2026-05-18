/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: EventFilterTableManager.java 364 2007-09-18 13:16:34Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.FilterTableManagerBase;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ch.bfs.meb.web.commons.util.IFilterService;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 364 $
 */
@Scope("session")
@Component("eventFilterTableManager")
public class EventFilterTableManager extends FilterTableManagerBase {
    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(EventFilterTableManager.class);

    public static final String MANAGER_NAME = "eventFilter";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IFilterService _filterService;

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

    public WebFilterListResult getAllRows() {
        return _filterService.getFiltersForRefObject(CodegroupUtility.SBG_OBJECTTYPE_EVENT);
    }
}
