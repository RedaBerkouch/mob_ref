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
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Callback for a command issued by a button click. A row has to be selected in the table.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class SimpleButtonCallback extends CallbackBase {
    protected final String _command;
    protected final String _noSelectionErrorMessage;
    protected final String _confirmMessage;
    protected final boolean _doIncSaveNr;

    public SimpleButtonCallback(IDhtmlxManager manager, String callback, String command, String noSelectionErrorMessage) {
        this(manager, callback, command, noSelectionErrorMessage, null);
    }

    public SimpleButtonCallback(IDhtmlxManager manager, String callback, String command, String noSelectionErrorMessage, boolean doIncSaveNr) {
        this(manager, callback, command, noSelectionErrorMessage, null, doIncSaveNr);
    }

    public SimpleButtonCallback(IDhtmlxManager manager, String callback, String command, String noSelectionErrorMessage, String confirmMessage) {
        this(manager, callback, command, noSelectionErrorMessage, confirmMessage, false);
    }

    public SimpleButtonCallback(IDhtmlxManager manager, String callback, String command, String noSelectionErrorMessage, String confirmMessage,
            boolean doIncSaveNr) {
        super(callback, manager);
        _command = command;
        _noSelectionErrorMessage = noSelectionErrorMessage;
        _confirmMessage = confirmMessage;
        _doIncSaveNr = doIncSaveNr;
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();
        final IWebLocalizationManager localization = getManager().getLocalizationManager();

        // Create wrapper
        TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        // Javascript wrapper
        final Javascript js = new Javascript(buf);

        final JSNumber selectedRow = JSNumber.byRef("selectedRow");
        js.define(selectedRow, table.getSelectedId());

        js.ifnotc(selectedRow).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // Error message
                js.alert(new JSString(localization.getMessage(_noSelectionErrorMessage)));
            }
        }).elsec(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                if (_confirmMessage != null) {
                    js.ifc(js.confirm(new JSString(localization.getMessage(_confirmMessage)))).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            synchCommand(js, selectedRow);
                        }
                    });
                } else {
                    synchCommand(js, selectedRow);
                }
            }
        });

        return buf.toString();
    }

    protected void synchCommand(Javascript js, JSNumber selectedRow) {
        // Show "working" bitmap
        js.append(new MethodCall(getManager().getName() + CallbackConstants.OnLoadingStartCallback).toString());

        if (_doIncSaveNr) {
            js.incSaveNr();
        }

        // Create wrapper
        DataProcessorClientWrapper client = new DataProcessorClientWrapper(getManager(), js.getBuf());
        // Generate amend command
        Command command = new Command(_command);
        // load data
        client.synchronize(command, selectedRow);
    }
}
