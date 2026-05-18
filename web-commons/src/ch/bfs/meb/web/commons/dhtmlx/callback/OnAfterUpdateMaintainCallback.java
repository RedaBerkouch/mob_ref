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
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * Callback for reload of target table(s) after update of one row
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnAfterUpdateMaintainCallback extends MasterDetailCallbackBase {
    protected final JSNumber _sid = JSNumber.byRef("sid");
    protected final JSString _action = JSString.byRef("action");
    protected final JSNumber _tid = JSNumber.byRef("tid");
    protected final IDhtmlxControl _firstParent;
    protected final IDhtmlxControl _secondParent;

    public OnAfterUpdateMaintainCallback(IDhtmlxManager manager, IDhtmlxControl firstChild, IDhtmlxControl secondChild, IDhtmlxControl firstParent,
            IDhtmlxControl secondParent) {
        super(CallbackConstants.OnAfterUpdateCallback, manager, firstChild, secondChild, null);
        _firstParent = firstParent;
        _secondParent = secondParent;

        // add parameters
        addParameter(_sid);
        addParameter(_action);
        addParameter(_tid);
    }

    public OnAfterUpdateMaintainCallback(IDhtmlxManager manager, IDhtmlxControl target, IDhtmlxControl target2) {
        this(manager, target, target2, null, null);
    }

    public OnAfterUpdateMaintainCallback(IDhtmlxManager manager, IDhtmlxControl target) {
        this(manager, target, null, null, null);
    }

    public OnAfterUpdateMaintainCallback(IDhtmlxManager manager) {
        this(manager, null, null, null, null);
    }

    public boolean hasFirstChild() {
        return hasOther();
    }

    public IDhtmlxControl getFirstChildTable() {
        return getOtherTable();
    }

    public boolean hasSecondChild() {
        return hasThird();
    }

    public IDhtmlxControl getSecondChildTable() {
        return getThirdTable();
    }

    public boolean hasFirstParent() {
        return _firstParent != null;
    }

    public IDhtmlxControl getFirstParentTable() {
        return _firstParent;
    }

    public boolean hasSecondParent() {
        return _secondParent != null;
    }

    public IDhtmlxControl getSecondParentTable() {
        return _secondParent;
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        TableClientWrapper table = new TableClientWrapper(getManager(), buf);

        Javascript js = new Javascript(buf);

        // Reload selected rows if command is set
        JSString command = JSString.byRef("command");
        js.define(command, table.getUserData(_sid, JSString.byVal("command")));

        // Reload children (and parents)
        buf.append("if (command && (command=='").append(CommandConstants.RELOAD_CHILDREN).append("')){");
        js.append(new MethodCall(getManager().getName() + CallbackConstants.ReloadSelectedCallback).toString());
        if (hasFirstChild()) {
            // reload first child table
            js.append(new MethodCall(getFirstChildTable().getName() + CallbackConstants.ReloadAllCallback).toString());
        }
        if (hasSecondChild()) {
            // reload second child table
            js.append(new MethodCall(getSecondChildTable().getName() + CallbackConstants.ReloadAllCallback).toString());
        }
        if (hasFirstParent()) {
            // reload first parent table
            js.append(new MethodCall(getFirstParentTable().getName() + CallbackConstants.ReloadAllCallback).toString());
        }
        if (hasSecondParent()) {
            // reload second parent table
            js.append(new MethodCall(getSecondParentTable().getName() + CallbackConstants.ReloadAllCallback).toString());
        }
        buf.append("}");

        // Reload parent
        buf.append("if (command && (command=='").append(CommandConstants.RELOAD_PARENT).append("')){");
        if (hasFirstParent()) {
            // reload parent table
            js.append(new MethodCall(getFirstParentTable().getName() + CallbackConstants.ReloadAllCallback).toString());
        }
        if (hasSecondParent()) {
            // reload second parent table
            js.append(new MethodCall(getSecondParentTable().getName() + CallbackConstants.ReloadAllCallback).toString());
        }
        buf.append("}");

        if (getFirstChildTable() != null) { // Bugfix with MANTIS-2261: SBG2.35 ref: mit Ereignisse als Haupttabelle, beim Löschen einer Person , wird die Anzeige nicht richtig gemacht.
            buf.append("if (action && (action=='delete')) {");
            buf.append(getFirstChildTable().getName() + CallbackConstants.FilterCallback + "();");
            buf.append("}");
        }
        if (getSecondChildTable() != null) {
            buf.append("if (action && (action=='delete')) {");
            buf.append(getSecondChildTable().getName() + CallbackConstants.FilterCallback + "();");
            buf.append("}");
        }

        return buf.toString();
    }
}
