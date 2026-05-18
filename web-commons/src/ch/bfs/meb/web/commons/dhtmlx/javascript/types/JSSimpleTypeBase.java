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
public abstract class JSSimpleTypeBase extends JSType implements IJSSimpleType {

    public boolean isVal() {
        return (asRef() == null);
    }

    public String asVar() {
        if (isVal()) {
            return asVal();
        }

        return asRef();
    }

    public String toString() {
        return asVar();
    }

    /**
     * Returns the value as string
     * 
     * @return Value as string
     */
    protected abstract String asVal();

    public JSSimpleTypeBase concat(JSSimpleTypeBase other) {
        return JSString.byRef(asRef() + '+' + other.asRef());
    }
}
