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
public class JSMath {

    private static final String name = "Math";

    public static JSNumber random() {
        // Create call
        MethodCall call = new MethodCall(name, "random");

        // Set parameters

        // return as variable
        return JSNumber.byRef(call.toRef());
    }

    public static JSNumber round(JSNumber value) {
        // Create call
        MethodCall call = new MethodCall(name, "round");

        // Set parameters
        call.param(value);

        // return as variable
        return JSNumber.byRef(call.toRef());
    }

    public static JSNumber abs(JSNumber value) {
        // Create call
        MethodCall call = new MethodCall(name, "abs");

        // Set parameters
        call.param(value);

        // return as variable
        return JSNumber.byRef(call.toRef());
    }
}
