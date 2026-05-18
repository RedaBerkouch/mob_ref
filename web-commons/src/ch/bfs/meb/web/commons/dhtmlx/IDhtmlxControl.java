/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id$
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.web.commons.dhtmlx;

/**
 * Interface that describes the dhtmlx div panel name (name) and javascript
 * object name (controlname)
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IDhtmlxControl {

    /**
     * The javascript object name
     * 
     * @return The name of the control used in javascript
     */
    public String getControlName();

    /**
     * The div panel reference of the control
     * 
     * @return The name of the control used by a html div panel
     */
    public String getName();
}
