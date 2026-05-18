/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: OnGridReconstructedReloadParamsCallback.java  18.02.2010 14:02:18 msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

public class OnGridReconstructedReloadChildCallback extends MasterDetailCallbackBase {
    protected final boolean _isMiddleTable;

    public OnGridReconstructedReloadChildCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl third, boolean isMiddleTable,
            IGlobalJavaScript globals) {
        super(CallbackConstants.OnGridReconstructedReloadChildCallback, manager, other, third, globals);
        _isMiddleTable = isMiddleTable;
    }

    public OnGridReconstructedReloadChildCallback(IDhtmlxManager manager, IDhtmlxControl other, IGlobalJavaScript globals) {
        this(manager, other, null, false, globals);
    }

    public OnGridReconstructedReloadChildCallback(IDhtmlxManager manager) {
        this(manager, null, null, false, null);
    }

    @Override
    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        final TableClientWrapper other = hasOther() ? new TableClientWrapper(getOtherTable(), buf) : null;
        final TableClientWrapper third = hasThird() ? new TableClientWrapper(getThirdTable(), buf) : null;

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        doEventualRefreshButtons(js, getManager());

        if (hasOther()) {
            js.ifc(isCallingManagerMaster()).thenc(new CodeBlock() {
                @Override
                public void code(StringBuilder buf) throws DhtmlxException {
                    other.clearAll(JSBoolean.isfalse);
                    if (hasThird()) {
                        third.clearAll(JSBoolean.isfalse);
                    }

                    // Create loader with selected row ids
                    Command load = new Command(CommandConstants.LOAD);

                    // Map function parameter id with selRowId
                    load.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, table.getSelectedId());
                    load.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getManager().getName()));

                    // load data
                    other.loadXML(load);
                    if (_isMiddleTable && hasThird()) {
                        third.loadXML(load);
                    }
                }
            });

            if (hasThird() && _isMiddleTable) {
                js.elsec(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        js.ifc(isOtherManagerMaster()).thenc(new CodeBlock() {
                            @Override
                            public void code(StringBuilder buf) throws DhtmlxException {
                                third.clearAll(JSBoolean.isfalse);

                                // Create loader with selected row ids
                                Command load = new Command(CommandConstants.LOAD);

                                // Map function parameter id with selRowId
                                load.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, table.getSelectedId());
                                load.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getManager().getName()));

                                // load data
                                third.loadXML(load);
                            }
                        }).elsec(new CodeBlock() // isThirdManagerMaster()
                        {
                            @Override
                            public void code(StringBuilder buf) throws DhtmlxException {
                                other.clearAll(JSBoolean.isfalse);

                                // Create loader with selected row ids
                                Command load = new Command(CommandConstants.LOAD);

                                // Map function parameter id with selRowId
                                load.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, table.getSelectedId());
                                load.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getManager().getName()));

                                // load data
                                other.loadXML(load);
                            }
                        });
                    }
                });
            }
        }

        return buf.toString();
    }
}
