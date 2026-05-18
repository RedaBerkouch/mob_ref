/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: NotImplementedCallback.java  09.02.2010 13:32:17 msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

public class NotImplementedCallback extends CallbackBase {
    protected final String NOT_IMPLEMENTED_MESSAGE = "not.implemented.message";

    public NotImplementedCallback(IDhtmlxManager manager) {
        super(CallbackConstants.NotImplementedCallback, manager);
    }

    @Override
    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        MethodCall alert = new MethodCall("alert");
        alert.param(JSString.byVal(getManager().getLocalizationManager().getMessage(NOT_IMPLEMENTED_MESSAGE)));
        js.append(alert.toString());

        return buf.toString();
    }
}
