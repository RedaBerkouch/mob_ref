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
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DeleteRowCallback extends MasterDetailCallbackBase {
    public static final String DELETE_TABLE_LOCK_MESSAGE_KEY = "delete.table.lock.message";

    protected String _deleteAlertMessage = null;

    public DeleteRowCallback(IDhtmlxManager manager, IDhtmlxControl other, IGlobalJavaScript globals, String deleteAlertMessage) {
        this(manager, other, null, globals, deleteAlertMessage);
    }

    public DeleteRowCallback(IDhtmlxManager manager, IDhtmlxControl other, IGlobalJavaScript globals) {
        this(manager, other, null, globals, null);
    }

    public DeleteRowCallback(IDhtmlxManager manager, String deleteAlertMessage) {
        this(manager, null, null, null, deleteAlertMessage);
    }

    public DeleteRowCallback(IDhtmlxManager manager) {
        this(manager, null, null, null, null);
    }

    public DeleteRowCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl third, IGlobalJavaScript globals) {
        this(manager, other, third, globals, null);
    }

    public DeleteRowCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl third, IGlobalJavaScript globals, String deleteAlertMessage) {
        super(CallbackConstants.DeleteRowCallback, manager, other, third, globals);
        _deleteAlertMessage = deleteAlertMessage;
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Javascript wrapper
        Javascript js = new Javascript(buf);

        if (hasOther()) {
            DataProcessorClientWrapper otherDP = new DataProcessorClientWrapper(getOtherTable(), buf);
            alertAndReturnWhenNotSynchronized(js, otherDP, DELETE_TABLE_LOCK_MESSAGE_KEY);
        }

        if (hasThird()) {
            DataProcessorClientWrapper thirdDP = new DataProcessorClientWrapper(getThirdTable(), buf);
            alertAndReturnWhenNotSynchronized(js, thirdDP, DELETE_TABLE_LOCK_MESSAGE_KEY);
        }

        if (_deleteAlertMessage != null) {
            js.alert(new JSString(getManager().getLocalizationManager().getMessage(_deleteAlertMessage)));
        }

        // Create wrapper
        TableClientWrapper table = new TableClientWrapper(getManager(), buf);

        // delete all selected items
        table.deleteSelectedItem();

        return buf.toString();
    }
}
