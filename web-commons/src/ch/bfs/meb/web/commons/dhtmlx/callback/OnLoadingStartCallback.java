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
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnLoadingStartCallback extends CallbackBase {

    private final JSString _pane = JSString.byRef("pane");

    public OnLoadingStartCallback(IDhtmlxManager manager) {
        super(CallbackConstants.OnLoadingStartCallback, manager);

        // add parameters
    }

    public String getScriptingBody() throws DhtmlxException {

        StringBuilder buf = new StringBuilder();

        Javascript js = new Javascript(buf);

        js.define(_pane, JSString.byRef("dijit.byId('" + getManager().getName() + "Panel')"));

        js.ifc(_pane).thenc(new CodeBlock() {

            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                buf.append("pane.setLoadingStart();");
            }
        });

        js.assign(JSString.byRef(getManager().getControlName() + ".onLoading"), JSBoolean.byVal(true));

        return buf.toString();
    }
}
