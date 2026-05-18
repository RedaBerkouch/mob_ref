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
public class JSBoolean extends JSSimpleTypeBase {

    public static final JSBoolean istrue = new JSBoolean(true);

    public static final JSBoolean isfalse = new JSBoolean(false);

    protected boolean _boolean;

    public JSBoolean(boolean b) {
        _boolean = b;
    }

    private JSBoolean(String ref) {
        setRef(ref);
    }

    public static JSBoolean byVal(boolean b) {
        return new JSBoolean(b);
    }

    public static JSBoolean byRef(String ref) {
        return new JSBoolean(ref);
    }

    public String asVal() {
        return new Boolean(_boolean).toString();
    }
}
