/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * Calls updateStatus on the server for every row with status "Imported"
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ReloadAllCallback extends CallbackBase {
    public ReloadAllCallback(IDhtmlxManager manager) {
        super(CallbackConstants.ReloadAllCallback, manager);
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();
        DataProcessorClientWrapper dataProcessor = new DataProcessorClientWrapper(getManager(), buf);
        Javascript js = new Javascript(buf);

        js.append(getManager().getControlName() + ".forEachRow(function(rowId){");
        // Reload row
        Command command = new Command(CommandConstants.RELOAD);
        command.param(ParameterConstants.PARAM_ROWID, JSNumber.byRef("rowId"));
        dataProcessor.synchronize(command, JSNumber.byRef("rowId"));
        js.append("});");

        return buf.toString();
    }
}
