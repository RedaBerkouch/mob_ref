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
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

public class DuplicateRecordCallback extends MasterDetailCallbackBase {
    public enum Master {
        ALWAYS, MANAGER_MUST_BE_MASTER, MANAGER_MUST_NOT_BE_MASTER, OTHER_MUST_BE_MASTER
    };

    public static final String INSERT_TABLE_LOCK_MESSAGE_KEY = "insert.table.lock.message";

    private final JSNumber _index = JSNumber.byRef("index");

    protected final Master _master;
    protected final String _message;

    public DuplicateRecordCallback(IDhtmlxManager manager) {
        this(manager, null, null, Master.ALWAYS, null, null);
    }

    public DuplicateRecordCallback(IDhtmlxManager manager, IDhtmlxControl other, IGlobalJavaScript globals) {
        this(manager, other, null, Master.ALWAYS, globals, null);
    }

    public DuplicateRecordCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl third, IGlobalJavaScript globals) {
        this(manager, other, third, Master.ALWAYS, globals, null);
    }

    public DuplicateRecordCallback(IDhtmlxManager manager, Master master, IGlobalJavaScript globals, String message) {
        this(manager, null, null, master, globals, message);
    }

    public DuplicateRecordCallback(IDhtmlxManager manager, IDhtmlxControl other, Master master, IGlobalJavaScript globals, String message) {
        this(manager, other, null, master, globals, message);
    }

    public DuplicateRecordCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl third, Master master, IGlobalJavaScript globals, String message) {
        super(CallbackConstants.DuplicateRecordCallback, manager, other, third, globals);
        _message = message;
        _master = master;
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final DataProcessorClientWrapper callingTableDP = new DataProcessorClientWrapper(getManager(), buf);
        TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        final TableClientWrapper otherTable = new TableClientWrapper(getOtherTable(), buf);
        final Javascript js = new Javascript(buf);

        if (hasGlobals()) {
            switch (_master) {
            case OTHER_MUST_BE_MASTER:
                js.ifnotc(isOtherManagerMaster()).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        js.alert(new JSString(getManager().getLocalizationManager().getMessage(_message)));
                        js.returnc(JSBoolean.isfalse);
                    }
                });
                break;
            case MANAGER_MUST_BE_MASTER:
                js.ifnotc(isCallingManagerMaster()).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        js.alert(new JSString(getManager().getLocalizationManager().getMessage(_message)));
                        js.returnc(JSBoolean.isfalse);
                    }
                });
                break;
            case MANAGER_MUST_NOT_BE_MASTER:
                js.ifc(isCallingManagerMaster()).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        js.alert(new JSString(getManager().getLocalizationManager().getMessage(_message)));
                        js.returnc(JSBoolean.isfalse);
                    }
                });
                break;
            }
        }

        if (hasOther()) {
            DataProcessorClientWrapper otherDP = new DataProcessorClientWrapper(getOtherTable(), buf);
            alertAndReturnWhenNotSynchronized(js, otherDP, INSERT_TABLE_LOCK_MESSAGE_KEY);
        }

        if (hasThird()) {
            DataProcessorClientWrapper thirdDP = new DataProcessorClientWrapper(getThirdTable(), buf);
            alertAndReturnWhenNotSynchronized(js, thirdDP, INSERT_TABLE_LOCK_MESSAGE_KEY);
        }

        // Set variable
        js.define(_index, JSString.byRef("(new Date()).valueOf()"));

        // Duplicate the selected record
        table.duplicateRecord(_index);
        table.setSelectedRow(_index);
        
        return buf.toString();
    }
}
