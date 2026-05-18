/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSArray;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnLoadErrorCallback extends CallbackBase {
    protected final JSString type = JSString.byRef("type");
    protected final JSString desc = JSString.byRef("desc");
    protected final JSString erData = JSString.byRef("erData");

    protected final String _secondTable;
    protected final String _thirdTable;
    protected final boolean _stopShowWait;

    public OnLoadErrorCallback(IDhtmlxManager manager, String secondTableName, String thirdTableName, boolean stopShowWait) {
        super(CallbackConstants.LoadErrorCallback, manager);

        // add parameters
        addParameter(type);
        addParameter(desc);
        addParameter(erData);

        _secondTable = secondTableName;
        _thirdTable = thirdTableName;
        _stopShowWait = stopShowWait;
    }

    public OnLoadErrorCallback(IDhtmlxManager manager, String secondTableName, String thirdTableName) {
        this(manager, secondTableName, thirdTableName, false);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final Javascript js = new Javascript(buf);

        JSString req = JSString.byRef("req");
        js.define(req, JSArray.byRef(erData.asVar()).valueAt(0));
        js.ifc(js.compare(JSString.byRef("req.responseText"), Javascript.NE, JSString.byVal(""))).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                js.alert(JSString.byRef("req.responseText"));
            }
        });

        // Stop load display
        js.append(new MethodCall(getManager().getName() + CallbackConstants.OnLoadingEndCallback).toString());
        if (_secondTable != null) {
            js.append(new MethodCall(_secondTable + CallbackConstants.OnLoadingEndCallback).toString());
        }
        if (_thirdTable != null) {
            js.append(new MethodCall(_thirdTable + CallbackConstants.OnLoadingEndCallback).toString());
        }
        if (_stopShowWait) {
            buf.append("showWait(false);");
        }

        js.returnc(JSBoolean.isfalse);

        return buf.toString();
    }
}