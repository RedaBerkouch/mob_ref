/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MainTabManager.java 108 2007-06-13 15:00:51Z dwi $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.tab.TabManagerBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 * 
 * @author $Author: dwi $
 * @version $Revision: 108 $
 */
@Scope("session")
@Component("mainTabManager")
public class MainTabManager extends TabManagerBase {
    private static final Log LOGGER = LogFactory.getLog(MainTabManager.class);

    public static final String MANAGER_NAME = "mainTab";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    public MainTabManager() throws DhtmlxException {
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

    /*
     * (non-Javadoc)
     * 
     * @see ch.admin.bfs.sbg.dhtmlx.IDhtmlxManager#create()
     */
    public void create() {
        LOGGER.debug("create");
    }
}
