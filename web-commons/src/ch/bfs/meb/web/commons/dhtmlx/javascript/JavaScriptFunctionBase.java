/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import java.util.ArrayList;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.IJSType;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class JavaScriptFunctionBase implements IJavaScriptFunction {

    private String _methodName;

    private final ArrayList<IJSType> _parameters = new ArrayList<IJSType>();

    public JavaScriptFunctionBase(String methodName) {
        // Add manager name to create a unique method name
        _methodName = methodName;
    }

    public void addParameter(IJSType parameter) {
        _parameters.add(parameter);
    }

    public String getGlobals() {
        return "";
    }

    public String getScriptingPart() throws DhtmlxException {

        StringBuffer buf = new StringBuffer("\n");

        // Global variables
        buf.append(getGlobals());

        // Function
        buf.append("function ").append(_methodName).append("(");

        int i = 0;
        for (IJSType param : _parameters) {
            if (i > 0)
                buf.append(",");
            buf.append(param);
            i++;
        }

        buf.append("){");

        // Append actual code
        buf.append(getScriptingBody());

        buf.append("}");

        return buf.toString();
    }

    public String getMethodName() {
        return _methodName;
    }

    public void setMethodName(String methodeName) {
        _methodName = methodeName;
    }

    public String getMethodCall() {
        if (_parameters.size() == 0) {
            return _methodName + "()";
        }

        return null;
    }
}
