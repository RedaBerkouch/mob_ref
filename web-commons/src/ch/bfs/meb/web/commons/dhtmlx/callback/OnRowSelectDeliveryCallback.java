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
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;

/**
 * Generate Javascript code for onRowSelect function of delivery table
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnRowSelectDeliveryCallback extends OnRowSelectCallback {
    public OnRowSelectDeliveryCallback(IDhtmlxManager manager, IDhtmlxControl target, IGlobalJavaScript globals) {
        super(manager, target, null, false, globals);
    }

    protected JSNumber getId() {
        return JSNumber.byRef("rowId");
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder(super.getScriptingBody());
        Javascript js = new Javascript(buf);

        // refresh buttons
        js.append(new MethodCall(getManager().getName() + CallbackConstants.RefreshButtonsCallback).param(getId()).toString());
        js.append(new MethodCall(getOtherTable().getName() + CallbackConstants.RefreshButtonsCallback).param(JSNumber.byVal(0)).toString());

        doEventualDisplayNumbers(js, getManager());

        return buf.toString();
    }
}
