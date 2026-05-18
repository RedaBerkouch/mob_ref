/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: OnBeforeSelect.java  03.09.2010 11:01:20 msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

public class OnBeforeSelectCallback extends CallbackBase {
    protected final JSString idd = JSString.byRef("idd");
    protected final JSString psid = JSString.byRef("psid");

    public OnBeforeSelectCallback(IDhtmlxManager manager) {
        super(CallbackConstants.OnBeforeSelectCallback, manager);

        addParameter(idd);
        addParameter(psid);
    }

    public String getScriptingBody() throws DhtmlxException {
        return "return idd == null || idd == '' || ('' + idd).charAt(0) != 'd';";
    }
}
