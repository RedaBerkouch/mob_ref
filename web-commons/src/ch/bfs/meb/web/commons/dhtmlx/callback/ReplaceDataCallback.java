/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: RefreshStatusCallback.java 2646 2012-10-12 07:12:33Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Calls replaceData on the server for every row with toBeReplacedValue, data will be replaced in ShowReplaceDataCallback
 * 
 * @author $Author: msc $
 * @version $Revision: 2646 $
 */
public class ReplaceDataCallback extends CallbackBase {
    protected final JSNumber _rowId = JSNumber.byRef("rowId");

    private final String _data1ColumnId;

    public ReplaceDataCallback(IDhtmlxManager manager, String data1ColumnId) {
        super(CallbackConstants.ReplaceDataCallback, manager);
        _data1ColumnId = data1ColumnId;

        // add parameters
        addParameter(_rowId);
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();

        Integer data1ColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_data1ColumnId);

        Javascript js = new Javascript(buf);
        js.append("if(" + getManager().getControlName() + ".cells(rowId," + data1ColumnIndex.toString() + ").getValue()=='"
                + getManager().getLocalizationManager().getMessage(MebUtils.getDeliveryToBeLoadedMessage()) + "'){");
        js.append("dhtmlxAjax.get('" + Command.BASEURL + "?control=" + getManager().getControlName() + "&command=" + CommandConstants.REPLACE_DATA
                + "&gr_id='+rowId+'&t='+(new Date().getTime())," + getManager().getName() + CallbackConstants.ShowReplaceDataCallback + ");");
        js.append("}");

        return buf.toString();
    }
}
