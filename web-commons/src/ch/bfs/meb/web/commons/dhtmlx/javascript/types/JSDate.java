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
public class JSDate extends JSComplexTypeBase {

    /**
     * @param object
     * @param buf
     */
    public JSDate(String object, StringBuffer buf) {
        super(object, buf);
    }

    /**
     * Creates a constructor for the wrapped table manager
     */
    // public JSDate newDate() {
    //
    // _buf.append("= new Date(");
    // _buf.append(");");
    //		
    // //return new JSDate();
    // }

    public String asVar() {
        return null;
    }

    public JSNumber valueOf() {
        return new JSNumber(0);
    }
}
