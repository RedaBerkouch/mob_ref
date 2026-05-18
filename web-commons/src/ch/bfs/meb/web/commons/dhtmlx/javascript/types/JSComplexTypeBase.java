/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id$
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.web.commons.dhtmlx.javascript.types;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class JSComplexTypeBase extends JSType implements IJSComplexType {

    protected final String _object;

    protected final StringBuffer _buf;

    public JSComplexTypeBase(String object, StringBuffer buf) {

        _object = object;
        _buf = buf;
    }
}
