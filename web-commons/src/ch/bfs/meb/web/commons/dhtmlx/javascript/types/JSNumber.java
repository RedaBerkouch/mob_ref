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
public class JSNumber extends JSSimpleTypeBase {

    protected Byte _bnumber;
    protected Float _fnumber;
    protected Double _dnumber;
    protected Integer _inumber;

    public JSNumber(int i) {
        _inumber = i;
    }

    public JSNumber(double d) {
        _dnumber = d;
    }

    public JSNumber(float f) {
        _fnumber = f;
    }

    public JSNumber(byte b) {
        _bnumber = b;
    }

    private JSNumber(String ref) {
        setRef(ref);
    }

    public static JSNumber byVal(int i) {
        return new JSNumber(i);
    }

    public static JSNumber byVal(double d) {
        return new JSNumber(d);
    }

    public static JSNumber byVal(float f) {
        return new JSNumber(f);
    }

    public static JSNumber byVal(byte b) {
        return new JSNumber(b);
    }

    public static JSNumber byRef(String ref) {
        return new JSNumber(ref);
    }

    public String asVal() {
        if (_bnumber != null)
            return _bnumber.toString();
        if (_inumber != null)
            return _inumber.toString();
        if (_fnumber != null)
            return _fnumber.toString();
        if (_dnumber != null)
            return _dnumber.toString();

        return "0";
    }
}
