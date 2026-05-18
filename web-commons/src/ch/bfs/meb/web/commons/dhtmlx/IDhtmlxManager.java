/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx;

import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Generic Manager interface for all dhtml controls.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IDhtmlxManager extends IDhtmlxControl {

    /**
     * Creates a new manager.
     * 
     * @throws DhtmlxException
     *             Exception wrapper for all errors
     */
    public void create() throws DhtmlxException;

    /**
     * gets the localization manager.
     * 
     * @return The localization manager zo use
     */
    public IWebLocalizationManager getLocalizationManager();

    /**
     * Returns all javascript elements (functions, initialisation code)
     * 
     * @return javascript as string
     * @throws DhtmlxException
     *             when the scripting part is invalid
     */
    public String getScriptingPart() throws DhtmlxException;

    /**
     * Gets a regestered javascript callback function
     * 
     * @param callback
     *            The name of the registered callback
     * @return The callback or null, when no callback was registered
     * @throws DhtmlxException
     *             when the requested callback was not registered by the table
     */
    public IJavaScriptFunction getRegisteredCallback(String callback) throws DhtmlxException;

    /**
     * Gets the master state of the manager
     * 
     * @return true if the manager is a master control
     */
    public boolean isMaster();

    /**
     * Sets the master state of the manager
     * 
     * @param master
     *            sets the manager maser state
     */
    public void setMaster(boolean master);

    /**
     * Determines if server sorts the data
     * 
     * @return true, if data is sorted by server call
     */
    public boolean isServerSort();

    public String getExtraHtml(String partName);

    public boolean showIfTagBody(String condition);
}
