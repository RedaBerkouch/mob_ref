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

import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class JSString extends JSSimpleTypeBase {

    protected String _s;

    public JSString(String s) {
        _s = s;
    }

    public static JSString byVal(String s) {
        return new JSString(s);
    }

    public static JSString byRef(String ref) {
        JSString v = new JSString(null);
        v.setRef(ref);
        return v;
    }

    public String asVal() {
        if (_s == null) {
            _s = "";
        }
        return '"' + _s + '"';
    }

    public JSString toLowerCase() {
        // Create call
        MethodCall call = new MethodCall(asRef(), "toLowerCase");

        // Set parameters

        // return as variable
        return JSString.byRef(call.toRef());
    }

    public JSString toUpperCase() {
        // Create call
        MethodCall call = new MethodCall(asRef(), "toUpperCase");

        // Set parameters

        // return as variable
        return JSString.byRef(call.toRef());
    }

    public JSString substr(JSNumber start, JSNumber end) {
        MethodCall call = new MethodCall(asRef(), "substring");
        call.param(start).param(end);

        return JSString.byRef(call.toRef());
    }

}
