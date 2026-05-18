/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: OnAfterUpdateCallback.java 1536 2010-05-10 08:17:54Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;

/**
 * Callback for reload of target table after update of one row
 * 
 * @author $Author: msc $
 * @version $Revision: 1536 $
 */
public class OnAfterUpdateWizardCallback extends CallbackBase {
    protected final String _callback;

    public OnAfterUpdateWizardCallback(IDhtmlxManager manager, String callback) {
        super(CallbackConstants.OnAfterUpdateCallback, manager);

        _callback = callback;
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();
        Javascript js = new Javascript(buf);

        JSNumber isLastUpdate = JSNumber.byRef("isLastUpdate()");
        js.ifc(isLastUpdate).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                MethodCall call = new MethodCall(getManager().getName() + _callback);
                buf.append(call.toString());
            }
        });

        return buf.toString();
    }
}
