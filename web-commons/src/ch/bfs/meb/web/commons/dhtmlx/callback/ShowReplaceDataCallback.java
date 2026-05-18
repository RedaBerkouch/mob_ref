/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: RefreshStatusCallback.java 2646 2012-10-12 07:12:33Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Ajax loading function for replacing values toBeReplacedValue (See ReplaceDataCallback)
 * 
 * @author $Author: msc $
 * @version $Revision: 2646 $
 */
public class ShowReplaceDataCallback extends CallbackBase {
    public static final String LOADING_KEY = "!*loading*!";

    protected final JSNumber _loader = JSNumber.byRef("loader");

    private final static String NONE = "-";

    private final String _data1ColumnId;
    private final String _data2ColumnId;
    private final String _data3ColumnId;

    public ShowReplaceDataCallback(IDhtmlxManager manager, String data1ColumnId) {
        this(manager, data1ColumnId, NONE, NONE);
    }

    public ShowReplaceDataCallback(IDhtmlxManager manager, String data1ColumnId, String data2ColumnId) {
        this(manager, data1ColumnId, data2ColumnId, NONE);
    }

    public ShowReplaceDataCallback(IDhtmlxManager manager, String data1ColumnId, String data2ColumnId, String data3ColumnId) {
        super(CallbackConstants.ShowReplaceDataCallback, manager);
        _data1ColumnId = data1ColumnId;
        _data2ColumnId = data2ColumnId;
        _data3ColumnId = data3ColumnId;

        // add parameters
        addParameter(_loader);
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();

        Integer data1ColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_data1ColumnId);
        Integer data2ColumnIndex = _data2ColumnId.equals(NONE) ? -1 : ((TableManagerBase) getManager()).getColumnIndexById(_data2ColumnId);
        Integer data3ColumnIndex = _data3ColumnId.equals(NONE) ? -1 : ((TableManagerBase) getManager()).getColumnIndexById(_data3ColumnId);

        Javascript js = new Javascript(buf);
        js.append("var dataValues=loader.xmlDoc.responseText.split(\"#\");");
        js.append("if(dataValues[1]=='" + LOADING_KEY + "'){");
        js.append("window.setTimeout(function(){dhtmlxAjax.get('" + Command.BASEURL + "?control=" + getManager().getControlName() + "&command="
                + CommandConstants.REPLACE_DATA + "&gr_id='+dataValues[0]+'&t='+(new Date().getTime())," + getManager().getName()
                + CallbackConstants.ShowReplaceDataCallback + ");},1000);");
        js.append("}else{");
        js.append(getManager().getControlName() + ".cells(dataValues[0]," + data1ColumnIndex.toString() + ").setValue(dataValues[1]);");
        if (!data2ColumnIndex.equals(-1)) {
            js.append(getManager().getControlName() + ".cells(dataValues[0]," + data2ColumnIndex.toString() + ").setValue(dataValues[2]);");
        }
        if (!data3ColumnIndex.equals(-1)) {
            js.append(getManager().getControlName() + ".cells(dataValues[0]," + data3ColumnIndex.toString() + ").setValue(dataValues[3]);");
        }
        js.append("}");

        return buf.toString();
    }
}
