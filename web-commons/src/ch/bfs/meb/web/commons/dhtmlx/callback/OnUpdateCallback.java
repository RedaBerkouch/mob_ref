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
public class OnUpdateCallback extends CallbackBase {

    protected final JSString action = JSString.byRef("action");
    protected final JSString message = JSString.byRef("message");

    public OnUpdateCallback(IDhtmlxManager manager) {
        super(CallbackConstants.UpdateCallback, manager);

        // add parameters
        addParameter(action);
        addParameter(message);
    }

    public String getScriptingBody() throws DhtmlxException {

        StringBuilder buf = new StringBuilder();

        // Create wrapper
        Javascript js = new Javascript(buf);

        js.ifc(message).thenc(new CodeBlock() {

            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                Javascript js = new Javascript(buf);
                js.append(new MethodCall("alert").param(message).toString());
            }
        });

        js.returnc(JSBoolean.istrue); // dataProcessor.afterUpdateCallback
        // will be called

        return buf.toString();
    }
}