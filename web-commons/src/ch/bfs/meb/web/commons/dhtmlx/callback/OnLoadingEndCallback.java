/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnLoadingEndCallback extends CallbackBase {
    private final JSString _pane = JSString.byRef("pane");
    private final boolean _displayNumbers;
    private final boolean _selectFirst;
    private final boolean _activateFilter;

    public OnLoadingEndCallback(IDhtmlxManager manager, boolean displayNumbers, boolean selectFirst, boolean activateFilter) {
        super(CallbackConstants.OnLoadingEndCallback, manager);
        _displayNumbers = displayNumbers;
        _selectFirst = selectFirst;
        _activateFilter = activateFilter;
    }

    public OnLoadingEndCallback(IDhtmlxManager manager, boolean displayNumbers, boolean selectFirst) {
        this(manager, displayNumbers, selectFirst, false);
    }

    public OnLoadingEndCallback(IDhtmlxManager manager, boolean displayNumbers) {
        this(manager, displayNumbers, false);
    }

    public String getScriptingBody() throws DhtmlxException {
        final String ctrlName = getManager().getControlName();
        StringBuilder buf = new StringBuilder();

        final Javascript js = new Javascript(buf);

        js.ifc(JSString.byRef(ctrlName + ".loadMessage")).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) {
                buf.append("alert(" + ctrlName + ".loadMessage" + ");");
            }
        });

        // js.append (new MethodCall (ctrlName, "clearSelection").toString());
        // update sort image, if sort is done by server
        if (getManager().isServerSort()) {
            js.append(new MethodCall(getManager().getName() + CallbackConstants.ShowSortImgCallback).toString());
        }

        js.assign(JSString.byRef(ctrlName + ".onLoading"), JSBoolean.byVal(false));

        js.define(_pane, JSString.byRef("dijit.byId('" + getManager().getName() + "Panel')"));
        js.ifc(_pane).thenc(new CodeBlock() {

            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                buf.append("pane.setLoadingEnd();");
                if (_displayNumbers) {
                    buf.append(new MethodCall(getManager().getName() + CallbackConstants.DisplayNumbersCallback).toString());
                }

                doEventualRefreshButtons(js, getManager());
            }
        });

        if (_selectFirst) {
            buf.append("if(" + ctrlName + ".getRowsNum() > 0 && " + ctrlName + ".objBox.scrollTop == 0){window.setTimeout('" + ctrlName
                    + ".selectRow(0,true)',1000);}");
        }

        if (_activateFilter) {
            buf.append("activateFilter('" + ctrlName + "');");
        }

        return buf.toString();
    }
}
