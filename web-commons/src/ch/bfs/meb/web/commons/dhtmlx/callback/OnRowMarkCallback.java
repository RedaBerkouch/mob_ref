/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: OnRowMarkCallback.java  23.03.2010 11:06:55 msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;

public class OnRowMarkCallback extends CallbackBase {
    private final IDhtmlxControl _otherTable;
    private final IDhtmlxControl _thirdTable;
    private final IDhtmlxControl _forthTable;

    public OnRowMarkCallback(IDhtmlxManager manager, IDhtmlxControl otherTable, IDhtmlxControl thirdTable, IDhtmlxControl forthTable) {
        super(CallbackConstants.OnRowMarkCallback, manager);
        _otherTable = otherTable;
        _thirdTable = thirdTable;
        _forthTable = forthTable;
    }

    public OnRowMarkCallback(IDhtmlxManager manager, IDhtmlxControl otherTable, IDhtmlxControl thirdTable) {
        this(manager, otherTable, thirdTable, null);
    }

    public OnRowMarkCallback(IDhtmlxManager manager, IDhtmlxControl otherTable) {
        this(manager, otherTable, null, null);
    }

    @Override
    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        Javascript js = new Javascript(buf);

        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        js.ifc(managerDP.getSyncState()).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                buf.append(_otherTable.getControlName() + ".setEditable(true);");
                if (_thirdTable != null) {
                    buf.append(_thirdTable.getControlName() + ".setEditable(true);");
                }
                if (_forthTable != null) {
                    buf.append(_forthTable.getControlName() + ".setEditable(true);");
                }
            }
        }).elsec(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                buf.append(_otherTable.getControlName() + ".setEditable(false);");
                if (_thirdTable != null) {
                    buf.append(_thirdTable.getControlName() + ".setEditable(false);");
                }
                if (_forthTable != null) {
                    buf.append(_forthTable.getControlName() + ".setEditable(false);");
                }
            }
        });

        // Refresh buttons
        doEventualRefreshButtons(js, getManager());

        js.returnc(JSBoolean.istrue);

        return buf.toString();
    }
}
