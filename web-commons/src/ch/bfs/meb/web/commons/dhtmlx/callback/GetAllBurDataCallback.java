/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: SimpleDeliveryButtonCallback.java 305 2009-12-03 10:25:28Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * TODO Describe this class
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public class GetAllBurDataCallback extends CallbackBase {
    public static final String BURSCHOOL_TABLE_LOCK_MESSAGE_KEY = "burschool.table.lock.message";

    protected final IGlobalJavaScript _globals;

    public GetAllBurDataCallback(IDhtmlxManager manager, IGlobalJavaScript globals) {
        super(CallbackConstants.GetAllBurDataCallback, manager);
        _globals = globals;
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        DataProcessorClientWrapper masterDP = new DataProcessorClientWrapper(getManager(), buf);
        alertAndReturnWhenNotSynchronized(js, masterDP, BURSCHOOL_TABLE_LOCK_MESSAGE_KEY);

        JSString filterCommand = (JSString) _globals.getGlobal(ParameterConstants.PARAM_FILTERCOMMAND);
        js.assign(filterCommand, JSString.byVal(CallbackConstants.GetAllBurDataCallback));
        buf.append(new MethodCall(getManager().getName() + CallbackConstants.FilterCallback).toString());
        js.assign(filterCommand, JSString.byVal(""));

        return buf.toString();
    }
}
