/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;

/**
 * Generate Javascript code for onRowSelect function of delivery table
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnRowSelectAdminCallback extends OnRowSelectCallback {
    public OnRowSelectAdminCallback(IDhtmlxManager manager, IDhtmlxControl target, IGlobalJavaScript globals) {
        super(manager, target, null, false, globals);
    }

    protected JSNumber getId() {
        return JSNumber.byRef("rowId");
    }
}
