/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id$
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import java.util.ArrayList;

import ch.bfs.meb.web.commons.dhtmlx.javascript.types.IJSType;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class MethodCall {

    private final String _object;

    private final String _methodName;

    private final ArrayList<String> _params = new ArrayList<String>();

    public MethodCall(String object, String methodName) {

        _object = object;
        _methodName = methodName;
    }

    public MethodCall(String methodName) {

        this(null, methodName);
    }

    public String getObjectName() {
        return _object;
    }

    public String getMethodName() {
        return _methodName;
    }

    public ArrayList<String> getParams() {
        return _params;
    }

    public MethodCall param(String param) {
        _params.add(param);
        return this;
    }

    public MethodCall param(boolean param) {
        _params.add(param ? "true" : "false");
        return this;
    }

    public MethodCall param(Command param) {
        _params.add(param.toString());
        return this;
    }

    public MethodCall param(IJavaScriptFunction param) {
        _params.add(param.getMethodName());
        return this;
    }

    public MethodCall param(int param) {
        _params.add(new Integer(param).toString());
        return this;
    }

    public MethodCall param(IJSType param) {
        _params.add(param.asVar());
        return this;
    }

    public MethodCall qparam(String param) {
        _params.add('"' + param + '"');
        return this;
    }

    /**
     * Method without ";"
     * 
     * @return
     */
    public String toRef() {

        StringBuffer buf = new StringBuffer();

        if (_object != null) {
            buf.append(_object);
            buf.append(".");
        }
        buf.append(_methodName);
        buf.append("(");

        int i = 0;
        for (String param : _params) {
            if (i > 0)
                buf.append(",");
            buf.append(param);
            i++;
        }

        buf.append(")");

        return buf.toString();
    }

    public String toString() {

        StringBuffer buf = new StringBuffer();

        buf.append(toRef());
        buf.append(";");

        return buf.toString();
    }
}
