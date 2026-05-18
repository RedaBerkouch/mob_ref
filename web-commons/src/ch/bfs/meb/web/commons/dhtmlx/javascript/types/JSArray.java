/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.javascript.types;

import java.util.ArrayList;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class JSArray extends JSSimpleTypeBase {

    protected ArrayList<String> _array;

    public JSArray(ArrayList<String> a) {
        _array = a;
    }

    private JSArray(String ref) {
        setRef(ref);
    }

    private JSArray() {
        _array = new ArrayList<String>();
    }

    public static JSArray byVal() {
        return new JSArray();
    }

    public static JSArray byVal(ArrayList<String> a) {
        return new JSArray(a);
    }

    public JSArray add(String s) {
        _array.add(s);
        return this;
    }

    public static JSArray byRef(String ref) {
        return new JSArray(ref);
    }

    public String asVal() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        int i = 0;
        for (String val : _array) {
            if (i > 0)
                buf.append(",");
            buf.append(val);
            i++;
        }
        buf.append("]");
        return buf.toString();
    }

    public JSType valueAt(int index) {
        return JSString.byRef(asRef() + "[" + index + "]");
    }

    public JSType length() {
        return JSString.byRef(asRef() + ".length");
    }

}
