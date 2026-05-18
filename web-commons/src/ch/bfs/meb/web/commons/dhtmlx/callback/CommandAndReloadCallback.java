/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: CommandAndReloadCallback.java 2145 2010-11-16 07:56:46Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Command;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * Callback for a command issued by a button click. The whole (tab-)page will be reloaded
 * 
 * @author $Author: msc $
 * @version $Revision: 2145 $
 */
public class CommandAndReloadCallback extends CallbackBase {
    protected JSString _methodParam = null;
    protected final String _command;
    protected final String _loadPage;
    protected final boolean _doIncSaveNr;
    protected String _confirmMessage = null;

    public CommandAndReloadCallback(IDhtmlxManager manager, String callback, String command, String methodParam, String loadPage, boolean doIncSaveNr,
            String confirmMessage) {
        super(callback, manager);
        _command = command;
        _loadPage = loadPage;
        _doIncSaveNr = doIncSaveNr;
        _confirmMessage = confirmMessage;

        if (methodParam != null) {
            _methodParam = JSString.byRef(methodParam);
            addParameter(_methodParam);
        }
    }

    public CommandAndReloadCallback(IDhtmlxManager manager, String callback, String command, String methodParam, String loadPage, boolean doIncSaveNr) {
        this(manager, callback, command, methodParam, loadPage, doIncSaveNr, null);
    }

    public CommandAndReloadCallback(IDhtmlxManager manager, String callback, String command, String loadPage, boolean doIncSaveNr, String confirmMessage) {
        this(manager, callback, command, null, loadPage, doIncSaveNr, confirmMessage);
    }

    public CommandAndReloadCallback(IDhtmlxManager manager, String callback, String command, String loadPage, boolean doIncSaveNr) {
        this(manager, callback, command, loadPage, doIncSaveNr, null);
    }

    public CommandAndReloadCallback(IDhtmlxManager manager, String callback, String command, String loadPage) {
        this(manager, callback, command, null, loadPage, false);
    }

    public CommandAndReloadCallback(IDhtmlxManager manager, String callback, String command, String methodParam, String loadPage) {
        this(manager, callback, command, methodParam, loadPage, false);
    }

    protected void doLoad(Javascript js) {
        StringBuilder buf = js.getBuf();

        if (_doIncSaveNr) {
            js.incSaveNr();
        }

        buf.append("showWait(true);");

        Command cmd = new Command(_command);

        if (_methodParam != null) {
            cmd.param(_methodParam.toString(), _methodParam);
        }

        cmd.setControl(getManager());

        buf.append(
                getManager().getControlName() + ".loadXML(" + cmd.toString() + ", function() { document.location='" + _loadPage + "?saveNr=' + _saveNr; });");
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();
        final Javascript js = new Javascript(buf);

        if (_confirmMessage != null) {
            js.ifc(js.confirm(JSString.byVal(getManager().getLocalizationManager().getMessage(_confirmMessage)))).thenc(new CodeBlock() {
                @Override
                public void code(StringBuilder buf) throws DhtmlxException {
                    doLoad(js);
                }
            });
        } else {
            doLoad(js);
        }

        return buf.toString();
    }
}
