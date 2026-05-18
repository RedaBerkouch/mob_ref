/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.IJSType;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class Javascript {

    // private static final Log LOGGER = LogFactory.getLog(Javascript.class);

    protected final StringBuilder _buf;

    public static String EQ = "==";
    public static final String NE = "!=";
    public static String LT = "<";
    public static final String GT = ">";
    public static String LE = "<=";
    public static String GE = ">=";
    public static String AND = "&&";
    public static String IN = "In";

    public Javascript(StringBuilder buf) {

        _buf = buf;
    }

    public Javascript ifc(IJSType condition) {

        _buf.append("if(");
        _buf.append(condition);
        _buf.append(")");

        return this;
    }

    public Javascript ifnotc(IJSType condition) {

        _buf.append("if(!");
        _buf.append(condition);
        _buf.append(")");

        return this;
    }

    public Javascript elseifc(IJSType condition) {

        _buf.append("else if(");
        _buf.append(condition);
        _buf.append(")");

        return this;
    }

    public Javascript thenc(CodeBlock code) throws DhtmlxException {

        _buf.append("{");
        code.code(_buf);
        _buf.append("}");

        return this;
    }

    public Javascript code(CodeBlock code) throws DhtmlxException {

        code.code(_buf);

        return this;
    }

    public Javascript elsec(CodeBlock code) throws DhtmlxException {

        _buf.append("else{");
        code.code(_buf);
        _buf.append("}");

        return this;
    }

    public IJSType compare(IJSType val1, String comperator, IJSType val2) {

        return JSBoolean.byRef(val1 + comperator + val2);
    }

    public IJSType concat(IJSType var, IJSType var2) {

        return JSString.byRef(var.toString() + var2.toString());
    }

    public void define(IJSType var) {

        _buf.append("var ");
        _buf.append(var.toString());
        _buf.append(";");
    }

    public void define(IJSType var, IJSType value) {

        _buf.append("var ");
        _buf.append(var.toString());
        _buf.append("=");
        _buf.append(value.toString());
        _buf.append(";");
    }

    public void assign(IJSType var, IJSType value) {

        _buf.append(var.toString());
        _buf.append("=");
        _buf.append(value.toString());
        _buf.append(";");
    }

    public Javascript append(String text) {
        _buf.append(text);
        return this;
    }

    public void incSaveNr() {
        _buf.append("if(incSaveNr!=null)incSaveNr();");
    }

    public void returnc(IJSType retval) {

        _buf.append("return ");
        _buf.append(retval);
        _buf.append(";");
    }

    public void returnc() {

        _buf.append("return ");
        _buf.append(";");
    }

    public MethodCall clearTimeout(JSNumber timeoutHnd) {
        // Create call
        MethodCall call = new MethodCall("clearTimeout");

        // Set parameters
        call.param(timeoutHnd);

        // Append
        _buf.append(call);

        return call;
    }

    public JSNumber setTimeout(IJavaScriptFunction function, JSNumber timeout) {

        // Create call
        MethodCall call = new MethodCall("setTimeout");

        // Set parameters
        call.param(function).param(timeout);

        // return as variable
        return JSNumber.byRef(call.toRef());
    }

    public MethodCall alert(JSString alert) {

        // Create call
        MethodCall call = new MethodCall("alert");

        // Set parameters
        call.param(alert.asVar());

        // Append
        _buf.append(call);

        // return as variable
        return call;
    }

    public JSBoolean confirm(JSString confirm) {
        // Create call
        MethodCall call = new MethodCall("confirm");

        // Set parameters
        call.param(confirm.asVar());

        // return as variable
        return JSBoolean.byRef(call.toRef());
    }

    public StringBuilder getBuf() {
        return _buf;
    }
}
