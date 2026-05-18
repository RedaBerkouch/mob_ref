/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Calls updateStatus on the server for every row with status "Imported"
 * 
 * @author $Author$
 * @version $Revision$
 */
public class RefreshStatusCallback extends CallbackBase {
    private final String _stateColumnId;
    private final String _reportColumnId;
    private final JSNumber _interval;
    private final long _deliveryStatusImported;

    public RefreshStatusCallback(IDhtmlxManager manager, String stateColumnId, String reportColumnId, Integer interval, long deliveryStatusImported) {
        super(CallbackConstants.RefreshStatusCallback, manager);
        _stateColumnId = stateColumnId;
        _reportColumnId = reportColumnId;
        _interval = new JSNumber(interval);
        _deliveryStatusImported = deliveryStatusImported;
    }

    public RefreshStatusCallback(IDhtmlxManager manager, String stateColumnId, String reportColumnId, Integer interval) {
        this(manager, stateColumnId, reportColumnId, interval, CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.javascript.JavaScriptFunctionBase#getGlobals()
     */
    @Override
    public String getGlobals() {
        MethodCall call = new MethodCall("window", "setInterval");
        call.param(new JSString(this.getMethodCall()).asVal()).param(_interval);
        return call.toString();
    }

    public String getScriptingBody() {
        // resulting Javascript:
        // function deliveryUpdateStatus () {
        // deliveryTableManager.forEachRow (function (rowId) {
        // var state = cells(rowId, stateColumnId).getValue();
        // var report = cells(rowId, reportColumnId).getValue();
        // if (state == IMPORTED || report) {
        // deliveryTableManagerProc.synchronize ("controller.do?control=deliveryTableManager&command=updateStatus",rowId)
        // }
        // });
        // }

        StringBuilder buf = new StringBuilder();

        Integer stateColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_stateColumnId);
        Integer reportColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_reportColumnId);

        Javascript js = new Javascript(buf);
        js.append(getManager().getControlName() + ".forEachRow(function(rowId){");
        js.append("var state=" + getManager().getControlName() + ".cells(rowId," + stateColumnIndex.toString() + ").getValue();");
        js.append("var report=" + getManager().getControlName() + ".cells(rowId," + reportColumnIndex.toString() + ").getValue();");
        js.append("if (state==" + _deliveryStatusImported + " || report=='true'){");
        DataProcessorClientWrapper client = new DataProcessorClientWrapper(getManager(), buf);
        // Generate updateStatus command
        Command command = new Command(CommandConstants.REFRESH_STATUS);
        // load data
        client.synchronize(command, JSNumber.byRef("rowId"));
        js.append("}});");

        return buf.toString();
    }
}
