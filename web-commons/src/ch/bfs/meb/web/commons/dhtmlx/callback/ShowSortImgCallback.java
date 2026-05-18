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
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSArray;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * This Class generates a javascript undo callback handler.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ShowSortImgCallback extends CallbackBase {
    protected final JSArray _sortState = JSArray.byRef("sortState");
    protected final JSNumber _sortCol = JSNumber.byRef("sortCol");
    protected final JSString _sortOrder = JSString.byRef("sortOrder");

    public ShowSortImgCallback(IDhtmlxManager manager) {
        super(CallbackConstants.ShowSortImgCallback, manager);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        TableClientWrapper table = new TableClientWrapper(getManager(), buf);

        Javascript js = new Javascript(buf);
        js.define(_sortState, table.getSortingState());

        js.ifc(_sortState.length()).thenc(new CodeBlock() {

            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                Javascript js = new Javascript(buf);
                js.define(_sortCol, _sortState.valueAt(0));
                js.define(_sortOrder, _sortState.valueAt(1));
                new TableClientWrapper(getManager(), buf).setSortImgState(JSString.byVal("true"), _sortCol, _sortOrder);
            }
        });

        return buf.toString();
    }
}
