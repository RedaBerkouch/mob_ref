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
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnErrorCallback extends CallbackBase {
    protected final JSString _action = JSString.byRef("action");
    protected final JSString _message = JSString.byRef("message");
    protected final boolean _stopShowWait;

    public OnErrorCallback(IDhtmlxManager manager, boolean stopShowWait) {
        super(CallbackConstants.ErrorCallback, manager);

        // add parameters
        addParameter(_action);
        addParameter(_message);

        _stopShowWait = stopShowWait;
    }

    public OnErrorCallback(IDhtmlxManager manager) {
        this(manager, false);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final Javascript js = new Javascript(buf);

        js.ifc(js.compare(_message, Javascript.NE, JSString.byVal(""))).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                js.alert(_message);
            }
        });
        if (_stopShowWait) {
            buf.append("showWait(false);");
        }
        js.append(new MethodCall(getManager().getName() + CallbackConstants.OnLoadingEndCallback).toString());
        js.returnc(JSBoolean.isfalse);

        return buf.toString();
    }
}