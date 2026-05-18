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
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class SaveParamCallback extends MasterDetailCallbackBase {
    public SaveParamCallback(IDhtmlxManager manager, IDhtmlxControl masterManager) {
        super(CallbackConstants.SaveCallback, manager, masterManager, null, null);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        DataProcessorClientWrapper client = new DataProcessorClientWrapper(getManager(), buf);
        TableClientWrapper masterTable = new TableClientWrapper(getOtherTable(), buf);
        // Generate save command
        Command command = new Command(CommandConstants.SAVE);
        command.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, masterTable.getSelectedId());
        // load data
        client.synchronize(command);

        return buf.toString();
    }
}
