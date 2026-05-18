/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: GlobalJavascriptBase.java 305 2009-12-03 10:25:28Z msc $
 */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import java.util.HashMap;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSType;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 212 $
 */
public abstract class GlobalJavascriptBase implements IGlobalJavaScript {
    private final HashMap<String, Global> globals = new HashMap<String, Global>();

    private final HashMap<String, IJavaScriptFunction> scripts = new HashMap<String, IJavaScriptFunction>();

    public abstract void create() throws DhtmlxException;

    private class Global {
        private JSType _type;
        private JSType _value;

        public Global(JSType type, JSType value) {
            _type = type;
            _value = value;
        }

        public Global(JSType type) {
            this(type, null);
        }

        public JSType getType() {
            return _type;
        }

        @SuppressWarnings("unused")
        public void setType(JSType type) {
            _type = type;
        }

        public JSType getValue() {
            return _value;
        }

        @SuppressWarnings("unused")
        public void setValue(JSType value) {
            _value = value;
        }

        public void toJavascript(Javascript js) {
            if (getValue() == null) {
                js.define(getType());
            } else {
                js.define(getType(), getValue());
            }
        }
    }

    public JSType getGlobal(String name) {
        return ((Global) globals.get(name)).getType();
    }

    public String getGlobals() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();
        Javascript js = new Javascript(buf);
        for (Global global : globals.values()) {
            global.toJavascript(js);
        }
        return buf.toString();
    }

    public String getScripts() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();
        for (IJavaScriptFunction script : scripts.values()) {
            buf.append(script.getScriptingPart());
        }
        return buf.toString();
    }

    public void defineGlobal(JSType type, JSType value) throws DhtmlxException {
        if (type.isRef()) {
            globals.put(type.asVar(), new Global(type, value));
        } else {
            throw new DhtmlxException("Global type not a reference");
        }
    }

    public void defineGlobal(JSType type) throws DhtmlxException {
        if (type.isRef()) {
            globals.put(type.asVar(), new Global(type));
        } else {
            throw new DhtmlxException("Global type not a reference");
        }
    }

    public void defineJavaScript(IJavaScriptFunction function) {
        scripts.put(function.getMethodName(), function);
    }
}
