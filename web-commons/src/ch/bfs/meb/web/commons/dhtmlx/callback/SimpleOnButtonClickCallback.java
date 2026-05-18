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
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class SimpleOnButtonClickCallback extends CallbackBase {

    protected final String _target;

    protected final JSString menuItemId = JSString.byRef("menuitemId");
    protected final JSString gridId = JSString.byRef("grid_Id");

    public SimpleOnButtonClickCallback(IDhtmlxManager manager, String target) {
        super(CallbackConstants.OnButtonClickCallback, manager);
        _target = target;

        // add parameters
        addParameter(menuItemId);
        addParameter(gridId);
    }

    public String getScriptingBody() throws DhtmlxException {

        StringBuilder buf = new StringBuilder();

        // Get target table
        // IDhtmlxManager targetManager =
        // getManager().getRegisteredManager(_target);

        // // Create wrapper
        // TableClientWrapper target = new TableClientWrapper(targetManager,
        // buf);
        // MenuClientWrapper menu = new MenuClientWrapper(getManager(), buf);

        buf.append("var data=grid_Id.split(\"_\"); "); // rowId_colInd
        // buf.append(targetManager.getControlName());
        // buf.append(".setRowTextStyle(data[0],\"background-color:blue\");");
        buf.append("alert('onButtonClick '+menuitemId+' '+data[0]+' '+data[1] );");

        return buf.toString();
    }
}
