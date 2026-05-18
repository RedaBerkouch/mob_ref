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
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * Callback for reload of target table after update of one row
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnAfterUpdateCallback extends MasterDetailCallbackBase {
    protected final JSNumber _sid = JSNumber.byRef("sid");
    protected final JSString _action = JSString.byRef("action");
    protected final JSNumber _tid = JSNumber.byRef("tid");
    protected final JSString _pane = JSString.byRef("pane");

    protected final boolean _isMiddleTable;

    public OnAfterUpdateCallback(IDhtmlxManager manager, IDhtmlxControl target, IDhtmlxControl target2, boolean isMiddleTable) {
        super(CallbackConstants.OnAfterUpdateCallback, manager, target, target2, null);

        _isMiddleTable = isMiddleTable;

        // add parameters
        addParameter(_sid);
        addParameter(_action);
        addParameter(_tid);
    }

    public OnAfterUpdateCallback(IDhtmlxManager manager, IDhtmlxControl target) {
        this(manager, target, null, false);
    }

    public OnAfterUpdateCallback(IDhtmlxManager manager) {
        this(manager, null, null, false);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        TableClientWrapper target = hasOther() ? new TableClientWrapper(getOtherTable(), buf) : null;
        TableClientWrapper target2 = hasThird() ? new TableClientWrapper(getThirdTable(), buf) : null;

        Javascript js = new Javascript(buf);

        // deactivate load cursor
        js.define(_pane, JSString.byRef("dijit.byId('" + getManager().getName() + "Panel');"));
        js.ifc(_pane).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                buf.append("pane.setLoadingEnd();");
            }
        });

        // Display numbers
        doEventualDisplayNumbers(js, getManager());

        // Refresh buttons
        doEventualRefreshButtons(js, getManager());

        // Show plausi report if command is set
        JSString command = JSString.byRef("command");
        js.define(command, table.getUserData(_sid, JSString.byVal("command")));
        buf.append("if (command && (command=='").append(CommandConstants.SHOW_LAST_PLAUSIREPORT).append("')){");
        js.append(new MethodCall(getManager().getName() + CallbackConstants.ShowLastPlausireportCallback).toString());
        buf.append("}");

        if (target != null) {
            // reload target table
            target.clearAll(JSBoolean.isfalse);
            Command load = new Command(CommandConstants.LOAD);
            load.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, _sid);
            target.loadXML(load);
        }

        if (target2 != null) {
            target2.clearAll(JSBoolean.isfalse);
            if (_isMiddleTable) {
                // reload target2 table
                Command load = new Command(CommandConstants.LOAD);
                load.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, _sid);
                target.loadXML(load);
            }
        }

        return buf.toString();
    }
}
