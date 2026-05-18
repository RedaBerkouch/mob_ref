/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.HashMap;
import java.util.Iterator;

import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DataProcessor implements IDhtmlxControl {

    public static class UPDATEMODE {

        public static final String MANUAL = "manual";

        public static String CELL = "cell";

        public static String ROW = "row";
    }

    public static class TRANSACTIONMODE {

        public static final String POST = "POST";

        public static String GET = "GET";
    }

    protected static final String TRAILER = "Proc";

    protected final TableManagerBase _manager;

    protected final IJavaScriptFunction _errorCallback;
    protected IJavaScriptFunction _afterUpdateCallback;
    protected IJavaScriptFunction _rowMarkCallback;

    protected final HashMap<String, IJavaScriptFunction> _callbackFunctions = new HashMap<String, IJavaScriptFunction>();

    protected String _updateMode = UPDATEMODE.MANUAL;

    protected final String _transactionMode = TRANSACTIONMODE.POST;

    public DataProcessor(TableManagerBase manager, IJavaScriptFunction errorCallback) {

        _manager = manager;
        _errorCallback = errorCallback;
    }

    public void addCallbackFunction(String functionName, IJavaScriptFunction jsFunction) {
        _callbackFunctions.put(functionName, jsFunction);
    }

    public void setAfterUpdateFunction(IJavaScriptFunction jsFunction) {
        _afterUpdateCallback = jsFunction;
    }

    public void setRowMarkFunction(IJavaScriptFunction jsFunction) {
        _rowMarkCallback = jsFunction;
    }

    public String getControlName() {
        return _manager.getControlName() + TRAILER;
    }

    public static String getControlName(IDhtmlxControl control) {
        return control.getControlName() + TRAILER;
    }

    public String getName() {
        return _manager.getName() + TRAILER;
    }

    /**
     * Returns the scripting part that initializes the fhtmris and the callbacks
     * 
     * @return Javascript
     */
    public String getScriptingPart() {

        StringBuilder buf = new StringBuilder();

        // client Javascript wrapper
        DataProcessorClientWrapper dataprocessor = new DataProcessorClientWrapper(_manager, buf);

        dataprocessor.construct();
        dataprocessor.setUpdateMode(JSString.byVal(getUpdateMode()));
        dataprocessor.setTransactionMode(JSString.byVal(getTransactionMode()));
        dataprocessor.enableUTFEncoding();
        dataprocessor.defineAction(JSString.byVal("error"), _errorCallback);
        if (_afterUpdateCallback != null) {
            dataprocessor.setOnAfterUpdate(_afterUpdateCallback);
        }
        if (_rowMarkCallback != null) {
            dataprocessor.setOnRowMark(_rowMarkCallback);
        }
        Iterator<String> iter = _callbackFunctions.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            dataprocessor.defineAction(JSString.byVal(key), _callbackFunctions.get(key));
        }
        dataprocessor.init();

        return buf.toString();
    }

    public String getUpdateMode() {
        return _updateMode;
    }

    public void setUpdateMode(String updateMode) {
        _updateMode = updateMode;
    }

    public String getTransactionMode() {
        return _transactionMode;
    }

    public void setTransactionMode(String transactionMode) {
        _updateMode = transactionMode;
    }
}
